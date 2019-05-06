package roart.aggregator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.data.ExtraData;
import roart.aggregatorindicator.impl.AggregatorMLIndicator;
import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.executor.MyExecutors;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.common.MLClassifyModel;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.talib.util.TaUtil;

public class MLIndicator extends Aggregator {

    Map<String, PeriodData> periodDataMap;
    String key;
    Map<String, Double[][]> listMap;
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();

    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;

    private static final String MYTITLE = "comb";

    @Override
    public Map<String, Object> getResultMap() {
        return new HashMap<>();
    }

    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }

    public Map<String, Double[][]> getListMap() {
        return listMap;
    }

    private int fieldSize = 0;

    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;

    @Override
    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        Map<Integer, List<ResultItemTableRow>> retMap = new HashMap<>();
        if (mlTimesTableRows != null) {
            retMap.put(Constants.MLTIMES, mlTimesTableRows);
        }
        if (eventTableRows != null) {
            retMap.put(Constants.EVENT, eventTableRows);
        }
        return retMap;
    }

    List<MLClassifyDao> mldaos = new ArrayList<>();

    public MLIndicator(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, 
            String title, int category, AbstractCategory[] categories, Pipeline[] datareaders) throws Exception {
        super(conf, string, category);
        this.periodDataMap = periodDataMap;
        this.key = title;
        makeMapTypes();
        if (conf.wantML()) {
            if (conf.wantMLSpark()) {
                mldaos.add(new MLClassifyDao("spark", conf));
            }
            if (conf.wantMLTensorflow()) {
                mldaos.add(new MLClassifyDao("tensorflow", conf));
            }
        }
        fieldSize = fieldSize();
        if (conf.wantMLTimes()) {
            mlTimesTableRows = new ArrayList<>();
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        if (isEnabled()) {
            calculateMomentums(conf, marketdatamap, categories, datareaders);
            cleanMLDaos();
        }
    }

    private void cleanMLDaos() {
        for (MLClassifyDao mldao : mldaos) {
            mldao.clean();
        }        
    }

    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }

    private void makeMapTypes() {
        mapTypes.put(0, MYTITLE);
    }

    @Override
    public List<Integer> getTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(0);
        return retList;
    }

    private Map<Integer, String> mapTypes = new HashMap<>();

    public Map<double[], Double> getEvaluations(MyMyConfig conf, int j, Object[] retObj) throws JsonParseException, JsonMappingException, IOException {
        int listlen = conf.getTableDays();
        List<Map<String, Double[][]>> listList = (List<Map<String, Double[][]>>) retObj[2];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
        if (dayIndicatorMap == null) {
            int jj = 0;
        }
        Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
        if (indicatorMap == null) {
            return new HashMap<>();
        }
        Map<double[], Double> retMap = new HashMap<>();
        for (String id : indicatorMap.keySet()) {
            int newlistidx = listlen - 1 - j + conf.getAggregatorsIndicatorFuturedays();
            int curlistidx = listlen - 1 - j;
            Double[][] list = listList.get(0).get(id);
            if (list[0][newlistidx] == null || list[0][curlistidx] == null) {
                continue;
            }
            double change = list[0][newlistidx]/list[0][curlistidx] - 1;
            double[] merged = ArraysUtil.convert(indicatorMap.get(id));

            // cat 1.0 is for >= threshold, 2.0 is for belov
            Double cat = 2.0;
            if (change > conf.getAggregatorsIndicatorThreshold()) {
                cat = 1.0;
            }
            retMap.put(merged, cat);
        }
        return retMap;
    }

    // TODO make an oo version of this
    private void calculateMomentums(MyMyConfig conf, Map<String, MarketData> marketdatamap,
            AbstractCategory[] categories, Pipeline[] datareaders) throws Exception {
        AbstractCategory cat = IndicatorUtils.getWantedCategory(categories, category);
        if (cat == null) {
            return;
        }
        log.info("checkthis {}", category == cat.getPeriod());
        log.info("checkthis {}", title.equals(cat.getTitle()));
        log.info("checkthis {}", key.equals(title));
        Map<String, Pipeline> pipelineMap = new HashMap<>();
        for (Pipeline datareader : datareaders) {
            pipelineMap.put(datareader.pipelineName(), datareader);
        }
        Pipeline extrareader = pipelineMap.get(PipelineConstants.EXTRAREADER);
        Map<String, Object> localResults =  extrareader.getLocalResultMap();
        Map<Pair<String, String>, List<StockItem>> pairStockMap = (Map<Pair<String, String>, List<StockItem>>) localResults.get(PipelineConstants.PAIRSTOCK);
        Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap = (Map<Pair<String, String>, Map<Date, StockItem>>) localResults.get(PipelineConstants.PAIRDATE);
        Map<Pair<String, String>, String> pairCatMap = (Map<Pair<String, String>, String>) localResults.get(PipelineConstants.PAIRCAT);

        List<Date> dateList = (List<Date>) pipelineMap.get("" + this.category).getLocalResultMap().get(PipelineConstants.DATELIST);
        Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();
        Map<String, AbstractIndicator> usedIndicatorMap = cat.getIndicatorMap();

        Map<String, List<AggregatorMLIndicator>> usedIndicators = AggregatorMLIndicator.getUsedAggregatorMLIndicators(conf);
        Set<String> ids = new HashSet<>();
        Map<String, Map<String, Object>> localResultMap = cat.getIndicatorLocalResultMap();
        Map<String, Double[][]> list0 = (Map<String, Double[][]>) localResultMap.get(localResultMap.keySet().iterator().next()).get(PipelineConstants.LIST);
        ids.addAll(list0.keySet());
        TaUtil tu = new TaUtil();
        List<AbstractIndicator> indicators = getIndicators(marketdatamap, datareaders, cat, newIndicatorMap, usedIndicatorMap,
                usedIndicators, ids);

        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        this.listMap = list0;
        if (!anythingHere(listMap)) {
            log.info("empty {}", key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        otherResultMap = new HashMap<>();
        probabilityMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();
        otherMeta = new ArrayList<>();
        objectMap = new HashMap<>();
        long time2 = System.currentTimeMillis();
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();

        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
        ids.addAll(list0.keySet());
        getMergedLists(cat, ids, indicators);

        int macdlen = conf.getTableDays();
        ExtraData extraData = new ExtraData(dateList, pairStockMap, pairDateMap, cat.getPeriod(), pairCatMap, categories, datareaders);
        int tableDays = Math.min(conf.getTableDays(), dateList.size());
        Object[] retObj2 = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, conf.getAggregatorsIndicatorFuturedays(), tableDays, conf.getAggregatorsIndicatorIntervaldays(), extraData);
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj2[0];
        Map<double[], Double> mergedCatMap = new HashMap<>();
        for (int j = conf.getAggregatorsIndicatorFuturedays(); j < macdlen; j += conf.getAggregatorsIndicatorIntervaldays()) {
            Map<double[], Double> retMap = getEvaluations(conf, j, retObj2);
            mergedCatMap.putAll(retMap);
        }


        // map from h/m to model to posnegcom map<model, results>
        Map<MLClassifyModel, Map<String, Double[]>> mapResult = new HashMap<>();
        log.info("Period {} {}", title, mapMap.keySet());
        String nnconfigString = conf.getAggregatorsMLIndicatorMLConfig();
        NeuralNetConfigs nnConfigs = null;
        if (nnconfigString != null) {
            ObjectMapper mapper = new ObjectMapper();
            nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);
        }
        if (conf.wantML() && !mergedCatMap.keySet().isEmpty()) {
            if (mergedCatMap.keySet().isEmpty()) {
                log.info("Merget set empty");
            }
            // TODO add a null check
            int arrayLength = mergedCatMap.keySet().iterator().next().length;
            for(double[] array : mergedCatMap.keySet()) {
                if (array.length != arrayLength) {
                    log.info("Different lengths {} {}", arrayLength, array.length);
                }
            }
            Map<Double, String> labelMapShort = createLabelMapShort();
            if (conf.wantMLMP()) {
                doLearnTestClassifyFuture(nnConfigs, conf, dayIndicatorMap, mergedCatMap, mapResult, arrayLength, labelMapShort);
            } else {
                doLearnTestClassify(nnConfigs, conf, dayIndicatorMap, mergedCatMap, mapResult, arrayLength, labelMapShort);
           }
        }
        createResultMap(conf, mapResult);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        // and others done with println
        handleOtherStats(conf, mergedCatMap);
        handleSpentTimes(conf);

    }

    private void doLearnTestClassify(NeuralNetConfigs nnconfigs, MyMyConfig conf, Map<Integer, Map<String, Double[]>> dayIndicatorMap,
            Map<double[], Double> mergedCatMap, Map<MLClassifyModel, Map<String, Double[]>> mapResult, int arrayLength,
            Map<Double, String> labelMapShort) {
        try {
            int testCount = 0;   
            // calculate sections and do ML
            log.info("Indicatormap keys {}", dayIndicatorMap.keySet());
            Map<String, Double[]> indicatorMap2 = dayIndicatorMap.get(conf.getAggregatorsIndicatorFuturedays());
            Map<String, double[]> indicatorMap3 = new HashMap<>();
            if (indicatorMap2 != null) {
                for (Entry<String, Double[]> indicatorEntry : indicatorMap2.entrySet()) {
                    indicatorMap3.put(indicatorEntry.getKey(), ArraysUtil.convert(indicatorEntry.getValue()));
                }
            }
            for (MLClassifyDao mldao1 : mldaos) {
                Map<double[], Double> map1 = mergedCatMap;
                for (MLClassifyModel model1 : mldao1.getModels()) {          
                    Map<Object, Long> countMap1 = map1.values().stream().collect(Collectors.groupingBy(e2 -> labelMapShort.get(e2), Collectors.counting()));                            
                    // make OO of this, create object
                    Object[] meta1 = new Object[6];
                    meta1[0] = mldao1.getName();
                    meta1[1] = model1.getName();
                    meta1[2] = model1.getReturnSize();
                    meta1[3] = countMap1;
                    resultMetaArray.add(meta1);
                    ResultMeta resultMeta1 = new ResultMeta();
                    resultMeta1.setMlName(mldao1.getName());
                    resultMeta1.setModelName(model1.getName());
                    resultMeta1.setReturnSize(model1.getReturnSize());
                    resultMeta1.setLearnMap(countMap1);
                    getResultMetas().add(resultMeta1);
                    Map<String, double[]> map = indicatorMap3;
                    if (map == null) {
                        log.error("map null ");
                        testCount++;
                        continue;
                    } else {
                        log.info("keyset {}", map.keySet());
                    }
                    log.info("len {}", arrayLength);
                    LearnTestClassifyResult result = mldao1.learntestclassify(nnconfigs, this, map1, model1, arrayLength, key, MYTITLE, 2, mapTime, map, labelMapShort);  
                    Map<String, Double[]> classifyResult = result.getCatMap();
                    probabilityMap.put(mldao1.getName() + model1.getId(), result.getAccuracy());
                    meta1[4] = result.getAccuracy();
                    resultMeta1.setTestAccuracy(result.getAccuracy());
                    mapResult.put(model1, classifyResult);
                    IndicatorUtils.filterNonExistingClassifications(labelMapShort, classifyResult);
                    Map<String, Long> countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                    StringBuilder counts = new StringBuilder("classified ");
                    for (Entry<String, Long> countEntry : countMap.entrySet()) {
                        counts.append(countEntry.getKey() + " : " + countEntry.getValue() + " ");
                    }
                    addEventRow(counts.toString(), "", "");  
                    Object[] meta = resultMetaArray.get(testCount);
                    meta[5] = countMap;
                    ResultMeta resultMeta = getResultMetas().get(testCount);
                    resultMeta.setClassifyMap(countMap);
                    testCount++;
                }
            }
        } catch (Exception e1) {
            log.error("Exception", e1);
        }
        for (MLClassifyDao mldao : mldaos) {
            for (MLClassifyModel model : mldao.getModels()) {
            }
        }
    }

    private void doLearnTestClassifyFuture(NeuralNetConfigs nnconfigs, MyMyConfig conf, Map<Integer, Map<String, Double[]>> dayIndicatorMap,
            Map<double[], Double> mergedCatMap, Map<MLClassifyModel, Map<String, Double[]>> mapResult, int arrayLength,
            Map<Double, String> labelMapShort) {
        try {
            // calculate sections and do ML
            log.info("Indicatormap keys {}", dayIndicatorMap.keySet());
            Map<String, Double[]> indicatorMap2 = dayIndicatorMap.get(conf.getAggregatorsIndicatorFuturedays());
            Map<String, double[]> indicatorMap3 = new HashMap<>();
            if (indicatorMap2 != null) {
                for (Entry<String, Double[]> indicatorEntry : indicatorMap2.entrySet()) {
                    indicatorMap3.put(indicatorEntry.getKey(), ArraysUtil.convert(indicatorEntry.getValue()));
                }
            }
            List<Future<LearnTestClassifyResult>> futureList = new ArrayList<>();
            Map<Future<LearnTestClassifyResult>, FutureMap> futureMap = new HashMap<>();
            for (MLClassifyDao mldao : mldaos) {
                Map<double[], Double> map1 = mergedCatMap;
                for (MLClassifyModel model : mldao.getModels()) {          
                    Map<Object, Long> countMap1 = map1.values().stream().collect(Collectors.groupingBy(e2 -> labelMapShort.get(e2), Collectors.counting()));                            
                    // make OO of this, create object
                    Object[] meta1 = new Object[6];
                    meta1[0] = mldao.getName();
                    meta1[1] = model.getName();
                    meta1[2] = model.getReturnSize();
                    meta1[3] = countMap1;
                    resultMetaArray.add(meta1);
                    ResultMeta resultMeta1 = new ResultMeta();
                    resultMeta1.setMlName(mldao.getName());
                    resultMeta1.setModelName(model.getName());
                    resultMeta1.setReturnSize(model.getReturnSize());
                    resultMeta1.setLearnMap(countMap1);
                    getResultMetas().add(resultMeta1);
                    Map<String, double[]> map = indicatorMap3;
                    if (map == null) {
                        log.error("map null ");
                        continue;
                    } else {
                        log.info("keyset {}", map.keySet());
                    }
                    log.info("len {}", arrayLength);
                    //LearnTestClassifyResult result = mldao.learntestclassify(this, map1, model, arrayLength, key, MYTITLE, 2, mapTime, map, labelMapShort);  
                    Callable callable = new MLClassifyLearnTestPredictCallable(nnconfigs, mldao, this, map1, model, arrayLength, key, MYTITLE, 4, mapTime, map, labelMapShort);  
                    Future<LearnTestClassifyResult> future = MyExecutors.run(callable, 1);
                    futureList.add(future);
                    futureMap.put(future, new FutureMap(mldao, model, resultMetaArray.size() - 1));
                }
            }
            for (Future<LearnTestClassifyResult> future: futureList) {
                FutureMap futMap = futureMap.get(future);
                MLClassifyDao mldao = futMap.getDao();
                MLClassifyModel model = futMap.getModel();
                int testCount = futMap.getTestCount();
                LearnTestClassifyResult result = future.get();
                Map<String, Double[]> classifyResult = result.getCatMap();
                probabilityMap.put(mldao.getName() + model.getId(), result.getAccuracy());
                Object[] meta = resultMetaArray.get(testCount);
                ResultMeta resultMeta = getResultMetas().get(testCount);
                meta[4] = result.getAccuracy();
                resultMeta.setTestAccuracy(result.getAccuracy());
                log.info("keys" + Arrays.deepToString(classifyResult.values().toArray()));
                log.info("keys" + classifyResult.keySet());
                //log.info("ke2 " + classifyResult.values().stream().toString());
                mapResult.put(model, classifyResult);
                IndicatorUtils.filterNonExistingClassifications(labelMapShort, classifyResult);
                Map<String, Long> countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                StringBuilder counts = new StringBuilder("classified ");
                for (Entry<String, Long> countEntry : countMap.entrySet()) {
                    counts.append(countEntry.getKey() + " : " + countEntry.getValue() + " ");
                }
                addEventRow(counts.toString(), "", "");  
                meta[5] = countMap;
                resultMeta.setClassifyMap(countMap);
            }
        } catch (Exception e1) {
            log.error("Exception", e1);
        }
    }

    private void doLearnTestClassifyOld(NeuralNetConfigs nnconfigs, MyMyConfig conf, Map<Integer, Map<String, Double[]>> dayIndicatorMap,
            Map<double[], Double> mergedCatMap, Map<MLClassifyModel, Map<String, Double[]>> mapResult, int arrayLength,
            Map<Double, String> labelMapShort) {
        doLearningAndTests(nnconfigs, mergedCatMap, arrayLength, labelMapShort);
        // calculate sections and do ML
        log.info("Indicatormap keys {}", dayIndicatorMap.keySet());
        Map<String, Double[]> indicatorMap2 = dayIndicatorMap.get(conf.getAggregatorsIndicatorFuturedays());
        Map<String, double[]> indicatorMap3 = new HashMap<>();
        if (indicatorMap2 != null) {
            for (Entry<String, Double[]> indicatorEntry : indicatorMap2.entrySet()) {
                indicatorMap3.put(indicatorEntry.getKey(), ArraysUtil.convert(indicatorEntry.getValue()));
            }
        }
        doClassifications(mapResult, arrayLength, labelMapShort, indicatorMap3);
    }

    private void getMergedLists(AbstractCategory cat, Set<String> ids, List<AbstractIndicator> indicators) {
        Map<String, Object[]> result = new HashMap<>();
        for (String id : ids) {
            Object[] arrayResult = new Object[0];
            for (AbstractIndicator indicator : indicators) {
                String indicatorName = indicator.indicatorName();
                // TODO fix
                Map<String, Double[][]> aListMap = (Map<String, Double[][]>) cat.getIndicatorLocalResultMap().get(indicatorName).get(PipelineConstants.LIST);
                Double[][] aResult = aListMap.get(id);
                arrayResult = ArrayUtils.addAll(arrayResult, aResult[0]);
            }
            result.put(id, arrayResult);
        }
    }

    private void handleSpentTimes(MyMyConfig conf) {
        if (conf.wantMLTimes()) {
            for (Map.Entry<MLClassifyModel, Long> entry : mapTime.entrySet()) {
                MLClassifyModel model = entry.getKey();
                ResultItemTableRow row = new ResultItemTableRow();
                row.add("MLIndicator " + key);
                row.add(model.getEngineName());
                row.add(model.getName());
                row.add(entry.getValue());
                mlTimesTableRows.add(row);
            }
        }
    }

    private void handleOtherStats(MyMyConfig conf, Map<double[], Double> mergedCatMap) {
        if (conf.wantOtherStats() && conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();            
            Map<Double, Long> countMap = mergedCatMap.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
            StringBuilder counts = new StringBuilder();
            for (Entry<Double, Long> countEntry : countMap.entrySet()) {
                counts.append(labelMapShort.get(countEntry.getKey()) + " : " + countEntry.getValue() + " ");
            }
            addEventRow(counts.toString(), "", "");
        }
    }

    private void createResultMap(MyMyConfig conf, Map<MLClassifyModel, Map<String, Double[]>> mapResult) {
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[fieldSize];
            resultMap.put(id, fields);
            int retindex = 0 ;

            // TODO make OO of this
            if (conf.wantML()) {
                Map<Double, String> labelMapShort2 = createLabelMapShort();
                for (MLClassifyDao mldao : mldaos) {
                    for (MLClassifyModel model : mldao.getModels()) {
                        Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                        mapResult2.put(MYTITLE, mapResult.get(model));
                        List<Integer> typeList = getTypeList();
                        for (int mapTypeInt : typeList) {
                            String mapType = mapTypes.get(mapTypeInt);
                            Map<String, Double[]> resultMap1 = mapResult2.get(mapType);
                            Double[] aType = null;
                            if (resultMap1 != null) {
                                aType = resultMap1.get(id);
                            } else {
                                log.info("map null  {}", mapType);
                            }
                            fields[retindex++] = aType != null ? labelMapShort2.get(aType[0]) : null;
                            if (model.getReturnSize() > 1) {
                                fields[retindex++] = aType != null ? aType[1] : null;
                            }
                            //retindex = mldao.addResults(fields, retindex, id, model, this, mapResult2, labelMapShort2);
                        }
                    }   
                }
            }
        }
    }

    private List<AbstractIndicator> getIndicators(Map<String, MarketData> marketdatamap, Pipeline[] datareaders, AbstractCategory cat,
            Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap,
            Map<String, List<AggregatorMLIndicator>> usedIndicators, Set<String> ids) throws Exception {
        Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
        for (Entry<String, List<AggregatorMLIndicator>> entry : usedIndicators.entrySet()) {
            List<AggregatorMLIndicator> list = entry.getValue();
            for (AggregatorMLIndicator ind : list) {
                String indicator = ind.indicator();
                if (indicator != null) {
                    indicatorMap.put(indicator, ind.getIndicator(marketdatamap, category, newIndicatorMap, usedIndicatorMap, datareaders));
                }
                // TODO fix
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                ids.retainAll(aResult.keySet());
            }
        }
        return new ArrayList<>(indicatorMap.values());
    }

    private void doClassifications(Map<MLClassifyModel, Map<String, Double[]>> mapResult, int arrayLength,
            Map<Double, String> labelMapShort, Map<String, double[]> indicatorMap3) {
        int testCount = 0;   
        for (MLClassifyDao mldao : mldaos) {
            for (MLClassifyModel model : mldao.getModels()) {
                Map<String, double[]> map = indicatorMap3;
                if (map == null) {
                    log.error("map null ");
                    testCount++;
                    continue;
                } else {
                    log.info("keyset {}", map.keySet());
                }
                log.info("len {}", arrayLength);
                Map<String, Double[]> classifyResult = mldao.classify(this, map, model, arrayLength, key, MYTITLE, 2, labelMapShort, mapTime);
                mapResult.put(model, classifyResult);
                IndicatorUtils.filterNonExistingClassifications(labelMapShort, classifyResult);
                Map<String, Long> countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                StringBuilder counts = new StringBuilder("classified ");
                for (Entry<String, Long> countEntry : countMap.entrySet()) {
                    counts.append(countEntry.getKey() + " : " + countEntry.getValue() + " ");
                }
                addEventRow(counts.toString(), "", "");  
                Object[] meta = resultMetaArray.get(testCount);
                meta[5] = countMap;
                ResultMeta resultMeta = getResultMetas().get(testCount);
                resultMeta.setClassifyMap(countMap);
                testCount++;
            }
        }
    }

    private void doLearningAndTests(NeuralNetConfigs nnconfigs, Map<double[], Double> mergedCatMap, int arrayLength,
            Map<Double, String> labelMapShort) {
        try {
            for (MLClassifyDao mldao : mldaos) {
                Map<double[], Double> map = mergedCatMap;
                for (MLClassifyModel model : mldao.getModels()) {          
                    Double testAccuracy = mldao.learntest(nnconfigs, this, map, model, arrayLength, key, MYTITLE, 2, mapTime);  
                    probabilityMap.put(mldao.getName() + model.getId(), testAccuracy);
                    IndicatorUtils.filterNonExistingClassifications2(labelMapShort, map);
                    Map<Object, Long> countMap = map.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e), Collectors.counting()));                            
                    // make OO of this, create object
                    Object[] meta = new Object[6];
                    meta[0] = mldao.getName();
                    meta[1] = model.getName();
                    meta[2] = model.getReturnSize();
                    meta[3] = countMap;
                    meta[4] = testAccuracy;
                    resultMetaArray.add(meta);
                    ResultMeta resultMeta = new ResultMeta();
                    resultMeta.setMlName(mldao.getName());
                    resultMeta.setModelName(model.getName());
                    resultMeta.setReturnSize(model.getReturnSize());
                    resultMeta.setLearnMap(countMap);
                    resultMeta.setTestAccuracy(testAccuracy);
                    getResultMetas().add(resultMeta);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private boolean anythingHere(Map<String, Double[][]> listMap2) {
        for (Double[][] array : listMap2.values()) {
            for (int i = 0; i < array.length; i++) {
                int len = array[i].length;
                if (array[i][len - 1] != null) {
                    return true;
                }
                if (array[i][0] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void mapAdder(Map<MLClassifyModel, Long> map, MLClassifyModel key, Long add) {
        Long val = map.get(key);
        if (val == null) {
            val = Long.valueOf(0);
        }
        val += add;
        map.put(key, val);
    }

    public void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add(key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }

    public static Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, Constants.INC);
        labelMap1.put(2.0, Constants.DEC);
        return labelMap1;
    }

    @Override
    public Object calculate(double[] array) {
        TaUtil tu = new TaUtil();
        return tu.getMomAndDeltaFull(array, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
    }

    @Override
    public boolean isEnabled() {
        if (!conf.wantAggregatorsIndicatorMACD() && !conf.wantAggregatorsIndicatorRSI()) {
            return false;
        }
        return conf.wantAggregatorsIndicatorML();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        String market = conf.getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new Pair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        if (perioddata == null) {
            log.info("key {} : {}", key, periodDataMap.keySet());
        }
        Object[] result = resultMap.get(id);
        if (result == null) {
            result = emptyField;
        }
        return result;
    }

    @Override
    public Object[] getResultItemTitle() {
        Object[] objs = new Object[fieldSize];
        int retindex = 0;
        objs[retindex++] = title + Constants.WEBBR + "hist";
        if (conf.isMACDHistogramDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "hist";
        }
        objs[retindex++] = title + Constants.WEBBR + "mom";
        if (conf.isMACDDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        retindex = getTitles(retindex, objs);
        log.info("fieldsizet {}", retindex);
        return objs;
    }

    private int fieldSize() {
        int size = 0;
        for (MLClassifyDao mldao : mldaos) {
            size += mldao.getSizes(this);
        }
        emptyField = new Object[size];
        return size;
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] objs = new Object[fieldSize];
        Object[] fields = objs;
        if (resultMap != null) {
            fields = resultMap.get(stock.getId());
        }
        row.addarr(fields);        
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        int retindex = 0;
        Object[] objs = new Object[fieldSize];
        getTitles(retindex, objs);
        headrow.addarr(objs);
    }

    private int getTitles2(int retindex, Object[] objs) {
        for (MLClassifyDao mldao : mldaos) {
            //retindex = mldao.addTitles(objs, retindex, this, title, key, MYTITLE);
        }
        return retindex;
    }

    private int getTitles(int retindex, Object[] objs) {
        // TODO make OO of this
        for (MLClassifyDao mldao : mldaos) {
            for (MLClassifyModel model : mldao.getModels()) {
                List<Integer> typeList = getTypeList();
                for (int mapTypeInt : typeList) {
                    String mapType = mapTypes.get(mapTypeInt);
                    String val = "";
                    // TODO workaround
                    try {
                        val = "" + MLClassifyModel.roundme((Double) probabilityMap.get(mldao.getName() + model.getId()));
                    } catch (Exception e) {
                        log.error("Exception fix later, refactor", e);
                    }
                    objs[retindex++] = title + Constants.WEBBR +  model.getName() + mapType + " " + val;
                    if (model.getReturnSize() > 1) {
                        objs[retindex++] = title + Constants.WEBBR +  model.getName() + mapType + " prob ";
                    }
                }
            }
        }
        return retindex;
    }

    @Override
    public String getName() {
        return PipelineConstants.MLINDICATOR;
    }

    private class FutureMap {
        private MLClassifyDao dao;

        private MLClassifyModel model;

        private int testCount;
        
        public FutureMap(MLClassifyDao dao, MLClassifyModel model, int testCount) {
            super();
            this.dao = dao;
            this.model = model;
            this.testCount = testCount;
        }

        public MLClassifyDao getDao() {
            return dao;
        }

        public void setDao(MLClassifyDao dao) {
            this.dao = dao;
        }

        public MLClassifyModel getModel() {
            return model;
        }

        public void setModel(MLClassifyModel model) {
            this.model = model;
        }

        public int getTestCount() {
            return testCount;
        }

        public void setTestCount(int testCount) {
            this.testCount = testCount;
        }
    }
}


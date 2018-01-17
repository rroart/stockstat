package roart.aggregate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.category.Category;
import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.indicator.IndicatorUtils;
import roart.ml.MLClassifyDao;
import roart.ml.MLClassifyModel;
import roart.model.ResultItemTableRow;
import roart.model.ResultMeta;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public class MLIndicator extends Aggregator {

    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
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
            retMap.put(ControlService.MLTIMES, mlTimesTableRows);
        }
        if (eventTableRows != null) {
            retMap.put(ControlService.EVENT, eventTableRows);
        }
        return retMap;
    }

    List<MLClassifyDao> mldaos = new ArrayList<>();

    public MLIndicator(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, 
            String title, int category, Category[] categories, Pipeline[] datareaders) throws Exception {
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
            Object[] objs = new Object[fieldSize];
            int retindex = 0;
            objs[retindex++] = "";
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        if (isEnabled()) {
            calculateMomentums(conf, marketdatamap, categories, datareaders);        
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
            Category[] categories, Pipeline[] datareaders) throws Exception {
        Category cat = IndicatorUtils.getWantedCategory(categories);
        if (cat == null) {
            return;
        }
        category = cat.getPeriod();
        title = cat.getTitle();
        key = title;
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
        Map<String, Indicator> newIndicatorMap = new HashMap<>();
        Map<String, Indicator> usedIndicatorMap = cat.getIndicatorMap();

        Map<String, List<AggregatorMLIndicator>> usedIndicators = AggregatorMLIndicator.getUsedAggregatorMLIndicators(conf);
        Set<String> ids = new HashSet<>();
        Map<String, Map<String, Object>> localResultMap = cat.getIndicatorLocalResultMap();
        Map<String, Double[][]> list0 = (Map<String, Double[][]>) localResultMap.get(localResultMap.keySet().iterator().next()).get(PipelineConstants.LIST);
        ids.addAll(list0.keySet());
        TaUtil tu = new TaUtil();
        List<Indicator> indicators = getIndicators(marketdatamap, datareaders, cat, newIndicatorMap, usedIndicatorMap,
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
        Object[] retObj2 = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, conf.getAggregatorsIndicatorFuturedays(), conf.getTableDays(), conf.getAggregatorsIndicatorIntervaldays(), extraData);
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj2[0];
        Map<double[], Double> mergedCatMap = new HashMap<>();
        for (int j = conf.getAggregatorsIndicatorFuturedays(); j < macdlen; j += conf.getAggregatorsIndicatorIntervaldays()) {
            Map<double[], Double> retMap = getEvaluations(conf, j, retObj2);
            mergedCatMap.putAll(retMap);
        }


        // map from h/m to model to posnegcom map<model, results>
        Map<MLClassifyModel, Map<String, Double[]>> mapResult = new HashMap<>();
        log.info("Period {} {}", title, mapMap.keySet());
        if (conf.wantML()) {
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
            doLearningAndTests(mergedCatMap, arrayLength, labelMapShort);
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
        createResultMap(conf, mapResult);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        // and others done with println
        handleOtherStats(conf, mergedCatMap);
        handleSpentTimes(conf);

    }

    private void getMergedLists(Category cat, Set<String> ids, List<Indicator> indicators) {
        Map<String, Object[]> result = new HashMap<>();
        for (String id : ids) {
            Object[] arrayResult = new Object[0];
            for (Indicator indicator : indicators) {
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
                            retindex = mldao.addResults(fields, retindex, id, model, this, mapResult2, labelMapShort2);
                        }
                    }   
                }
            }
        }
    }

    private List<Indicator> getIndicators(Map<String, MarketData> marketdatamap, Pipeline[] datareaders, Category cat,
            Map<String, Indicator> newIndicatorMap, Map<String, Indicator> usedIndicatorMap,
            Map<String, List<AggregatorMLIndicator>> usedIndicators, Set<String> ids) throws Exception {
        Map<String, Indicator> indicatorMap = new HashMap<>();
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
                    continue;
                } else {
                    log.info("keyset {}", map.keySet());
                }
                log.info("len {}", arrayLength);
                Map<String, Double[]> classifyResult = mldao.classify(this, map, model, arrayLength, key, MYTITLE, 2, labelMapShort, mapTime);
                mapResult.put(model, classifyResult);
                Map<String, Long> countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                StringBuilder counts = new StringBuilder("classified ");
                for (Entry<String, Long> countEntry : countMap.entrySet()) {
                    counts.append(countEntry.getKey() + " : " + countEntry.getValue() + " ");
                }
                addEventRow(counts.toString(), "", "");  
                Object[] meta = resultMetaArray.get(testCount++);
                meta[5] = countMap;
                ResultMeta resultMeta = getResultMetas().get(testCount - 1);
                resultMeta.setClassifyMap(countMap);
            }
        }
    }

    private void doLearningAndTests(Map<double[], Double> mergedCatMap, int arrayLength,
            Map<Double, String> labelMapShort) {
        try {
            for (MLClassifyDao mldao : mldaos) {
                Map<double[], Double> map = mergedCatMap;
                for (MLClassifyModel model : mldao.getModels()) {          
                    Double testAccuracy = mldao.learntest(this, map, model, arrayLength, key, MYTITLE, 2, mapTime);  
                    probabilityMap.put(mldao.getName(), testAccuracy);
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
                if (array[0][i] != null) {
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
            retindex = mldao.addTitles(objs, retindex, this, title, key, MYTITLE);
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
                        val = "" + MLClassifyModel.roundme((Double) probabilityMap.get(mldao.getName()));
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

}


package roart.aggregator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.aggregatorindicator.impl.AggregatorMLIndicator;
import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialResultMeta;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.executor.MyExecutors;
import roart.iclij.config.IclijConfig;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.data.ExtraData;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.talib.Ta;
import roart.talib.impl.TalibMACD;

public class MLIndicator extends Aggregator {

    String key;
    Map<String, Double[][]> listMap;
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();

    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;

    private static final String MYTITLE = "comb";

    private static final int cats = 2;
    
    private static final double interval = 0.01;
    
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

    public MLIndicator(IclijConfig conf, String string, String title, int category, 
            PipelineData[] datareaders, NeuralNetCommand neuralnetcommand, List<String> stockDates, Inmemory inmemory) throws Exception {
        super(conf, string, category, inmemory);
        this.key = title;
        makeMapTypes();
        if (conf.wantML()) {
            if (true) {
                if (conf.wantMLSpark()) {
                    mldaos.add(new MLClassifyDao(MLConstants.SPARK, conf));
                }
                if (conf.wantMLTensorflow()) {
                    mldaos.add(new MLClassifyDao(MLConstants.TENSORFLOW, conf));
                }
                if (conf.wantMLPytorch()) {
                    mldaos.add(new MLClassifyDao(MLConstants.PYTORCH, conf));
                }
                if (conf.wantMLGem()) {
                    mldaos.add(new MLClassifyDao(MLConstants.GEM, conf));
                }
            } else {
                mldaos.add(new MLClassifyDao(MLConstants.RANDOM, conf));
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
            calculateMomentums(conf, datareaders, neuralnetcommand);
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

    public void getEvaluations(IclijConfig conf, int j, Object[] retObj, List<String> dateList, Map<String, List<Pair<Object, Double>>> mergedCatMap, Double threshold) throws JsonParseException, JsonMappingException, IOException {
        int listlen = conf.getTableDays();
        if (listlen == 0) {
            listlen = dateList.size();
        }
        List<Map<String, Double[][]>> listList = (List<Map<String, Double[][]>>) retObj[2];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
        if (dayIndicatorMap == null) {
            int jj = 0;
        }
        Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
        if (indicatorMap == null) {
            return;
        }
        for (String id : indicatorMap.keySet()) {
            Double cat = null;
            Double[] anArray = indicatorMap.get(id);
            double[] merged = ArraysUtil.convert(anArray);
            Double[][] list = listList.get(0).get(id);
            listlen = list[0].length;
            int newlistidx = listlen - 1 - j + conf.getAggregatorsIndicatorFuturedays();
            int curlistidx = listlen - 1 - j;
            if (newlistidx < listlen && curlistidx >= 0) {
                if (list[0][newlistidx] == null || list[0][curlistidx] == null) {
                    continue;
                }
                double change = list[0][newlistidx]/list[0][curlistidx];

                // cat 1.0 is for >= threshold, 2.0 is for belov
                cat = getCat(change, threshold);
            }
            mapGetter4(mergedCatMap, id).add(new ImmutablePair(merged, cat));
            //retMap.put(id, new ImmutablePair(merged, cat));
        }
    }

    static <V, P> List<V> mapGetter4(Map<P, List<V>> mapMap, P key) {
        return mapMap.computeIfAbsent(key, k -> new ArrayList<>());
    }

    public void getEvaluations(IclijConfig conf, int start, Object[] retObj, List<String> dateList, Map<String, List<Pair<Object, Double>>> mergedCatArrayMap, Set<String> ids, Double threshold) throws JsonParseException, JsonMappingException, IOException {
        int days = conf.getAggregatorsIndicatorDays();
        if (days == 0) {
            days = dateList.size();
        }
        int listlen = conf.getTableDays();
        if (listlen == 0) {
            listlen = dateList.size();
        }
        List<Map<String, Double[][]>> listList = (List<Map<String, Double[][]>>) retObj[2];
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
        //System.out.println("kk" + dayIndicatorMap.keySet());
        if (dayIndicatorMap == null) {
            int jj = 0;
        }
        int fut = conf.getAggregatorsIndicatorFuturedays();
        out:
            for (String id : ids) {
                double[][] arr = new double[days][];
                for (int j = 0; j < days; j++) {
                    int mykey = start + j;
                    Map<String, Double[]> indicatorMap = dayIndicatorMap.get(mykey);
                    if (indicatorMap == null) {
                        continue out;
                    }
                    Double[] anArray = indicatorMap.get(id);
                    if (anArray == null) {
                        continue out;
                    }
                    double[] merged = ArraysUtil.convert(anArray);
                    arr[days - 1 - j] = merged;
                }
                Double[][] list = listList.get(0).get(id);
                listlen = list[0].length;
                int newlistidx = listlen - 1 - start + fut;
                int curlistidx = listlen - 1 - start;
                Double cat = null;
                if (newlistidx < listlen && curlistidx >= 0) {
                    if (list[0][newlistidx] == null || list[0][curlistidx] == null) {
                        continue;
                    }
                    double change = list[0][newlistidx]/list[0][curlistidx];

                    // cat 1.0 is for >= threshold, 2.0 is for belov
		    cat = getCat(change, threshold);
                }
                mapGetter4(mergedCatArrayMap, id).add(new ImmutablePair(arr, cat));
            //retMap.put(id, new ImmutablePair(arr, cat));
            }
    }

    private void calculateMomentums(IclijConfig conf, PipelineData[] datareaders,
            NeuralNetCommand neuralnetcommand) throws Exception {
        log.info("checkthis {}", key.equals(title));
        PipelineData datareader = PipelineUtils.getPipeline(datareaders, key, inmemory);
        PipelineData extrareader = PipelineUtils.getPipeline(datareaders, PipelineConstants.EXTRAREADER, inmemory);
        /*
        Map<Pair<String, String>, List<StockDTO>> pairStockMap = null; // (Map<Pair<String, String>, List<StockDTO>>) localResults.get(PipelineConstants.PAIRSTOCK);
        Map<Pair<String, String>, Map<Date, StockDTO>> pairDateMap = null; // (Map<Pair<String, String>, Map<Date, StockDTO>>) localResults.get(PipelineConstants.PAIRDATE);
        Map<Pair<String, String>, String> pairCatMap = null; // (Map<Pair<String, String>, String>) localResults.get(PipelineConstants.PAIRCAT);
        */
        log.info("KEY" + this.key + " " + PipelineUtils.getPipelineMapKeys(datareaders) + " " + title + " ");
        List<String> dateList = PipelineUtils.getDatelist(datareader);
	List<String> dateList2 = new ArrayList<>(dateList); // StockDao.getDateList(conf.getConfigData().getMarket(), marketdatamap);
        if (extrareader.get(PipelineConstants.MARKETSTOCKS) != null) {
            dateList = PipelineUtils.getDatelist(extrareader);
            Collections.sort(dateList);
        }
        Map<String, PipelineData> usedIndicatorMap = PipelineUtils.getPipelineMapStartsWith(datareaders, PipelineConstants.INDICATOR);

        Map<String, List<AggregatorMLIndicator>> usedIndicators = AggregatorMLIndicator.getUsedAggregatorMLIndicators(conf);
        Set<String> ids = new HashSet<>();
        Map<String, Double[][]> list0 = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.LIST));
        ids.addAll(list0.keySet());
        List<String> indicators = getIndicators(datareaders, usedIndicators, ids, inmemory);
        log.info("INDIC" + usedIndicators.values().iterator().next().stream().map(AggregatorMLIndicator::indicator).toList());
        log.info("INDIC" + indicators);
        
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        this.listMap = list0;
        if (!anythingHereA(listMap)) {
            log.info("empty {}", key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        otherResultMap = new HashMap<>();
        accuracyMap = new HashMap<>();
        lossMap = new HashMap<>();
        otherMeta = new ArrayList<>();
        objectMap = new HashMap<>();
        long time2 = System.currentTimeMillis();
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();

        Map<String, Map<Object, Double>> mapMap = new HashMap<>();
        ids.addAll(list0.keySet());
        getMergedLists(ids, indicators, datareaders);

        int days = conf.getTableDays();
        if (days == 0) {
            days = dateList.size();
        }
        // TODO datelist
        ExtraData extraData = new ExtraData(dateList, datareaders, extrareader);
        int tableDays = Math.min(days, dateList.size());
        Object[] retObj2 = IndicatorUtils.getDayIndicatorMap(conf, indicators, 0, tableDays, 1, extraData, datareaders, dateList2, inmemory);
        //Map<Double, Pair> thresholdMap = new HashMap<>();
        Map<Double, Map<String, Map<String, Double[]>>> mapResult0 = new HashMap<>();
        Double[] thresholds = getThresholds(conf);
        for (Double threshold : thresholds) {
            Map<String, List<Pair<Object, Double>>> mergedCatMap = new HashMap<>();
            Map<String, List<Pair<Object, Double>>> mergedCatArrayMap = new HashMap<>();
            for (int j = 0; j < days; j += conf.getAggregatorsIndicatorIntervaldays()) {
                //String d = dateList.get(j);
                getEvaluations(conf, j, retObj2, dateList, mergedCatMap, threshold);
                //mergedCatMap.putAll(retMap);
                getEvaluations(conf, j, retObj2, dateList, mergedCatArrayMap, ids, threshold);
                //mergedCatArrayMap.putAll(retMap2);
            }
            //thresholdMap.put(threshold, new ImmutablePair(mergedCatMap, mergedCatArrayMap));

        //Object[] retObj3 = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, conf.getAggregatorsIndicatorFuturedays(), conf.getAggregatorsIndicatorDays() + conf.getAggregatorsIndicatorFuturedays() + conf.getAggregatorsIndicatorExtrasDeltas(), 1, extraData);
        //Map<Integer, Map<String, Double[]>> dayIndicatorMap2 = (Map<Integer, Map<String, Double[]>>) retObj3[0];

        // map from h/m to model to posnegcom map<model, results>
        Map<String, Map<String, Double[]>> mapResult = new HashMap<>();
        mapResult0.put(threshold, mapResult);
        log.info("Period {} {}", title, mapMap.keySet());
        String nnconfigString = conf.getAggregatorsMLIndicatorMLConfig();
        NeuralNetConfigs nnConfigs = null;
        if (nnconfigString != null) {
            nnConfigs = JsonUtil.convertnostrip(nnconfigString, NeuralNetConfigs.class);
        }
        if (conf.wantML() && !mergedCatMap.keySet().isEmpty()) {
            if (mergedCatMap.keySet().isEmpty()) {
                log.info("Merget set empty");
            }
            // add a null check
            int arrayLength = ((double[])mergedCatMap.values().iterator().next().get(0).getLeft()).length;
            for(Entry<String, List<Pair<Object, Double>>> entry : mergedCatMap.entrySet()) {
                double[] array = (double[]) entry.getValue().get(0).getLeft();
                if (array.length != arrayLength) {
                    log.info("Different lengths {} {}", arrayLength, array.length);
                }
            }
            Map<Double, String> labelMapShort = createLabelMapShort();
            MLMeta mlmeta = new MLMeta();
            mlmeta.dim1 = arrayLength;
            mlmeta.classify = true;
            mlmeta.features = true;
            boolean multi = neuralnetcommand.isMldynamic() || (neuralnetcommand.isMlclassify() && !neuralnetcommand.isMllearn());
            if (false /*multi*/ /*conf.wantMLMP()*/) {
                doLearnTestClassifyFuture(nnConfigs, conf, mergedCatMap, mapResult, arrayLength, labelMapShort, indicators, mlmeta, neuralnetcommand, threshold);
            } else {
                doLearnTestClassify(nnConfigs, conf, mergedCatMap, mapResult, arrayLength, labelMapShort, indicators, mlmeta, neuralnetcommand, threshold);
            }
            MLMeta mlmeta1 = new MLMeta();
            mlmeta1.dim1 = arrayLength;
            //mlmeta1.dim2 = arrayLength;
            mlmeta1.dim3 = arrayLength;
            mlmeta1.classify = true;
            mlmeta1.features = true;
            if (false /*multi*/ /*conf.wantMLMP()*/) {
                doLearnTestClassifyFuture(nnConfigs, conf, mergedCatArrayMap, mapResult, arrayLength, labelMapShort, indicators, mlmeta1, neuralnetcommand, threshold);
            } else {
                doLearnTestClassify(nnConfigs, conf, mergedCatArrayMap, mapResult, arrayLength, labelMapShort, indicators, mlmeta1, neuralnetcommand, threshold);
            }
        }
        // and others done with println
        handleOtherStats(conf, mergedCatMap, threshold);
        }
        createResultMap(conf, mapResult0);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        handleSpentTimes(conf);

    }

    private Double[] getThresholds(IclijConfig conf) {
        boolean gui = conf.getConfigData().getConfigValueMap().get(ConfigConstants.MISCTHRESHOLD) != null;
        log.info("GUI thresholds {}", gui);
        String thresholdString = conf.getAggregatorsIndicatorThreshold();
        if (gui) {
            thresholdString = conf.getThreshold();
        }
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Double[].class);
    }

    private void doLearnTestClassify(NeuralNetConfigs nnconfigs, IclijConfig conf, Map<String, List<Pair<Object, Double>>> mergedCatMap,
            Map<String, Map<String, Double[]>> mapResult, int arrayLength, Map<Double, String> labelMapShort,
            List<String> indicators, MLMeta mlmeta, NeuralNetCommand neuralnetcommand, Double threshold) {
        try {
            int testCount = 0;   
            // calculate sections and do ML
            /*
            log.info("Indicatormap keys {}", dayIndicatorMap.keySet());
            Map<String, Double[]> indicatorMap2 = dayIndicatorMap.get(conf.getAggregatorsIndicatorFuturedays());
            Map<String, Pair<Object, Double>> indicatorMap3 = new HashMap<>();
            if (indicatorMap2 != null) {
                for (Entry<String, Double[]> indicatorEntry : indicatorMap2.entrySet()) {
                    indicatorMap3.put(indicatorEntry.getKey(), new ImmutablePair(ArraysUtil.convert(indicatorEntry.getValue()), null));
                }
            }
            */
            for (MLClassifyDao mldao : mldaos) {
                if (mldao.getModels().size() != 1) {
                    log.error("Models size is {}", mldao.getModels().size());
                }
                for (MLClassifyModel model : mldao.getModels()) {          
                    if ((mlmeta.dim3 == null) == model.isFourDimensional()) {
                        continue;
                    }
                    List<LearnClassify> learnMap = transformLearnClassifyMap(mergedCatMap, true, mlmeta, model);
                    List<LearnClassify> classifyMap = transformLearnClassifyMap(mergedCatMap, false, mlmeta, model);
                    Map<Object, Long> countMap1 = learnMap.stream().collect(Collectors.groupingBy(e2 -> labelMapShort.get(e2.getClassification()), Collectors.counting()));                            
                    long count = countMap1.values().stream().distinct().count();
                    if (count == 1) {
                        log.info("Nothing to learn");
                        //testCount++;
                        //continue;
                    }
                    //Map<String, Pair<Object, Double>> classifyMap = indicatorMap3;
                    if (classifyMap == null || classifyMap.isEmpty()) {
                        log.error("map null ");
                        classifyMap = new ArrayList<>();
                        //testCount++;
                        //continue;
                    } else {
                        log.info("keyset {}", classifyMap.stream().map(e -> e.getId()).collect(Collectors.toList()));
                    }
                    SerialResultMeta resultMeta1 = new SerialResultMeta();
                    resultMeta1.setMlName(mldao.getName());
                    resultMeta1.setModelName(model.getName());
                    resultMeta1.setReturnSize(model.getReturnSize());
                    resultMeta1.setLearnMap(countMap1);
                    resultMeta1.setThreshold(threshold);
                    getResultMetas().add(resultMeta1);
                    log.info("len {}", arrayLength);
                    String filename = getFilename(mldao, model, "" + arrayLength, "" + cats, conf.getConfigData().getMarket(), indicators, threshold);
                    String path = model.getPath();
                    boolean mldynamic = conf.wantMLDynamic();
                    if (neuralnetcommand.isMlcross()) {
                        classifyMap = learnMap;
                    }
                    if (nnconfigs == null) {
                        String key = model.getKey();
                        nnconfigs = new NeuralNetConfigs();
                        String configValue = (String) conf.getValueOrDefault(key);
                        if (configValue != null) {
                        	Map<String, String> configMap = new NeuralNetConfigs().getConfigMapRev();
                        	String config = configMap.get(model.getKey());
                        	NeuralNetConfig nnconfig = nnconfigs.getAndSetConfig(config, configValue);
                        } else {
                            nnconfigs = null;
                        }
                    }
                    LearnTestClassifyResult result = mldao.learntestclassify(nnconfigs, this, learnMap, model, arrayLength, cats, mapTime, classifyMap, labelMapShort, path, filename, neuralnetcommand, mlmeta, true);  
                    if (result == null) {
                        continue;
                    }
                    Map<String, Double[]> classifyResult = result.getCatMap();
                    if (neuralnetcommand.isMlcross() && classifyResult != null && classifyMap.size() > 0) {
                        //Map<String, Double[]> classifyResult = result.getCatMap();
                        int cls = 0;
                        for (LearnClassify triple : classifyMap) {
                            String key = (String) triple.getId();
                            Object obj = triple.getClassification();
                            double cat;
                            if (obj instanceof Integer) {
                                cat = ((Integer) obj).doubleValue();
                            } else {
                                cat = (double) obj;
                            }
                            if (classifyResult == null) {
                                int jj = 0;
                            }
                            Double[] result0 = classifyResult.get(key);
                            if (result0 == null || result0[0] == null) {
                                int jj = 0;
                            }
                            if (cat == result0[0].doubleValue()) {
                                cls++;
                            }
                        }
                        result.setAccuracy((( double) cls) / classifyMap.size());
                    }
                    accuracyMap.put(mldao.getName() + model.getName() + threshold, result.getAccuracy());
                    lossMap.put(mldao.getName() + model.getName() + threshold, result.getLoss());
                    resultMeta1.setTestAccuracy(result.getAccuracy());
                    resultMeta1.setTrainAccuracy(result.getTrainaccuracy());
                    resultMeta1.setLoss(result.getLoss());
                    mapResult.put("" + model, classifyResult);
                    Map<String, Long> countMap = new HashMap<>();
                    if (classifyResult != null) {
                        IndicatorUtils.filterNonExistingClassifications(labelMapShort, classifyResult);
                        countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                    }
                    StringBuilder counts = new StringBuilder("classified ");
                    for (Entry<String, Long> countEntry : countMap.entrySet()) {
                        counts.append(countEntry.getKey() + " : " + countEntry.getValue() + " ");
                    }
                    addEventRow(counts.toString(), "", "");  
                    SerialResultMeta resultMeta = (SerialResultMeta) getResultMetas().get(testCount);
                    resultMeta.setClassifyMap(classifyResult);
                    resultMeta.setLearnMap(countMap);
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

    private void doLearnTestClassifyFuture(NeuralNetConfigs nnconfigs, IclijConfig conf, Map<String, List<Pair<Object, Double>>> mergedCatMap,
            Map<String, Map<String, Double[]>> mapResult, int arrayLength, Map<Double, String> labelMapShort,
            List<String> indicators, MLMeta mlmeta, NeuralNetCommand neuralnetcommand, Double threshold) {
        try {
            // calculate sections and do ML
            /*
            log.info("Indicatormap keys {}", dayIndicatorMap.keySet());
            Map<String, Double[]> indicatorMap2 = dayIndicatorMap.get(conf.getAggregatorsIndicatorFuturedays());
            Map<String, Pair<Object, Double>> indicatorMap3 = new HashMap<>();
            if (indicatorMap2 != null) {
                for (Entry<String, Double[]> indicatorEntry : indicatorMap2.entrySet()) {
                    indicatorMap3.put(indicatorEntry.getKey(), new ImmutablePair(ArraysUtil.convert(indicatorEntry.getValue()), null));
                }
            }
            */
            List<Future<LearnTestClassifyResult>> futureList = new ArrayList<>();
            Map<Future<LearnTestClassifyResult>, FutureMap> futureMap = new HashMap<>();
            for (MLClassifyDao mldao : mldaos) {
                if (mldao.getModels().size() != 1) {
                    log.error("Models size is {}", mldao.getModels().size());
                }
                for (MLClassifyModel model : mldao.getModels()) {          
                    if ((mlmeta.dim3 == null) == model.isFourDimensional()) {
                        continue;
                    }
                    List<LearnClassify> learnMap = transformLearnClassifyMap(mergedCatMap, true, mlmeta, model);
                    List<LearnClassify> classifyMap = transformLearnClassifyMap(mergedCatMap, false, mlmeta, model);
                    Map<Object, Long> countMap1 = learnMap.stream().collect(Collectors.groupingBy(e2 -> labelMapShort.get(e2.getClassification()), Collectors.counting()));                            
                    long count = countMap1.values().stream().distinct().count();
                    if (count == 1) {
                        log.info("Nothing to learn");
                        continue;
                    }
                    //Map<String, Pair<Object, Double>> classifyMap = indicatorMap3;
                    if (classifyMap == null || classifyMap.isEmpty()) {
                        log.error("map null ");
                        continue;
                    } else {
                        log.info("keyset {}", classifyMap.stream().map(e -> e.getId()).collect(Collectors.toList()));

                    }
                    SerialResultMeta resultMeta1 = new SerialResultMeta();
                    resultMeta1.setMlName(mldao.getName());
                    resultMeta1.setModelName(model.getName());
                    resultMeta1.setReturnSize(model.getReturnSize());
                    resultMeta1.setLearnMap(countMap1);
                    resultMeta1.setThreshold(threshold);
                    getResultMetas().add(resultMeta1);
                    log.info("len {}", arrayLength);
                    //LearnTestClassifyResult result = mldao.learntestclassify(this, map1, model, arrayLength, key, MYTITLE, 2, mapTime, map, labelMapShort);
                    boolean conv2d = true;
                    String filename = getFilename(mldao, model, "" + arrayLength, "" + cats, conf.getConfigData().getMarket(), indicators, threshold);
                    String path = model.getPath();
                    boolean mldynamic = conf.wantMLDynamic();
                    if (nnconfigs == null) {
                        String key = model.getKey();
                        nnconfigs = new NeuralNetConfigs();
                        String configValue = (String) conf.getValueOrDefault(key);                                
                        if (configValue != null) {
                            NeuralNetConfig nnconfig = nnconfigs.getAndSetConfig(key, configValue);
                        } else {
                            nnconfigs = null;
                        }
                    }
                    Callable callable = new MLClassifyLearnTestPredictCallable(nnconfigs, mldao, this, learnMap, model, arrayLength, cats, mapTime, classifyMap, labelMapShort, path, filename, neuralnetcommand, mlmeta);  
                    Future<LearnTestClassifyResult> future = MyExecutors.run(callable, 1);
                    futureList.add(future);
                    futureMap.put(future, new FutureMap(mldao, model, /* TODO resultMetaArray.size()*/ - 1));
                }
            }
            for (Future<LearnTestClassifyResult> future: futureList) {
                FutureMap futMap = futureMap.get(future);
                MLClassifyDao mldao = futMap.getDao();
                MLClassifyModel model = futMap.getModel();
                int testCount = futMap.getTestCount();
                LearnTestClassifyResult result = future.get();
                if (result == null) {
                    continue;
                }
                Map<String, Double[]> classifyResult = result.getCatMap();
                accuracyMap.put(mldao.getName() + model.getName() + threshold, result.getAccuracy());
                lossMap.put(mldao.getName() + model.getName() + threshold, result.getLoss());
                SerialResultMeta resultMeta = (SerialResultMeta) getResultMetas().get(testCount);
                resultMeta.setTestAccuracy(result.getAccuracy());
                resultMeta.setLoss(result.getLoss());
                //log.info("keys" + Arrays.deepToString(classifyResult.values().toArray()));
                //log.info("keys" + classifyResult.keySet());
                //log.info("ke2 " + classifyResult.values().stream().toString());
                mapResult.put("" + model, classifyResult);
                Map<String, Long> countMap = new HashMap<>();
                if (classifyResult != null) {
                    IndicatorUtils.filterNonExistingClassifications(labelMapShort, classifyResult);
                    countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                }
                StringBuilder counts = new StringBuilder("classified ");
                for (Entry<String, Long> countEntry : countMap.entrySet()) {
                    counts.append(countEntry.getKey() + " : " + countEntry.getValue() + " ");
                }
                addEventRow(counts.toString(), "", "");  
                resultMeta.setClassifyMap(classifyResult);
                resultMeta.setLearnMap(countMap);
            }
        } catch (Exception e1) {
            log.error("Exception", e1);
        }
    }

    private List<LearnClassify> transformLearnClassifyMap(Map<String, List<Pair<Object, Double>>> map, boolean classify, MLMeta mlmeta, MLClassifyModel model) {
        List<LearnClassify> mlMap = new ArrayList<>();
        for (Entry<String, List<Pair<Object, Double>>> entry : map.entrySet()) {
            List<Pair<Object, Double>> list = entry.getValue();
            for (Pair<Object, Double> pair : list) {
                Object array = pair.getLeft();
                Object newarray = model.transform(array, mlmeta);
                boolean classified = pair.getRight() != null;
                if (classified == classify) {
                    mlMap.add(new LearnClassify(entry.getKey(), newarray, pair.getRight()));
                }
            }
        }
        return mlMap;
    }

    private void getMergedLists(Set<String> ids, List<String> indicators, PipelineData[] datareaders) {
        Map<String, Object[]> result = new HashMap<>();
        PipelineData datareader = PipelineUtils.getPipeline(datareaders, this.key, inmemory);
        for (String id : ids) {
            Object[] arrayResult = new Object[0];
            for (String indicator : indicators) {
                String indicatorName = indicator;
                PipelineData indicatorResult = PipelineUtils.getPipeline(datareaders, indicatorName, inmemory);
                if (indicatorResult != null) {
                    Map<String, Double[][]> aListMap = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.LIST));
                    Double[][] aResult = aListMap.get(id);
                    arrayResult = ArrayUtils.addAll(arrayResult, aResult[0]);
                }
            }
            result.put(id, arrayResult);
        }
    }

    private void handleSpentTimes(IclijConfig conf) {
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

    private void handleOtherStats(IclijConfig conf, Map<String, List<Pair<Object, Double>>> mergedCatMap, Double threshold) {
        if (conf.wantOtherStats() && conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();            
            Map<Object, Long> countMap = getCountMap(mergedCatMap);
            //.values().stream().collect(Collectors.groupingBy(Pair::getRight, Collectors.counting()));
            StringBuilder counts = new StringBuilder();
            for (Entry<Object, Long> countEntry : countMap.entrySet()) {
                counts.append(labelMapShort.get(countEntry.getKey()) + " : " + countEntry.getValue() + " ");
            }
            addEventRow(counts.toString(), "", "");
        }
    }

    private Map<Object, Long> getCountMap(Map<String, List<Pair<Object, Double>>> mergedCatMap) {
        return mergedCatMap.values().stream().map(e -> e).flatMap(Collection::stream).filter(e -> e.getRight() != null).collect(Collectors.groupingBy(e -> e.getRight(), Collectors.counting()));
    }

    private void createResultMap(IclijConfig conf, Map<Double, Map<String, Map<String, Double[]>>> mapResult0) {
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[fieldSize];
            resultMap.put(id, fields);
            int retindex = 0 ;

            // make OO of this
            if (conf.wantML()) {
                Map<Double, String> labelMapShort2 = createLabelMapShort();
                Double[] thresholds = getThresholds(conf);
                for (Double threshold : thresholds) {
                Map<String, Map<String, Double[]>> mapResult = mapResult0.get(threshold);    
                for (MLClassifyDao mldao : mldaos) {
                    for (MLClassifyModel model : mldao.getModels()) {
                        Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                        mapResult2.put(MYTITLE, mapResult.get("" + model));
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
                            String type = null;
                            if (aType != null) {
                                type = labelMapShort2.get(aType[0]);
                                Double prob = aType[1];
                            }
                            fields[retindex++] = type;
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
    }

    @Deprecated
    private List<AbstractIndicator> getIndicators(PipelineData[] datareaders, Map<String, AbstractIndicator> newIndicatorMap, Map<String, AbstractIndicator> usedIndicatorMap,
            Map<String, List<AggregatorMLIndicator>> usedIndicators, Set<String> ids) throws Exception {
        Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
        Map<String, PipelineData> pipelineMap = PipelineUtils.getPipelineMap(datareaders);
        // TODO
        for (Entry<String, List<AggregatorMLIndicator>> entry : usedIndicators.entrySet()) {
            List<AggregatorMLIndicator> list = entry.getValue();
            for (AggregatorMLIndicator ind : list) {
                String indicator = ind.indicator();
                if (indicator != null) {
                    // TODO newIndicatorMap not used
                    AbstractIndicator pres = indicatorMap.put(indicator, ind.getIndicator(category, newIndicatorMap, usedIndicatorMap, datareaders, null, inmemory));
                    log.error("INDIC" + (pres != null));
                } else {
                    log.error("INDIC" + indicator);
                }
                PipelineData indicatorResult = PipelineUtils.getPipeline(datareaders, indicator);
                if (indicatorResult != null) {
                    PipelineData datareader = pipelineMap.get(this.key);
                    Map<String, Double[][]> aResult = (Map<String, Double[][]>) datareader.get(PipelineConstants.LIST);
                    //Map<String, Object[]> aResult = (Map<String, Object[]>) indicatorResult.get(PipelineConstants. LIST);
                    ids.retainAll(aResult.keySet());
                }
            }
        }
        return new ArrayList<>(indicatorMap.values());
    }

    private List<String> getIndicators(PipelineData[] datareaders, Map<String, List<AggregatorMLIndicator>> usedIndicators,
            Set<String> ids, Inmemory inmemory) throws Exception {
        List<String> indicators = new ArrayList<>();
        PipelineData datareader = PipelineUtils.getPipeline(datareaders, this.key, inmemory);
        // TODO
        for (Entry<String, List<AggregatorMLIndicator>> entry : usedIndicators.entrySet()) {
            List<AggregatorMLIndicator> list = entry.getValue();
            for (AggregatorMLIndicator ind : list) {
                String indicator = ind.indicator();
                PipelineData indicatorResult = PipelineUtils.getPipeline(datareaders, indicator, inmemory);
                if (indicatorResult != null) {
                    indicators.add(indicator);
                    Map<String, Double[][]> aResult = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.LIST));
                    //Map<String, Object[]> aResult = (Map<String, Object[]>) indicatorResult.get(PipelineConstants. LIST);
                    ids.retainAll(aResult.keySet());
                } else {
                    log.error("Indicator missing: {}", indicator);
                }
            }
        }
        return indicators;
    }

    private boolean anythingHereA(Map<String, Double[][]> listMap2) {
        if (listMap2 == null) {
            return false;
        }
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

    protected boolean anythingHere(Map<String, List<List<Double>>> listMap2) {
        if (listMap2 == null) {
            return false;
        }
        for (List<List<Double>> array : listMap2.values()) {
            for (int i = 0; i < array.get(0).size(); i++) {
                if (array.get(0).get(i) != null) {
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
	if (cats == 2) {
	    Map<Double, String> labelMap1 = new HashMap<>();
	    labelMap1.put(1.0, Constants.ABOVE);
	    labelMap1.put(2.0, Constants.BELOW);
	    return labelMap1;
	}
        Map<Double, String> labelMap1 = new HashMap<>();
        int halfcat = cats / 2;
        for (int i = 1; i <= halfcat; i++) {
            labelMap1.put((double) (halfcat + 1 - i), Constants.ABOVE + i);
            labelMap1.put((double) (halfcat + i), Constants.BELOW + i);
        }
        return labelMap1;
    }

    @Override
    public Object calculate(double[][] array) {
        Ta tu = new TalibMACD();
        return tu.calculate(array);
    }

    @Override
    public boolean isEnabled() {
        if (!conf.wantAggregatorsIndicatorMACD() && !conf.wantAggregatorsIndicatorRSI() && !conf.wantAggregatorsIndicatorATR() && !conf.wantAggregatorsIndicatorCCI() && !conf.wantAggregatorsIndicatorSTOCH() && !conf.wantAggregatorsIndicatorSTOCHRSI()) {
            return false;
        }
        return conf.wantAggregatorsIndicatorML();
    }

    @Override
    public Object[] getResultItem(StockDTO stock) {
        String market = conf.getConfigData().getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new ImmutablePair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
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
        objs[retindex++] = title + Constants.WEBBR + "macd";
        if (conf.isMACDDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        objs[retindex++] = title + Constants.WEBBR + "sig";
        if (conf.isMACDSignalDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        Double[] thresholds = getThresholds(conf);
        for (Double threshold : thresholds) {
        retindex = getTitles(retindex, objs, threshold);
        }
        log.info("fieldsizet {}", retindex);
        return objs;
    }

    private int fieldSize() {
        int size = 0;
        for (MLClassifyDao mldao : mldaos) {
            size += mldao.getSizes(this);
        }
        Double[] thresholds = getThresholds(conf);
        size = size * thresholds.length;
        emptyField = new Object[size];
        return size;
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockDTO stock) {
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
        Double[] thresholds = getThresholds(conf);
        for (Double threshold : thresholds) {
        retindex = getTitles(retindex, objs, threshold);
        }
        headrow.addarr(objs);
    }

    private int getTitles2(int retindex, Object[] objs) {
        for (MLClassifyDao mldao : mldaos) {
            //retindex = mldao.addTitles(objs, retindex, this, title, key, MYTITLE);
        }
        return retindex;
    }

    private int getTitles(int retindex, Object[] objs, Double threshold) {
        // make OO of this
        for (MLClassifyDao mldao : mldaos) {
            for (MLClassifyModel model : mldao.getModels()) {
                List<Integer> typeList = getTypeList();
                for (int mapTypeInt : typeList) {
                    String mapType = mapTypes.get(mapTypeInt);
                    String val = "";
                    // workaround
                    try {
                        val = "" + MLClassifyModel.roundme((Double) accuracyMap.get(mldao.getName() + model.getId() + threshold));
                    } catch (Exception e) {
                        log.error("Exception fix later, refactor", e);
                    }
                    objs[retindex++] = title + " " + "mlind" + " " + threshold + Constants.WEBBR +  mldao.getShortName() + " " + model.getShortName() + mapType + " " + val;
                    if (model.getReturnSize() > 1) {
                        objs[retindex++] = title + " " + "mlind" + " " + threshold + Constants.WEBBR +  mldao.getShortName() + " " + model.getShortName() + mapType + " prob ";
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
    
    public String getFilenamePart(List<String> indicators) {
        String ret = "";
        for (String indicatorName : indicators) {
            AbstractIndicator indicator = IndicatorUtils.dummyfactory(conf, indicatorName);
            ret = ret + indicator.getName() + "_";
            if (indicator.wantForExtras()) {
                ret = ret + "d" + "_";
            }
        }
        return ret;
    }
    
    //@Override
    public String getFilenamePartNot() {
        String ret = "";
        if (conf.wantAggregatorsIndicatorMACD()) {
            ret = ret + Constants.MACD + " ";
        }
        if (conf.wantAggregatorsIndicatorRSI()) { 
            ret = ret + Constants.RSI + " ";
        } 
        if (conf.wantAggregatorsIndicatorATR()) { 
            ret = ret + Constants.ATR + " ";
        } 
        if (conf.wantAggregatorsIndicatorCCI()) { 
            ret = ret + Constants.CCI + " ";
        } 
        if (conf.wantAggregatorsIndicatorSTOCH()) {
            ret = ret + Constants.STOCH + " " ;
        }
        if (conf.wantAggregatorsIndicatorSTOCHRSI()) {
            ret = ret + Constants.STOCHRSI + " " ;
        }
        //ret;
        return ret;
    }
    
    public String getFilename(MLClassifyDao dao, MLClassifyModel model, String in, String out, String market, List<String> indicators, Double threshold) {
        String testmarket = conf.getConfigData().getMlmarket();
        if (testmarket != null) {
            market = testmarket;
        }
        return market + "_" + getName() + "_" + dao.getName() + "_" +  model.getName() + "_" + getFilenamePart(indicators) + conf.getAggregatorsIndicatorFuturedays() + "_" + threshold + "_" + in + "_" + out;
    }
    
    private double getCat(double change, double threshold) {
        int halfcat = cats / 2;
        for (double cat = cats; cat > 1; cat--) {
            if (change > threshold + interval * (cat - 1 - halfcat)) {
                return cats + 1 - cat;
            }
        }
        return cats;
    }
}


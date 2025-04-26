package roart.aggregator.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.time.LocalDate;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregator.impl.IndicatorAggregator.AfterBeforeLimit;
import roart.aggregator.impl.IndicatorAggregator.SubType;
import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialIncDec;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.pipeline.data.SerialResultMeta;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.data.TwoDimd;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.MiscUtil;
import roart.common.util.TimeUtil;
import roart.executor.MyExecutors;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyDS;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnClassify;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.pytorch.MLClassifyPytorchModel;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.stockutil.StockUtil;
import roart.talib.util.TaConstants;

public abstract class IndicatorAggregator extends Aggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final int CMNTYPE = 0;
    protected static final int NEGTYPE = 1;
    protected static final int POSTYPE = 2;
    protected static final String CMNTYPESTR = "cmn";

    // when the curve is in the negative below area, and goes above
    
    protected static final String NEGTYPESTR = "neg";
    
    // when the curve is in the positive above area, and goes below
    
    protected static final String POSTYPESTR = "pos";

    static String labelTP = "TruePositive";
    static String labelFP = "FalsePositive";
    static String labelTN = "TrueNegative";
    static String labelFN = "FalseNegative";

    private static final String FRESH = "fresh";
    
    protected enum MySubType { MACDHIST, MACDSIG, MACDMACD, RSI, STOCHRSI, STOCH, ATR, CCI }

    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;

    protected Map<String, double[][]> listMap;

    protected int fieldSize = 0;

    protected Object[] emptyField;

    protected String key;

    private Map<String, String> idNameMap;

    protected PipelineData[] datareaders;

    private Map<MLClassifyModel, Long> mapTime = new HashMap<>();

    private List<ResultItemTableRow> mlTimesTableRows = null;
    private List<ResultItemTableRow> eventTableRows = null;

    protected Map<Integer, String> mapTypes = new HashMap<>();

    protected List<MLClassifyDao> mldaos = new ArrayList<>();

    protected List<SubType> wantedSubTypes = new ArrayList<>();

    protected List<String> stockDates;
    
    public IndicatorAggregator(IclijConfig conf, String string, int category, String title, Map<String, String> idNameMap, PipelineData[] datareaders, NeuralNetCommand neuralnetcommand, List<String> stockDates, Inmemory inmemory) throws Exception {
        super(conf, string, category, inmemory);
        this.key = title;
        this.idNameMap = idNameMap;
        this.datareaders = datareaders;
        this.stockDates = stockDates;

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
        if (conf.wantMLTimes()) {
            mlTimesTableRows = new ArrayList<>();
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        if (isEnabled()) {
            calculateMe(conf, datareaders, neuralnetcommand);    
            cleanMLDaos();
        }
    }

    protected abstract String getNeuralNetConfig();

    protected abstract AfterBeforeLimit getAfterBefore();

    private void calculateMe(IclijConfig conf,
            PipelineData[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        log.info("checkthis {}", key.equals(title));
        PipelineData datareader  = PipelineUtils.getPipeline(datareaders, key, inmemory);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        Map<String, Double[][]> aListMap = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.LIST));
        Map<String, double[][]> fillListMap = PipelineUtils.sconvertMapdd(datareader.get(PipelineConstants.TRUNCFILLLIST));
        Map<String, double[][]>  base100FillListMap = PipelineUtils.sconvertMapdd(datareader.get(PipelineConstants.TRUNCBASE100FILLLIST)) ;
        // TODO
        this.listMap = /*conf.wantPercentizedPriceIndex() ? base100FillListMap :*/ fillListMap;

        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        if (!anythingHereA(aListMap)) {
            log.debug("empty {}", key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        otherResultMap = new HashMap<>();
        objectMap = new HashMap<>();
        accuracyMap = new HashMap<>();
        lossMap = new HashMap<>();
        long time2 = System.currentTimeMillis();
        AfterBeforeLimit afterbefore = getAfterBefore();
        makeWantedSubTypes(afterbefore);
        //makeWantedSubTypesMap(wantedSubTypes());
        //log.debug("imap " + objectMap.size());
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        log.debug("listmap {} {}", listMap.size(), listMap.keySet());

        //Map<Double, Pair> thresholdMap = new HashMap<>();
        Map<Double, Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>>> mapResult0 = new HashMap<>();
        Double[] thresholds = getThresholds();
        for (Double threshold : thresholds) {
        
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<SubType, MLMeta> metaMap = new HashMap<>();
        Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap = createPosNegMaps(conf, metaMap, threshold);
        doMergeLearn(conf, mapMap, afterbefore, metaMap, threshold);
        
        usedSubTypes = new ArrayList<>(mapMap.keySet());
        fieldSize = fieldSize() * thresholds.length;
        // map from h/m to model to posnegcom map<model, results>
        Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        mapResult0.put(threshold, mapResult);
        log.debug("Period {} {}", title, mapMap.keySet());
        String nnconfigString = getNeuralNetConfig();
        NeuralNetConfigs nnConfigs = null;
        if (nnconfigString != null) {
            nnConfigs = JsonUtil.convertnostrip(nnconfigString, NeuralNetConfigs.class);
        }
        if (conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();
            boolean multi = neuralnetcommand.isMldynamic() || (neuralnetcommand.isMlclassify() && !neuralnetcommand.isMllearn());
            if (false /*multi*/ /*conf.wantMLMP()*/) {
                doLearnTestClassifyFuture(nnConfigs, conf, mapMap, mapResult, labelMapShort, metaMap, neuralnetcommand, threshold);
            } else {
                doLearnTestClassify(nnConfigs, conf, mapMap, mapResult, labelMapShort, metaMap, neuralnetcommand, threshold);
            }
        }        
        handleOtherStats(conf, mapMap);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        createResultMap(conf, mapResult0, mapMap);
        }
        handleSpentTime(conf);

    }

    private Double[] getThresholds() {
        boolean gui = conf.getConfigData().getConfigValueMap().get(ConfigConstants.MISCTHRESHOLD) != null;
        log.debug("GUI thresholds {}", gui);
        String thresholdString = getAggregatorsThreshold();
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

    protected boolean anythingHereA(Map<String, Double[][]> listMap2) {
        if (listMap2 == null) {
            return false;
        }
        for (Double[][] array : listMap2.values()) {
            for (int i = 0; i < array[0].length; i++) {
                if (array[0][i] != null) {
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

    protected boolean anythingHere3(Map<String, List<List<Double>>> listMap2) {
        if (listMap2 == null) {
            return false;
        }
        for (List<List<Double>> array : listMap2.values()) {
            if (array.size() != Constants.OHLC) {
                return false;
            }
            out:
            for (int i = 0; i < array.get(0).size(); i++) {
                for (int j = 0; j < array.size() - 1; j++) {
                    if (array.get(j).get(i) == null) {
                        continue out;
                    }
                }
                return true;
            }
        }
        return false;
    }

    protected boolean anythingHere3A(Map<String, Double[][]> listMap2) {
        if (listMap2 == null) {
            return false;
        }
        for (Double[][] array : listMap2.values()) {
            if (array.length != Constants.OHLC) {
                return false;
            }
            out:
            for (int i = 0; i < array[0].length; i++) {
                for (int j = 0; j < array.length - 1; j++) {
                    if (array[j][i] == null) {
                        continue out;
                    }
                }
                return true;
            }
        }
        return false;
    }

    private boolean anythingHereNot(Map<String, Double[][]> listMap) {
        if (listMap == null) {
            return false;
        }
        for (Double[][] array : listMap.values()) {
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

    private boolean anythingHere2(Map<String, Double[]> listMap) {
        if (listMap == null) {
            return false;
        }
        for (Double[] array : listMap.values()) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        int retindex = 0;
        Object[] objs = new Object[fieldSize];
        Double[] thresholds = getThresholds();
        for (Double threshold : thresholds) {
        retindex = getTitles(retindex, objs, threshold);
        }
        headrow.addarr(objs);
    }

    private void doLearnTestClassify(NeuralNetConfigs nnConfigs, IclijConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort, Map<SubType, MLMeta> metaMap, NeuralNetCommand neuralnetcommand, Double threshold) {
        List<SubType> subTypes = usedSubTypes();
        AfterBeforeLimit afterbefore = getAfterBefore();
        // map from h/m + posnegcom to map<model, results>
        try {
            for (SubType subType : subTypes) {
                if (!subType.useDirectly) {
                    continue;
                }
                MLMeta mlmeta = metaMap.get(subType);
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
                for (MLClassifyDao mldao : mldaos) {
                    if (mldao.getModels().size() != 1) {
                        log.error("Models size is {}", mldao.getModels().size());
                    }
                    // map from posnegcom to map<id, result>
                    Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                    for (MLClassifyModel model : mldao.getModels()) {
                        for (int mapTypeInt : getMapTypeList()) {
                            SerialResultMeta resultMeta = new SerialResultMeta();
                            resultMeta.setReturnSize(model.getReturnSize());
                            getResultMetas().add(resultMeta);
                            if (model.isPredictorOnly()) {
                                continue;
                            }
                            if (!isBinary(mapTypeInt) && model.isBinary()) {
                                continue;
                            }
                            if (mlmeta.dim3 == null && model.isFourDimensional()) {
                                continue;
                            }
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            Map<String, List<Pair<double[], Pair<Object, Double>>>> offsetMap = mapMap.get(subType).get("offset");
                            Map<String, List<Pair<double[], Pair<Object, Double>>>> learnMap = mapMap.get(subType).get(mapType);
                            Map<String, List<Pair<double[], Pair<Object, Double>>>> classifyMap = mapMap.get(subType).get(FRESH + mapType);
                            log.debug("map name {}", mapName);
                            if (learnMap == null || learnMap.isEmpty() || classifyMap == null || classifyMap.isEmpty()) {
                                log.error("map null and continue? {}", mapName);
                                if (learnMap == null) {
                                    learnMap = new HashMap<>();
                                }
                                if (classifyMap == null) {
                                    classifyMap = new HashMap<>();
                                }
                                //continue;
                            }
			    IndicatorUtils.filterNonExistingClassifications3(labelMapShort, learnMap);
                            
                            Map<String, Long> countMap = getCountMap(labelMapShort, learnMap);
                            long count = countMap.values().stream().distinct().count();
                            if (count == 1) {
                                log.info("Nothing to learn");
                                //continue;
                            }
                            //countMap = learnMap.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e.getRight().getRight()), Collectors.counting()));                            

                            int outcomes; // = (int) map.values().stream().distinct().count();
                            outcomes = 4;
                            log.debug("Outcomes {}", outcomes);
                            int size0 = getValidateSize2(learnMap, mlmeta);
                            List<LearnClassify> learnMLMap = transformLearnClassifyMap(learnMap, true, mlmeta, model);
                            List<LearnClassify> classifyMLMap = transformLearnClassifyMap(classifyMap, false, mlmeta, model);
                            int size = getValidateSize(learnMLMap, mlmeta);
                            List<AbstractIndicator> indicators = new ArrayList<>();
                            String filename = getFilename(mldao, model, "" + size, "" + outcomes, conf.getConfigData().getMarket(), indicators, subType.getType(), mapType, mlmeta, threshold);
                            String path = model.getPath();
                            boolean mldynamic = conf.wantMLDynamic();
                            //indicators.add(this);
                            log.info("Filename {}", filename);
                            if (neuralnetcommand.isMlcross()) {
                                classifyMLMap = learnMLMap;
                            }
                            if (nnConfigs == null) {
                                String key = model.getKey();
                                nnConfigs = new NeuralNetConfigs();
                                String configValue = (String) conf.getValueOrDefault(key);                                
                                if (configValue != null) {
                                    Map<String, String> configMap = new NeuralNetConfigs().getConfigMapRev();
                                    String config = configMap.get(model.getKey());
                                    NeuralNetConfig nnconfig = nnConfigs.getAndSetConfig(config, configValue);
                                } else {
                                    nnConfigs = null;
                                }
                            }
                            LearnTestClassifyResult result = mldao.learntestclassify(nnConfigs, this, learnMLMap, model, size, outcomes, mapTime, classifyMLMap, labelMapShort, path, filename, neuralnetcommand, mlmeta, true);  
                            if (result == null) {
                                continue;
                            }
                            resultMeta.setMlName(mldao.getName());
                            resultMeta.setModelName(model.getName());
                            resultMeta.setSubType(subType.getType() + mergeTxt(subType));
                            resultMeta.setSubSubType(mapType);
                            resultMeta.setLearnMap(countMap);
                            resultMeta.setThreshold(threshold);
                            if (neuralnetcommand.isMlcross() && result.getCatMap() != null && classifyMLMap.size() > 0) {
                                Map<String, Double[]> classifyResult = result.getCatMap();
                                int cls = 0;
                                for (LearnClassify triple : classifyMLMap) {
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
                                result.setAccuracy((( double) cls) / classifyMLMap.size());
                            }
                            accuracyMap.put(mldao.getName() + model.getName() + subType.getType() + mapType, result.getAccuracy());
                            lossMap.put(mldao.getName() + model.getName(), result.getLoss());
                            resultMeta.setTestAccuracy(result.getAccuracy());
                            resultMeta.setTrainAccuracy(result.getTrainaccuracy());
                            resultMeta.setLoss(result.getLoss());

                            Map<String, Double[]> classifyResult = result.getCatMap();
                            mapResult2.put(mapType, classifyResult);

                            Map<String, Long> countMap2 = null;
                            if (classifyResult != null && !classifyResult.isEmpty()) {
                                countMap2 = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                            }
                            if (countMap2 == null) {
                                log.info("No classified result");
                            }

                            if (countMap2 != null) {
                                addEventRow(subType, countMap2);
                            }
                            if (countMap2 != null && !isBinary(mapTypeInt)) {
                                Map<String, List<Pair<double[], Pair<Object, Double>>>> posMap = mapMap.get(subType).get(POSTYPESTR);
                                if (posMap != null) {
                                    Set<String> posIds = posMap.keySet();
                                    Map<String, Double[]> pos = classifyResult.entrySet().stream().filter(map -> posIds.contains(map.getKey())).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue())); 
                                    log.info("Pos {}", pos.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting())));
                                }
                                Map<String, List<Pair<double[], Pair<Object, Double>>>> negMap = mapMap.get(subType).get(NEGTYPESTR);
                                if (negMap != null) {
                                    Set<String> negIds = negMap.keySet();
                                    Map<String, Double[]> neg = classifyResult.entrySet().stream().filter(map -> negIds.contains(map.getKey())).collect(Collectors.toMap(map -> map.getKey(), map -> map.getValue())); 
                                    log.info("Neg {}", neg.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting())));
                                }
                            }
                            
                            log.info("Nowcount {}", getResultMetas().size());
                            handleResultMeta(getResultMetas().size() - 1, offsetMap, countMap, classifyResult);
                            //testCount++;
                        }
                        mapResult1.put(model, mapResult2);
                    }
                }
                mapResult.put(subType, mapResult1);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private Map<String, Long> getCountMap(Map<Double, String> labelMapShort,
            Map<String, List<Pair<double[], Pair<Object, Double>>>> learnMap) {
        Map<String, Long> countMap;
        countMap = learnMap.values().stream().map(e -> e).flatMap(Collection::stream).collect(Collectors.groupingBy(e -> labelMapShort.get(e.getRight().getRight()), Collectors.counting()));
        return countMap;
    }

    private List<LearnClassify> transformLearnClassifyMap(Map<String, List<Pair<double[], Pair<Object, Double>>>> learnMap, boolean classify, MLMeta mlmeta, MLClassifyModel model) {
        List<LearnClassify> mlMap = new ArrayList<>();
        for (Entry<String, List<Pair<double[], Pair<Object, Double>>>> entry : learnMap.entrySet()) {
            List<Pair<double[], Pair<Object, Double>>> list = entry.getValue();
            for (Pair<double[], Pair<Object, Double>> pair : list) {
                Pair<Object, Double> arrayclassify = pair.getRight();
                Object array = arrayclassify.getLeft();
                Object newarray = model.transform(array, mlmeta);
                boolean classified = arrayclassify.getRight() != null;
                if (classified == classify) {
                    mlMap.add(new LearnClassify(entry.getKey(), newarray, arrayclassify.getRight()));
                }
            }
        }
        return mlMap;
    }

    private void doLearnTestClassifyFuture(NeuralNetConfigs nnConfigs, IclijConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort, Map<SubType, MLMeta> metaMap, NeuralNetCommand neuralnetcommand, Double threshold) {
        List<SubType> subTypes = usedSubTypes();
        AfterBeforeLimit afterbefore = getAfterBefore();
        // map from h/m + posnegcom to map<model, results>
        List<Future<LearnTestClassifyResult>> futureList = new ArrayList<>();
        Map<Future<LearnTestClassifyResult>, FutureMap> futureMap = new HashMap<>();
        int testCount = 0;
        try {
            for (SubType subType : subTypes) {
                if (!subType.useDirectly) {
                    continue;
                }
                MLMeta mlmeta = metaMap.get(subType);
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                if (mapResult1 == null) {
                    mapResult1 = new HashMap<>();
                    mapResult.put(subType, mapResult1);
                }
                for (MLClassifyDao mldao : mldaos) {
                    if (mldao.getModels().size() != 1) {
                        log.error("Models size is {}", mldao.getModels().size());
                    }
                    // map from posnegcom to map<id, result>
                    for (MLClassifyModel model : mldao.getModels()) {
                        if (model.isPredictorOnly()) {
                            continue;
                        }
                        Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                        if (mapResult2 == null) {
                            mapResult2 = new HashMap<>();
                            mapResult1.put(model, mapResult2);
                        }
                        for (int mapTypeInt : getMapTypeList()) {
                            if (!isBinary(mapTypeInt) && model.isBinary()) {
                                continue;
                            }
                            if (mlmeta.dim3 == null && model.isFourDimensional()) {
                                continue;
                            }
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            Map<String, List<Pair<double[], Pair<Object, Double>>>> learnMap = mapMap.get(subType).get(mapType);
                            Map<String, List<Pair<double[], Pair<Object, Double>>>> classifyMap = mapMap.get(subType).get(FRESH + mapType);
                            log.debug("map name {}", mapName);
                            if (learnMap == null || learnMap.isEmpty() || classifyMap == null || classifyMap.isEmpty()) {
                                log.warn("Map null and continue? {}", mapName);
                                continue;
                            }
			    IndicatorUtils.filterNonExistingClassifications3(labelMapShort, learnMap);
			    Map<String, Long> countMap = getCountMap(labelMapShort, learnMap);
                            long count = countMap.values().stream().distinct().count();
                            if (count == 1) {
                                log.info("Nothing to learn");
                                continue;
                            }
                            int outcomes = (int) learnMap.values().stream().distinct().count();
                            outcomes = 4;
                            log.debug("Outcomes {}", outcomes);
                            int size0 = getValidateSize2(learnMap, mlmeta);
                            List<LearnClassify> learnMLMap = transformLearnClassifyMap(learnMap, true, mlmeta, model);
                            List<LearnClassify> classifyMLMap = transformLearnClassifyMap(classifyMap, false, mlmeta, model);
                            int size = getValidateSize(learnMLMap, mlmeta);
                            List<AbstractIndicator> indicators = new ArrayList<>();
                            String filename = getFilename(mldao, model, "" + size, "" + outcomes, conf.getConfigData().getMarket(), indicators, subType.getType(), mapType, mlmeta, threshold);
                            String path = model.getPath();
                            boolean mldynamic = conf.wantMLDynamic();
                            if (nnConfigs == null) {
                                String key = model.getKey();
                                nnConfigs = new NeuralNetConfigs();
                                String configValue = (String) conf.getValueOrDefault(key);
                                if (configValue != null) {
                                    NeuralNetConfig nnconfig = nnConfigs.getAndSetConfig(key, configValue);
                                } else {
                                    nnConfigs = null;
                                }
                            }
                            Callable callable = new MLClassifyLearnTestPredictCallable(nnConfigs, mldao, this, learnMLMap, model, size, outcomes, mapTime, classifyMLMap, labelMapShort, path, filename, neuralnetcommand, mlmeta);  
                            Future<LearnTestClassifyResult> future = MyExecutors.run(callable, 1);
                            futureList.add(future);
                            futureMap.put(future, new FutureMap(subType, mldao, model, mapType, mapMap, countMap));
                        }
                    }
                }
            }
            for (Future<LearnTestClassifyResult> future: futureList) {
                FutureMap futMap = futureMap.get(future);
                SubType subType = futMap.getSubType();
                MLClassifyDao mldao = futMap.getDao();
                MLClassifyModel model = futMap.getModel();
                String mapType = futMap.getMapType();
                Map<String, Long> countMap = futMap.getCountMap();
                LearnTestClassifyResult result = future.get();
                if (result == null) {
                    continue;
                }
		SerialResultMeta resultMeta = new SerialResultMeta();
		resultMeta.setMlName(mldao.getName());
		resultMeta.setModelName(model.getName());
		resultMeta.setReturnSize(model.getReturnSize());
		resultMeta.setSubType(subType.getType() + mergeTxt(subType));
		resultMeta.setSubSubType(mapType);
		resultMeta.setLearnMap(countMap);
		resultMeta.setThreshold(threshold);
		getResultMetas().add(resultMeta);
                            
                Map<String, Double[]> classifyResult = result.getCatMap();
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                mapResult2.put(mapType, classifyResult);
                Map<String, Long> countMap2 = null;
                if (classifyResult != null && !classifyResult.isEmpty()) {
                    countMap2 = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                }
                if (countMap2 == null) {
                    log.info("No classified result");
                }

                accuracyMap.put(mldao.getName() + model.getName() + subType.getType() + mapType, result.getAccuracy());
                lossMap.put(mldao.getName() + model.getName(), result.getLoss());

                if (countMap2 != null) {
                    addEventRow(subType, countMap2);
                }
                Map<String, List<Pair<double[], Pair<Object, Double>>>> offsetMap = mapMap.get(subType).get("offset");
                handleResultMeta(testCount, offsetMap, countMap2, classifyResult);
		handleResultMetaAccuracy(testCount, result);
                testCount++;
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private int getValidateSize(List<LearnClassify> list, MLMeta mlmeta) {
        int size = -1;
        for (LearnClassify entry : list) {
            Object myarray = entry.getArray();
            int dim = myarray.getClass().getName().indexOf("D");
            if (dim == 1) {
                double[] myvalue = (double[]) entry.getArray();
                if (size < 0) {
                    size = myvalue.length;
                }
                if (size != myvalue.length) {
                    return -1;
                }
            } else {
                double[][] myvalue = (double[][]) entry.getArray();
                for (int j = 0; j < myvalue.length; j++) {
                    if (size < 0) {
                        size = myvalue[j].length;
                    }
                    if (size != myvalue[j].length) {
                        return -1;
                    }
                }
            }
        }
        return size;
    }

    private int getValidateSize2(Map<String, List<Pair<double[], Pair<Object, Double>>>> learnMap, MLMeta mlmeta) {
        int size = -1;
        List<LearnClassify> mlMap = new ArrayList<>();
        for (Entry<String, List<Pair<double[], Pair<Object, Double>>>> entry : learnMap.entrySet()) {
            List<Pair<double[], Pair<Object, Double>>> list = entry.getValue();
            for (Pair<double[], Pair<Object, Double>> pair : list) {
                Pair<Object, Double> value = pair.getRight();
                if (mlmeta.dim2 == null) {
                    double[] myvalue = (double[]) value.getLeft();
                    if (size < 0) {
                        size = myvalue.length;
                    }
                    if (size != myvalue.length) {
                        return -1;
                    }
                } else {
                    double[][] myvalue = (double[][]) value.getLeft();
                    for (int j = 0; j < mlmeta.dim2; j++) {
                        if (size < 0) {
                            size = myvalue[j].length;
                        }
                        if (size != myvalue[j].length) {
                            return -1;
                        }
                    }
                }
             }
        }
        return size;
    }

    private void handleResultMetaAccuracy(int testCount, LearnTestClassifyResult result) {
        SerialResultMeta resultMeta = (SerialResultMeta) getResultMetas().get(testCount);
        resultMeta.setTestAccuracy(result.getAccuracy());
        resultMeta.setTrainAccuracy(result.getTrainaccuracy());
        resultMeta.setLoss(result.getLoss());
    }

    private void createResultMap(IclijConfig conf,
            Map<Double, Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>>> mapResult0, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap) {
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[fieldSize];
            resultMap.put(id, fields);
            int retindex = 0; //tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);

            // make OO of this
            if (conf.wantML()) {
                Map<Double, String> labelMapShort2 = createLabelMapShort();
                //int momidx = 6;
                Double[] thresholds = getThresholds();
                for (Double threshold : thresholds) {
                Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult = mapResult0.get(threshold);
                List<SubType> subTypes2 = usedSubTypes();      
                for (SubType subType : subTypes2) {
                    Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                    if (mapResult1 == null) {
                        int jj = 0;
                        continue;
                    }
                    //log.debug("mapget " + subType + " " + mapResult.keySet());
                    for (MLClassifyDao mldao : mldaos) {
                        for (MLClassifyModel model : mldao.getModels()) {
                            Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                            //for (int mapTypeInt : getMapTypeList()) {
                            //String mapType = mapTypes.get(mapTypeInt);
                            //Map<String, Double[]> mapResult3 = mapResult2.get(mapType);
                            //String mapName = subType.getType() + mapType;
                            //log.debug("fields " + fields.length + " " + retindex);
                            List<Integer> typeList = getTypeList();
                            Map<Integer, String> mapTypes = getMapTypes();
                            for (int mapTypeInt : typeList) {
                                String mapType = mapTypes.get(mapTypeInt);
                                if (mapResult2 == null) {
                                    continue;
                                }
                                Map<String, Double[]> resultMap1 = mapResult2.get(mapType);
                                Double[] aType = null;
                                if (resultMap1 != null) {
                                    aType = resultMap1.get(id);
                                } else {
                                    int jj = 0;
                                    //log.info("map null {}", mapType);
                                }
                                int modelSize = model.getSizes(this);
                                if (retindex > 28) {
                                    int jj = 0;
                                }
                                String type = null;
                                fields[retindex++] = aType != null ? labelMapShort2.get(aType[0]) : null;
                                if (model.getReturnSize() > 1) {
                                    fields[retindex++] = aType != null ? aType[1] : null;
                                } else {
                                    int jj = 0;
                                }
                                //retindex = mldao.addResults(fields, retindex, id, model, this, mapResult2, labelMapShort2);
                                //log.debug("sizej "+retindex);
                            }
                            //}
                        }   
                    }
                }
                }
            }
            //log.debug("ri" + retindex);
            if (retindex != fieldSize) {
                log.error("Field size too small {} < {}", retindex, fieldSize);
            }
        }
    } 

    private void doClassifications(IclijConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult) {
        AfterBeforeLimit afterbefore = getAfterBefore();
        // map from h/m + posnegcom to map<model, results>
        Map<Double, String> labelMapShort = createLabelMapShort();
        List<SubType> subTypes = usedSubTypes();
        int testCount = 0;
        for (SubType subType : subTypes) {
            if (!subType.useDirectly) {
                continue;
            }
            Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
            for (MLClassifyDao mldao : mldaos) {
                // map from posnegcom to map<id, result>
                Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                for (MLClassifyModel model : mldao.getModels()) {
                    for (int mapTypeInt : getMapTypeList()) {
                        String mapType = mapTypes.get(mapTypeInt);
                        Map<String, List<Pair<double[], Pair<Object, Double>>>> offsetMap = mapMap.get(subType).get("offset");
                        Map<String, Double[]> classifyResult = doClassifications(conf, mapMap, labelMapShort,
                                subType, mldao, mapResult2, model, mapTypeInt);                        
                        Map<String, Long> countMap = null;
                        if (classifyResult != null) {
                            IndicatorUtils.filterNonExistingClassifications(labelMapShort, classifyResult);
                            countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                        }
                        if (countMap == null) {
                            testCount++;
                            continue;
                        }
                        addEventRow(subType, countMap);
                        handleResultMeta(testCount, offsetMap, countMap, classifyResult);
                        testCount++;
                    }
                    mapResult1.put(model, mapResult2);
                }
            }
            mapResult.put(subType, mapResult1);
        }
    }

    // not used?
    private Map<String, Double[]> doClassifications(IclijConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap,
            Map<Double, String> labelMapShort, SubType subType,
            MLClassifyDao mldao, Map<String, Map<String, Double[]>> mapResult2, MLClassifyModel model, int mapTypeInt) {
        AfterBeforeLimit afterbefore = getAfterBefore();
        String mapType = mapTypes.get(mapTypeInt);
        String mapName = subType.getType() + mapType;
        Map<String, List<Pair<double[], Pair<Object, Double>>>> map = mapMap.get(subType).get(mapType);
        log.debug("map name {}", mapName);
        if (map == null || mapMap.get(mapName) == null) {
            log.error("map null and continue? {}", mapName);
            return null;
        }
        int outcomes = (int) map.values().stream().distinct().count();
        outcomes = 4;
        log.debug("Outcomes {}", outcomes);
        Map<String, List<Pair<double[], Pair<Object, Double>>>> learnMap = mapMap.get(subType).get(mapType);
        List<LearnClassify> classifyMLMap = transformLearnClassifyMap(learnMap, true, null, model);
        Map<String, Double[]> classifyResult = mldao.classify(this, classifyMLMap, model, afterbefore.before, outcomes, labelMapShort, mapTime);
        mapResult2.put(mapType, classifyResult);
        return classifyResult;
    }

    private void handleResultMeta(int testCount, Map<String, List<Pair<double[], Pair<Object, Double>>>> offsetMap, Map<String, Long> countMap, Map<String, Double[]> classifyMap) {
        SerialResultMeta resultMeta = (SerialResultMeta) getResultMetas().get(testCount);
        resultMeta.setOffsetMap(transformOffsetMap(offsetMap));
        resultMeta.setClassifyMap(classifyMap);
        resultMeta.setLearnMap(countMap);
    }

    private Map<String, double[]> transformOffsetMap(Map<String, List<Pair<double[], Pair<Object, Double>>>> offsetMap) {
        Map<String, double[]> map = new HashMap<>();
        if (offsetMap == null) {
            return map;
        }
        for (Entry<String, List<Pair<double[], Pair<Object, Double>>>> entry : offsetMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get(0).getLeft());
        }
        log.info("OffsetMap {}", map.keySet());
        return map;
    }

    protected void addEventRow(SubType subType, Map<String, Long> countMap) {
        StringBuilder counts = new StringBuilder();
        counts.append("classified ");
        for (Entry<String, Long> entry : countMap.entrySet()) {
            counts.append(entry.getKey() + " : " + entry.getValue() + " ");
        }
        addEventRow(counts.toString(), subType.getName(), "");
    }

    protected void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add(getName() + " " + key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }

    // not used?
    private void doLearningAndTests(NeuralNetConfigs nnConfigs, IclijConfig conf, Map<String, List<LearnClassify>> mapMap,
            Map<Double, String> labelMapShort, MLMeta mlmeta) {
        AfterBeforeLimit afterbefore = getAfterBefore();
        List<SubType> subTypes = usedSubTypes();
        for (SubType subType : subTypes) {
            if (!subType.useDirectly) {
                continue;
            }
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {
                    for (int mapTypeInt : getMapTypeList()) {
                        String mapType = mapTypes.get(mapTypeInt);
                        String mapName = subType.getType() + mapType;
                        List<LearnClassify> map = mapMap.get(mapName);
                        if (map == null) {
                            log.error("map null {}", mapName);
                            continue;
                        }
                        int outcomes; // = (int) map.values().stream().distinct().count();
                        outcomes = 4;
                        log.debug("Outcomes {}", outcomes);
                        int size = getValidateSize(map, mlmeta);
                        Double testaccuracy = mldao.learntest(nnConfigs, this, map, model, size, outcomes, mapTime, null);  
                        accuracyMap.put(mldao.getName() + model.getName() + subType.getType() + mapType, testaccuracy);
                        IndicatorUtils.filterNonExistingClassifications4(labelMapShort, map);
                        Map<String, Long> countMap = map.stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e.getClassification()), Collectors.counting()));                            
                        SerialResultMeta resultMeta = new SerialResultMeta();
                        resultMeta.setMlName(mldao.getName());
                        resultMeta.setModelName(model.getName());
                        resultMeta.setReturnSize(model.getReturnSize());
                        resultMeta.setSubType(subType.getType());
                        resultMeta.setSubSubType(mapType);
                        resultMeta.setLearnMap(countMap);
                        resultMeta.setTestAccuracy(testaccuracy);
                        getResultMetas().add(resultMeta);
                    }
                }
            }
        }
    }

    protected abstract int fieldSize();

    protected List<Integer> getMapTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(CMNTYPE);
        retList.add(POSTYPE);
        retList.add(NEGTYPE);
        return retList;
    }

    private boolean isBinary(int mapType) {
        return mapType != CMNTYPE;
    }
    
    protected Map<String, double[][]> getListMap() {
        return listMap;
    }

    private void makeMapTypes() {
        mapTypes.put(CMNTYPE, CMNTYPESTR);
        mapTypes.put(POSTYPE, POSTYPESTR);
        mapTypes.put(NEGTYPE, NEGTYPESTR);
    }

    private void handleSpentTime(IclijConfig conf) {
        if (conf.wantMLTimes()) {
            //Map<MLModel, Long> mapTime = new HashMap<>();
            for (MLClassifyModel model : mapTime.keySet()) {
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(key);
                row.add(model.getEngineName());
                row.add(model.getName());
                row.add(mapTime.get(model));
                mlTimesTableRows.add(row);
            }
        }
    }

    private void handleOtherStats(IclijConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap) {
        // and others done with println
        if (conf.wantOtherStats() && conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();            
            List<SubType> subTypes = usedSubTypes();
            for (SubType subType : subTypes) {
                List<Integer> list = new ArrayList<>();
                list.add(POSTYPE);
                list.add(NEGTYPE);
                for (Integer type : list) {
                    String name = mapTypes.get(type);
                    String mapName = subType.getType() + name;
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> myMap = mapMap.get(subType).get(name);
                    if (myMap == null) {
                        log.error("map null {}", mapName);
                        continue;
                    }
                    Map<Double, Long> countMap = getCountMap(myMap);
                    String counts = "";
                    for (Double label : countMap.keySet()) {
                        counts += labelMapShort.get(label) + " : " + countMap.get(label) + " ";
                    }
                    addEventRow(counts, subType.getName(), "");
                }
            }
        }
    }

    private Map<Double, Long> getCountMap(Map<String, List<Pair<double[], Pair<Object, Double>>>> myMap) {
        Map<Double, Long> countMap = myMap.values().stream().map(e -> e).flatMap(Collection::stream).collect(Collectors.groupingBy(e -> e.getRight().getRight(), Collectors.counting()));
        return countMap;
    }

    @Override
    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        Map<Integer, List<ResultItemTableRow>> retMap = new HashMap<>();
        List<ResultItemTable> otherTables = new ArrayList<>();
        if (mlTimesTableRows != null) {
            retMap.put(Constants.MLTIMES, mlTimesTableRows);
        }
        if (eventTableRows != null) {
            retMap.put(Constants.EVENT, eventTableRows);
        }
        return retMap;
    }

    protected String getName(String id) {
        if (idNameMap ==  null) {
            return id;
        }
        String name = idNameMap.get(id);
        if (name == null) {
            name = id;
        }
        return name;
    }

    protected List<SubType> wantedMergeSubTypes() {
        List<SubType> list = new ArrayList<>();
        for (SubType subType : wantedSubTypes()) {
            if (subType.useMergeLimitTrigger) {
                SubType merge = new MergeSubType(getAfterBefore());
                merge.mySubType = subType.mySubType;
                merge.isMerge = true;
                list.add(merge);
            }
        }
        return list;
    }

    protected void makeWantedSubTypes(AfterBeforeLimit afterbefore) {
        this.wantedSubTypes = getWantedSubTypes(afterbefore);
    }
    
    protected List<SubType> wantedSubTypes() {
        return wantedSubTypes;
    }

    protected abstract List<SubType> getWantedSubTypes(AfterBeforeLimit afterbefore);

    private Map<String, SubType> subTypeMap = new HashMap<>();

    private void makeWantedSubTypes(List<SubType> subTypes) {
        for (SubType subType : subTypes) {

        }
    }

    protected void printSignChange(String txt, String id, Map<Integer, Integer> posneg, boolean positive, int listsize, int daysAfterZero, Map<Double, String> labelMapShort) {
    }

    protected String[] shortneg = { Constants.FN, Constants.TN };
    protected String[] shortpos = { Constants.TP, Constants.FP };
    protected String[][] shortposnegs = { shortneg, shortpos };
    protected String[] neg = { labelFN, labelTN };
    protected String[] pos = { labelTP, labelFP };
    protected String[][] posnegs = { neg, pos };
    protected String[] posneg = { POSTYPESTR, NEGTYPESTR };

    private List<SubType> usedSubTypes;

    protected Map<String, Double> createShortLabelMap2() {
        Map<String, Double> labelMap2 = new HashMap<>();
        labelMap2.put(Constants.TP, 1.0);
        labelMap2.put(Constants.FP, 2.0);
        labelMap2.put(Constants.TN, 3.0);
        labelMap2.put(Constants.FN, 4.0);
        return labelMap2;
    }

    protected Map<String, Double> createLabelMap2() {
        Map<String, Double> labelMap2 = new HashMap<>();
        labelMap2.put(labelTP, 1.0);
        labelMap2.put(labelFP, 2.0);
        labelMap2.put(labelTN, 3.0);
        labelMap2.put(labelFN, 4.0);
        return labelMap2;
    }

    protected Map<Double, String> createLabelMap1() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, labelTP);
        labelMap1.put(2.0, labelFP);
        labelMap1.put(3.0, labelTN);
        labelMap1.put(4.0, labelFN);
        return labelMap1;
    }

    public static Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, Constants.TP);
        labelMap1.put(2.0, Constants.FP);
        labelMap1.put(3.0, Constants.TN);
        labelMap1.put(4.0, Constants.FN);
        return labelMap1;
    }

    private void getPosNegMap3(Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap, SubType subType, String commonType, String posnegType , String id,
            double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String[] labels, String[] startlabels, AfterBeforeLimit afterbefore, Double threshold) {
        //boolean endOnly = subType.filters[0].limit == subType.filters[1].limit;
        //Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, afterbefore.before, afterbefore.after, listsize, endOnly);
        SerialTA objs = subType.taMap.get(id);
        //int begOfArray = (int) objs[subType.range[0]];
        int endOfArray = (int) objs.get(subType.range[1]);
        for (Entry<Integer, Integer> entry : posneg.entrySet()) {
            String textlabel;
            int start = entry.getKey();
            int end = entry.getValue();

            //Map<String, Object[]> taMap = subType.taMap;
            //Object[] aMaps = taMap.get(id);
            //double[] arr = (double[]) aMaps[subType.getArrIdx()];
            //double[] truncArray2 = ArraysUtil.getSub(array, start, end);
            //double[] truncArray3 = Arrays.copyOfRange(array, start, end + 1);
            //log.info("g2 {}", Arrays.toString(truncArray2));
            //log.info("g3 {}", Arrays.toString(truncArray3));

            List<Triple<Integer, Integer, String>> triples = getRangeLabel(list, listsize, labels, startlabels, afterbefore,
                    start, end, threshold);
            for (Triple<Integer, Integer, String> triple : triples) {
                start = (int) triple.getLeft();
                end = (int) triple.getMiddle();
                textlabel = (String) triple.getRight();
                log.debug("{}: {} at {}", textlabel, id, end);
                //printme(textlabel, end, list, array, afterbefore);
                double[] truncArray = ArraysUtil.getSub(array, start, end);
		if (!Arrays.stream(truncArray).allMatch(e -> !Double.isNaN(e))) {
		    continue;
		}
                Double doublelabel = labelMap2.get(textlabel);
                Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> subtypeMap = mapGetter3(mapMap, subType);
                if (textlabel != null) {
                    //String commonMapName = subType.getType() + commonType;
                    //String posnegMapName = subType.getType() + posnegType;
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> commonMap = mapGetter(subtypeMap, commonType);
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> posnegMap = mapGetter(subtypeMap, posnegType);
                    //commonMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    //posnegMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    if (doublelabel == null) {
                        int jj = 0;
                    }
                    mapGetter4(commonMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                    mapGetter4(posnegMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                } else {
                    //String subNameShort = subType.getType();
                    //String freshMapName = subType.getType() + posnegType + FRESH;
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> offsetMap = mapGetter(subtypeMap, "offset");
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> commonFreshMap = mapGetter(subtypeMap, FRESH + commonType);
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> posnegFreshMap = mapGetter(subtypeMap, FRESH + posnegType);
                    double[] doubleArray = new double[] { (endOfArray - 1) - (end + 1) };
                    
                    mapGetter4(offsetMap, id).add(new MutablePair(doubleArray, null));
                    mapGetter4(commonFreshMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                    mapGetter4(posnegFreshMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                }
            }
        }
    }

    private void getPosNegMap4(
            Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap,
            SubType subType, String commonType, String posnegType, String id, double[] list,
            Map<String, Double> labelMap2, double[][] arrays, int listsize, Map<Integer, Integer> posneg, String[] labels,
            Object object, AfterBeforeLimit afterbefore, SubType[] subs, Double threshold/*, Pair<Integer, Integer> intersect*/) {
        SerialTA objs = subType.taMap.get(id);
        /*
        int begOfArray = (int) objs[subType.range[0]];
        int endOfArray = (int) objs[subType.range[1]];
        int intersectBegOfArray = intersect.getLeft();
        int intersectEndOfArray = intersect.getRight() - begOfArray + 1;
        */
        for (Entry<Integer, Integer> entry : posneg.entrySet()) {
            String textlabel;
            int start = entry.getKey();
            int end = entry.getValue();

            //Map<String, Object[]> taMap = subType.taMap;
            //Object[] aMaps = taMap.get(id);
            //double[] arr = (double[]) aMaps[subType.getArrIdx()];
            //double[] arr = Arrays.copyOfRange(anArray, start, end + 1);
            //double[] truncArray2 = ArraysUtil.getSub(array, start, end);
            //double[] truncArray3 = Arrays.copyOfRange(array, start, end + 1);
            //log.info("g2 {}", Arrays.toString(truncArray2));
            //log.info("g3 {}", Arrays.toString(truncArray3));

            List<Triple<Integer, Integer, String>> triples = getRangeLabel(list, listsize, labels, null, afterbefore,
                    start, end, threshold);
            out:
            for (Triple<Integer, Integer, String> triple : triples) {
                start = (int) triple.getLeft();
                end = (int) triple.getMiddle();
                textlabel = (String) triple.getRight();
                log.debug("{}: {} at {}", textlabel, id, end);
                //printme(textlabel, end, list, array, afterbefore);
                double[] array = new double[0];
                double[][] newarrays = new double[arrays.length][afterbefore.before];
                for (int i = 0; i < arrays.length; i++) {
                    double[] anArray = arrays[i];
                    //anArray = ArrayUtils.subarray(anArray, intersectBegOfArray, intersectBegOfArray + intersectEndOfArray);
                    //int newBeg = intersectBegOfArray - begOfArray;
                    //anArray = ArrayUtils.subarray(anArray, newBeg, intersectEndOfArray + 1);
                    double[] aTruncArray = ArraysUtil.getSub(anArray, start, end);
                    if (!Arrays.stream(aTruncArray).allMatch(e -> !Double.isNaN(e))) {
                        int jj = 0;
                        continue out;
                    }
                    array = (double[]) ArrayUtils.addAll(array, aTruncArray);                    
                    newarrays[i] = aTruncArray;
                }
                //double[] truncArray = array;
                double[][] truncArray = newarrays;
                Double doublelabel = labelMap2.get(textlabel);
                Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>> subtypeMap = mapGetter3(mapMap, subType);
                if (textlabel != null) {
                    //String commonMapName = subType.getType() + commonType;
                    //String posnegMapName = subType.getType() + posnegType;
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> commonMap = mapGetter(subtypeMap, commonType);
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> posnegMap = mapGetter(subtypeMap, posnegType);
                    //commonMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    //posnegMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    if (doublelabel == null) {
                        int jj = 0;
                    }
                    mapGetter4(commonMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                    mapGetter4(posnegMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                } else {
                    //String subNameShort = subType.getType();
                    //String freshMapName = subType.getType() + posnegType + FRESH;
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> offsetMap = mapGetter(subtypeMap, "offset");
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> commonFreshMap = mapGetter(subtypeMap, FRESH + commonType);
                    Map<String, List<Pair<double[], Pair<Object, Double>>>> posnegFreshMap = mapGetter(subtypeMap, FRESH + posnegType);
                    double[] doubleArray = new double[] { (list.length - 1) - (end + 1)};
                    
                    mapGetter4(offsetMap, id).add(new MutablePair(doubleArray, null));
                    mapGetter4(commonFreshMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                    mapGetter4(posnegFreshMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                }
            }
        }
        
    }

    private List<Triple<Integer, Integer, String>> getRangeLabel(double[] list, int listsize, String[] labels, String[] startlabels,
            AfterBeforeLimit afterbefore, boolean endOnly, int start, int end, Double threshold) {
        String textlabel = null;
        List<Triple<Integer, Integer, String>> triples = new ArrayList<>();
        if (end + 1 + afterbefore.after < listsize) {
            int mystart = start;
            int myend = end;
            if (myend + 1 - mystart >= afterbefore.before) {
                mystart = myend + 1 - afterbefore.before;
                if (mystart < 0) {
                    mystart = 0;
                }
            }
            double change = list[myend + 1 + afterbefore.after] / list[myend + 1];
            if (change > threshold) {
                textlabel = labels[0];
            } else {
                textlabel = labels[1];
            }
            triples.add(new ImmutableTriple(mystart, myend, textlabel));
        }
	// not updated with + 1
        if (!endOnly) {
            if (start - afterbefore.before >= 0 && start + afterbefore.after < listsize) {
                int mystart = start;
                int myend = end;
                if (myend - mystart + 1 >= afterbefore.after) {
                    myend = mystart + afterbefore.after - 1;
                }
                if (list[mystart] < list[mystart + afterbefore.after]) {
                    textlabel = startlabels[0];
                } else {
                    textlabel = startlabels[1];
                }
                triples.add(new ImmutableTriple(mystart, myend, textlabel));
            }
        }
        return triples;
    }

    private List<Triple<Integer, Integer, String>> getRangeLabel(double[] list, int listsize, String[] labels, String[] startlabels,
            AfterBeforeLimit afterbefore, int start, int end, Double threshold) {
        String textlabel = null;
        List<Triple<Integer, Integer, String>> triples = new ArrayList<>();
        if (end == listsize - 1) {
            return triples;
        }
        int mystart = start;
        int myend = end;
        if (myend + 1 - mystart >= afterbefore.before) {
            mystart = myend + 1 - afterbefore.before;
            if (mystart < 0) {
                mystart = 0;
            }
            if (end + 1 + afterbefore.after < listsize) {
                double change = list[myend + 1 + afterbefore.after] / list[myend + 1];
                if (change > threshold) {
                    textlabel = labels[0];
                } else {
                    textlabel = labels[1];
                }
            }
            triples.add(new ImmutableTriple(mystart, myend, textlabel));
        }
	// not updated with + 1
        /*
        if (false) {
            if (start - afterbefore.before >= 0 && start + afterbefore.after < listsize) {
                int mystart = start;
                int myend = end;
                if (myend - mystart + 1 >= afterbefore.after) {
                    myend = mystart + afterbefore.after - 1;
                }
                if (list[mystart] < list[mystart + afterbefore.after]) {
                    textlabel = startlabels[0];
                } else {
                    textlabel = startlabels[1];
                }
                triples.add(List.of(mystart, myend, textlabel));
            }
        }
         */
        return triples;
    }

    protected abstract String getAggregatorsThreshold();

    /**
     * 
     * @param mapMap
     * @param subType
     * @param commonType
     * @param posnegType
     * @param id
     * @param list
     * @param labelMap2
     * @param array
     * @param listsize
     * @param posneg
     * @param labels
     * @param afterbefore
     * 
     * returns 
     * @param threshold TODO
     * 
     */

    private void getPosNegMap(Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>> mapMap, SubType subType, String commonType, String posnegType , String id,
            double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String[] labels, String[] startlabels, AfterBeforeLimit afterbefore, Double threshold) {
        //Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, afterbefore.after, listsize);
        boolean endOnly = true; // subType.filters[0].limit == subType.filters[1].limit;
        for (Entry<Integer, Integer> entry : posneg.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            String textlabel;
            List<Triple<Integer, Integer, String>> triples = getRangeLabel(list, listsize, labels, startlabels, afterbefore,
                    start, end, threshold);
            for (Triple triple : triples) {
                start = (int) triple.getLeft();
                end = (int) triple.getMiddle();
                textlabel = (String) triple.getRight();
                log.debug("{}: {} at {}", textlabel, id, end);
                //printme(textlabel, end, list, array, afterbefore);
                //double[] truncArray = ArraysUtil.getSub(array, entry.getKey(), end);
                Double doublelabel = labelMap2.get(textlabel);
                //String commonMapName = subType + commonType;
                //String posnegMapName = subType + posnegType;
                Map<Pair<Integer, Integer>, Double> commonMap = mapGetter3(mapMap, new ImmutablePair(subType, commonType));
                Map<Pair<Integer, Integer>, Double> posnegMap = mapGetter3(mapMap, new ImmutablePair(subType, posnegType));
                if (doublelabel == null) {
                    int jj = 0;
                }
                commonMap.put(new ImmutablePair(start, end), doublelabel);
                posnegMap.put(new ImmutablePair(start, end), doublelabel);
            }
        }
    }

    /**
     * 
     * @param conf
     * @param metaMap 
     * @param threshold TODO
     * @param range
     * @param afterbefore
     * @param objectMaps
     * @param otherResultMaps
     * @return a complex map
     * 
     * maps for learning
     * a map from the id subtype short name + posnegcmn to a map of a value list to labels
     *
     * next: a map from subtype to a map with posnegcmn to a map from stock id to list of (pair (range, pair of value array and eventual label))
     * a similar without label, to be classified
     * 
     */

    private Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> createPosNegMaps(IclijConfig conf, Map<SubType, MLMeta> metaMap, Double threshold) {
        Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap = new HashMap<>();
        for (String id : getListMap().keySet()) {
            double[][] list = getListMap().get(id);
            log.debug("t {}", Arrays.toString(list[0]));
            log.debug("listsize {}", list.length);
            /*
            if (conf.wantPercentizedPriceIndex() && list[0].length > 0) {
                list[0] = ArraysUtil.getPercentizedPriceIndex(list[0]);
            }
             */
            log.debug("list {} {} ", list.length, Arrays.asList(list));
            if (conf.wantML()) {
                Map<String, Double> labelMap2 = createShortLabelMap2();
                // also macd
                List<SubType> subTypes = wantedSubTypes();
                for (SubType subType : subTypes) {
                    if (!metaMap.containsKey(subType)) {
                        MLMeta mlmeta = new MLMeta();
                        mlmeta.timeseries = true;
                        mlmeta.classify = true;
                        mlmeta.dim1 = subType.afterbefore.before;
                        metaMap.put(subType, mlmeta);
                    } else {
                        MLMeta mlmeta = metaMap.get(subType);
                        if (mlmeta.dim1 != subType.afterbefore.before) {
                            log.error("Wrong value {} {}", mlmeta.dim1, subType.afterbefore.before);
                        }
                    }
                    Map<String, Double[]> anOtherResultMap = subType.resultMap;
                    if (anOtherResultMap == null) {
                        int jj = 0;
                    }
                    Double[] curResult = anOtherResultMap.get(id);
                    if (curResult == null) {
                        log.debug("no macd for id {}", id);
                    }
                    if (curResult == null || !Arrays.stream(curResult).allMatch(i -> i != null)) {
                        continue;
                    }
                    SerialMapTA taObjectMap = subType.taMap;
                    SerialTA taObject = taObjectMap.get(id);
                    int begOfArray = (int) taObject.get(subType.range[0]);
                    int endOfArray = (int) taObject.get(subType.range[1]);
                    log.debug("beg end {} {} {}", id, begOfArray, endOfArray);
                    if (endOfArray <= 0) {
                        log.error("error arrayend 0");
                        continue;
                    }
                    double[] trunclist = ArrayUtils.subarray(list[0], begOfArray, begOfArray + endOfArray);
                    log.debug("trunclist {} {}", list.length, Arrays.asList(trunclist));

                    double[] anArray = (double[]) taObject.getarray(subType.getArrIdx());
                    //anArray = createArray(anArray, begOfArray, endOfArray);
                    for (int i = 0; i < posneg.length; i++) {
                        Map<Integer, Integer>[] map = ArraysUtil.searchForwardLimit(anArray, endOfArray, subType.filters[i].limit, subType.filters[1 - i].limit);
                        // instead of posneg, take from filter
                        getPosNegMap3(mapMap, subType, CMNTYPESTR, posneg[i], id, trunclist, labelMap2, anArray, trunclist.length, map[i], subType.filters[i].texts, null, subType.afterbefore, threshold);
                    }
                }
            }
        }
        return mapMap;
    }

    protected void printme(String label, int end, double[] values, double[] array, AfterBeforeLimit afterbefore) {
        StringBuilder me1 = new StringBuilder();
        StringBuilder me2 = new StringBuilder();
        for (int i = end - 3; i <= Math.min(end + afterbefore.after, values.length - 1); i++) {
            if ( i < 0 ) {
                int jj = 0;
                return;
            }
            if (i >= values.length) {
                int jj = 0;
            }
            me1.append(values[i] + " ");
            me2.append(array[i] + " ");
        }
        String m1 = me1.toString();
        String m2 = me2.toString();
        log.debug("me1 {}", m1);
        log.debug("me2 {}", m2);
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
    public Object[] getResultItem(StockDTO stock) {
        String market = conf.getConfigData().getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new ImmutablePair(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
        Object[] result = resultMap.get(id);
        if (result == null) {
            result = emptyField;
        }
        return result;
    }

    private void doMergeLearn(IclijConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap,
            AfterBeforeLimit afterbefore,
            Map<SubType, MLMeta> metaMap, Double threshold) {
        Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> newMapMap = new HashMap<>();
        Map<String, Double> labelMap2 = createShortLabelMap2();
        List<SubType> subTypes = getWantedSubTypes(afterbefore);
        List<SubType> mergelist = wantedMergeSubTypes();
        // for each wanted merge trigger
        for (SubType mergeSubType : mergelist) {
            int wanteds = 0;
            for (int i = 0; i < subTypes.size(); i++) {
                SubType aSubType = subTypes.get(i);
                if (aSubType.useMerged) {
                    wanteds++;
                }
            }
            SubType mySubType = getMySubType(subTypes, mergeSubType);
            if (!metaMap.containsKey(mySubType)) {
                MLMeta mlmeta = new MLMeta();
                mlmeta.timeseries = true;
                mlmeta.classify = true;
                mlmeta.dim1 = mergeSubType.afterbefore.before;
                mlmeta.dim2 = wanteds;
                if (wanteds == 1) {
                    log.error("Only one dim");
                }
                mlmeta.also1d = true;
                metaMap.put(mySubType, mlmeta);
            } else {
                MLMeta mlmeta = metaMap.get(mergeSubType);
                if (mlmeta != null) {
                    int jj = 0;
                }
            }
        }
        for (SubType mergeSubType : mergelist) {
            SubType mySubType = getMySubType(subTypes, mergeSubType);
            MLMeta mlmeta = metaMap.get(mySubType);
            if (mlmeta != null) {
                if (mlmeta.dim1 != mergeSubType.afterbefore.before) {
                    log.error("Wrong value {} {}", mlmeta.dim1, mergeSubType.afterbefore.before);
                }
            } else {
                int jj = 0;
            }
           // map from h/m + posnegcom to map<model, results>
            for (String id : getListMap().keySet()) {
                double[][] list = getListMap().get(id);
                log.debug("t {}", Arrays.toString(list[0]));
                log.debug("listsize {}", list.length);
                log.debug("list {} {} ", list.length, Arrays.asList(list));
                SubType triggerSubType = null;
                Map<Integer, Map<Integer, Integer>[]> rangeMap = new HashMap<>();
                // for each its subtype
                for (SubType subType : subTypes) {
                    if (mergeSubType.mySubType == subType.mySubType) {
                        triggerSubType = subType;
                        triggerSubType.isMerge = true;
                        //continue;
                        // use with trigger
                        /*
			Map<String, Double[]> anOtherResultMap = subType.resultMap;
			Double[] curResult = anOtherResultMap.get(id);
			if (curResult == null) {
			    log.debug("no macd for id {}", id);
			}
			if (curResult == null || !Arrays.stream(curResult).allMatch(i -> i != null)) {
			    continue;
			}
			Map<String, Object[]> taObjectMap = subType.taMap;
			Object[] taObject = taObjectMap.get(id);
			int begOfArray = (int) taObject[subType.range[0]];
			int endOfArray = (int) taObject[subType.range[1]];
			log.debug("beg end {} {} {}", id, begOfArray, endOfArray);
			if (endOfArray <= 0) {
			    log.error("error arrayend 0");
			    continue;
			}
			double[] trunclist = ArrayUtils.subarray(list[0], begOfArray, begOfArray + endOfArray);
			log.debug("trunclist {} {}", list.length, Arrays.asList(trunclist));

			double[] anArray = (double[]) taObject[subType.getArrIdx()];
			for (int i = 0; i < posneg.length; i++) {
			    Map<Integer, Integer>[] map = ArraysUtil.searchForwardLimit(anArray, endOfArray, subType.filters[i].limit);
			    // instead of posneg, take from filter
			    //getPosNegMap3(mapMap, subType, CMNTYPESTR, posneg[i], id, trunclist, labelMap2, anArray, trunclist.length, map[i], subType.filters[i].texts, null, subType.afterbefore);
			    rangeMap.put(i, map);
			}
			*/
		    }
		}
                //double[] array = new double[0];
                double[][] arrays = new double[mlmeta.dim2][];
                // not used here
                SubType[] subs = new SubType[mlmeta.dim2];
                int count = 0;
                List<Pair<Integer, Integer>> begendList = new ArrayList<>();
		for (SubType subType : subTypes) {
                    if (!subType.useMerged) {
                        continue;
                    }
                    Map<String, Double[]> anOtherResultMap = subType.resultMap;

                    Double[] curResult = anOtherResultMap.get(id);
                    if (curResult == null) {
                        log.debug("no macd for id {}", id);
                    }
                    if (curResult == null || !Arrays.stream(curResult).allMatch(i -> i != null)) {
                        continue;
                    }
                    SerialMapTA taObjectMap = subType.taMap;
                    SerialTA taObject = taObjectMap.get(id);
                    int begOfArray = (int) taObject.get(subType.range[0]);
                    int endOfArray = (int) taObject.get(subType.range[1]);
                    begendList.add(new ImmutablePair(begOfArray, begOfArray + endOfArray - 1));
                    log.debug("beg end {} {} {}", id, begOfArray, endOfArray);
                    if (endOfArray <= 0) {
                        log.error("error arrayend 0");
                        //continue;
                    }
                    subs[count] = subType;
                    if (begOfArray > 0) {
                        //anArray = null;
                    }
		}
		if (begendList.isEmpty()) {
		    continue;
		}
                Pair<Integer, Integer> intersect = ArraysUtil.intersect(begendList);
                int intersectBegOfArray = intersect.getLeft();
                int intersectEndOfArray = intersect.getRight();
                if (mergeSubType.afterbefore.before > intersectEndOfArray + 1 - intersectBegOfArray) {
                    log.error("Mergetype too small");
                    continue;
                }
		double[] triggerArray = null;
		for (SubType subType : subTypes) {
                    if (!subType.useMerged) {
                        continue;
                    }
                    SerialMapTA taObjectMap = subType.taMap;
                    SerialTA taObject = taObjectMap.get(id);
                    if (taObject == null) {
                        int jj = 0;
                    }
                    double[] anArray = (double[]) taObject.getarray(subType.getArrIdx());
                    int begOfArray = (int) taObject.get(subType.range[0]);
                    int endOfArray = (int) taObject.get(subType.range[1]);
                    int newBeg = intersectBegOfArray - begOfArray;
                    if (subType.afterbefore.before > endOfArray - newBeg) {
                        log.error("Subtype too small");
                        continue;
                    }
                    Map<String, Double[]> anOtherResultMap = subType.resultMap;
                    Double[] curResult = anOtherResultMap.get(id);
                    if (curResult == null || !Arrays.stream(curResult).allMatch(i -> i != null)) {
                    	continue;
                    }
		    anArray = ArrayUtils.subarray(anArray, newBeg, endOfArray);
                    arrays[count++] = anArray;
		    if (subType == triggerSubType) {
			triggerArray = anArray;
		    }
                    //double[] arr = Arrays.copyOfRange(anArray, start, end + 1);
                    //array = (double[]) ArrayUtils.addAll(array, arr);
                    //double[] trunclist = ArrayUtils.subarray(list[0], begOfArray, begOfArray + endOfArray);
                    //log.debug("trunclist {} {}", list.length, Arrays.asList(trunclist));

                    //double[] anArray = (double[]) taObject[subType.getArrIdx()];
                    // for all pos neg cmn
                    // for all stock ids
                    // use per subtype one result per
                }
                if (!Arrays.stream(arrays).allMatch(Objects::nonNull)) {
                    continue;
                }
                if (triggerSubType != null) {
                    SubType subType = triggerSubType;
                    SerialMapTA taObjectMap = subType.taMap;
                    SerialTA taObject = taObjectMap.get(id);
                    int begOfArray = (int) taObject.get(subType.range[0]);
                    int endOfArray = (int) taObject.get(subType.range[1]);
                    log.debug("beg end {} {} {}", id, begOfArray, intersectEndOfArray);
                    if (endOfArray <= 0) {
                        log.error("error arrayend 0");
                        continue;
                    }
                    double[] trunclist = ArrayUtils.subarray(list[0], intersectBegOfArray, intersectBegOfArray + intersectEndOfArray);
                    //anArray = createArray(anArray, begOfArray, endOfArray);
                    for (int i = 0; i < posneg.length; i++) {
                           /*
                        if (anArray.length < intersectEndOfArray + 1 - newBeg) {
                            int jj = 0;
                        }
                        */
                        Map<Integer, Integer>[] map = ArraysUtil.searchForwardLimit(triggerArray, triggerArray.length, subType.filters[i].limit, subType.filters[1 - i].limit);
                        //Map<Integer, Integer>[] map = rangeMap.get(i);
                        // instead of posneg, take from filter                  
                        try {
                            getPosNegMap4(newMapMap, subType, CMNTYPESTR, posneg[i], id, trunclist, labelMap2, arrays, trunclist.length, map[i], subType.filters[i].texts, null, mergeSubType.afterbefore, subs, threshold/*, intersect*/);
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                    }
                }
            }
        }
        mapMap.putAll(newMapMap);
    }

    private double[] createArray(double[] anArray, int begOfArray, int endOfArray) {
        double[] newArray = new double[begOfArray + endOfArray];
        return newArray;
    }

    private SubType getMySubType(List<SubType> subTypes, SubType mergeSubType) {
        SubType mySubType = null;
        for (SubType subType : subTypes) {
            if (mergeSubType.mySubType == subType.mySubType) {
                mySubType = subType;
            }
        }
        return mySubType;
    }

    static <K, V> Map<K, V> mapGetter(Map<String, Map<K, V>> mapMap, String key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    static <K, V> Map<K, V> mapGetter2(Map<Pair, Map<K, V>> mapMap, Pair key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    static <K, V, P> Map<K, V> mapGetter3(Map<P, Map<K, V>> mapMap, P key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    static <V, P> List<V> mapGetter4(Map<P, List<V>> mapMap, P key) {
        return mapMap.computeIfAbsent(key, k -> new ArrayList<>());
    }

    private int getTitles(int retindex, Object[] objs, Double threshold) {
        // make OO of this
        List<SubType> subTypes = usedSubTypes();
        if (subTypes == null) {
        	int jj = 0;
        }
        if (subTypes == null || subTypes.isEmpty()) {
            //subTypes = wantedSubTypes();
            int jj = 0;
        }
        for (SubType subType : subTypes) {
            if (!subType.useDirectly) {
                continue;
            }
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {
                    List<Integer> typeList = getTypeList();
                    for (int mapTypeInt : typeList) {
                        String mapType = mapTypes.get(mapTypeInt);
                        String val = "";
                        // workaround
                        try {
                            val = "" + MLClassifyModel.roundme((Double) accuracyMap.get("" + model . getId() + key + subType + mapType));
                            //val = "" + MLClassifyModel.roundme(mldao.eval(model . getId(), key, subType + mapType));
                        } catch (Exception e) {
                            log.error("Exception fix later, refactor", e);
                        }
                        String merge = mergeTxt(subType);
                        objs[retindex++] = title + " " + merge + subType.getName() + " " + threshold + Constants.WEBBR +  subType.getType() + mldao.getShortName() + " " + model.getShortName() + mapType + " " + val;
                        if (model.getReturnSize() > 1) {
                            objs[retindex++] = title + " " + merge + subType.getName() + " " + threshold + Constants.WEBBR +  subType.getType() + mldao.getShortName() + " " + model.getShortName() + mapType + " prob ";
                        }
                        //retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
                    }
                }
            }
        }
        return retindex;
    }

    private String mergeTxt(SubType subType) {
        return subType.isMerge ? "MRG" : "";
    }

    protected List<SubType> usedSubTypes() {
        return usedSubTypes;
    }

    private void cleanMLDaos() {
        for (MLClassifyDao mldao : mldaos) {
            mldao.clean();
        }        
    }

    class AfterBeforeLimit {
        public int before;
        public int after;

        public AfterBeforeLimit(int before, int after) {
            this.before = before;
            this.after = after;
        }
        
        public String getFilePart() {
            return "" + before + "_" + after + "_";
        }
    }

    class Filter {
        public boolean aboveEqual;
        public double limit;
        public String[] texts;

        public Filter(boolean aboveEqual, double limit, String[] texts) {
            this.aboveEqual = aboveEqual;
            this.limit = limit;
            this.texts = texts;
        }
    }

    protected abstract class SubType {
        public abstract String getType();
        public abstract String getName();
        public abstract int getArrIdx();
        //public abstract int getIdx();
        //public abstract Filter getFilter();
        protected Map<String, Double[][]> listMap;
        protected Map<String, Double[]> resultMap;
        protected SerialMapTA taMap;
        protected AfterBeforeLimit afterbefore;
        protected int[] range;
        protected Filter[] filters;
        public boolean useDirectly = true;
        public boolean useMergeLimitTrigger = false;
        public boolean useMerged = false;
        public boolean isMerge = false;
        public MySubType mySubType;
    }

    protected class MergeSubType extends SubType {

        public MergeSubType(AfterBeforeLimit afterbefore) {
            this.afterbefore = afterbefore;
            useMergeLimitTrigger = true;
            useMerged = true;
            }

        @Override
        public String getType() {
            return "Me";
        }

        @Override
        public String getName() {
            return "Merge";
        }

        @Override
        public int getArrIdx() {
            return 0;
        }

    }

    private class FutureMap {
        private SubType subType;

        private MLClassifyDao dao;
        
        private MLClassifyModel model;

        private String mapType;

        private Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap;

        private Map<String, Long> countMap;

        public FutureMap(SubType subType, MLClassifyDao dao, MLClassifyModel model, String mapType, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap, Map<String, Long> countMap) {
            super();
            this.subType = subType;
            this.dao = dao;
            this.model = model;
            this.mapType = mapType;
            this.mapMap = mapMap;
            this.countMap = countMap;
        }

        public SubType getSubType() {
            return subType;
        }

        public void setSubType(SubType subType) {
            this.subType = subType;
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

        public String getMapType() {
            return mapType;
        }

        public void setMapType(String mapType) {
            this.mapType = mapType;
        }

        public Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> getMapMap() {
            return mapMap;
        }

        public void setMapMap(Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<Object, Double>>>>>> mapMap) {
            this.mapMap = mapMap;
        }

        public Map<String, Long> getCountMap() {
            return countMap;
        }

        public void setCountMap(Map<String, Long> countMap) {
            this.countMap = countMap;
        }

    }

    public abstract String getFilenamePart();
    
    public String getFilename(MLClassifyDao dao, MLClassifyModel model, String in, String out, String market, List<AbstractIndicator> indicators, String subType, String mapType, MLMeta mlmeta, Double threshold) {
        String testmarket = conf.getConfigData().getMlmarket();
        if (testmarket != null) {
            market = testmarket;
        }
        return market + "_" + getName() + "_" + dao.getName() + "_" + model.getName() + "_" + getFilenamePart() + threshold + "_" + subType + "_" + mapType + "_" + mlmeta.dimString() + in + "_" + out;
    }
    
}

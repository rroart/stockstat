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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregator.impl.IndicatorAggregator.AfterBeforeLimit;
import roart.aggregator.impl.IndicatorAggregator.SubType;
import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.executor.MyExecutors;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.model.StockItem;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.talib.util.TaConstants;

public abstract class IndicatorAggregator extends Aggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected static final int CMNTYPE = 0;
    protected static final int NEGTYPE = 1;
    protected static final int POSTYPE = 2;
    protected static final String CMNTYPESTR = "cmn";
    protected static final String NEGTYPESTR = "neg";
    protected static final String POSTYPESTR = "pos";

    static String labelTP = "TruePositive";
    static String labelFP = "FalsePositive";
    static String labelTN = "TrueNegative";
    static String labelFN = "FalseNegative";

    protected enum MySubType { MACDHIST, MACDSIG, MACDMACD, RSI, STOCHRSI, STOCH, ATR, CCI }

    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;

    protected Map<String, double[][]> listMap;

    protected int fieldSize = 0;

    protected Object[] emptyField;

    protected String key;

    private Map<String, String> idNameMap;

    private AbstractCategory[] categories;

    private Pipeline[] datareaders;

    private Map<MLClassifyModel, Long> mapTime = new HashMap<>();

    private List<ResultItemTableRow> mlTimesTableRows = null;
    private List<ResultItemTableRow> eventTableRows = null;

    protected Map<Integer, String> mapTypes = new HashMap<>();

    protected List<MLClassifyDao> mldaos = new ArrayList<>();

    protected List<SubType> wantedSubTypes = new ArrayList<>();

    public IndicatorAggregator(MyMyConfig conf, String string, int category, String title, Map<String, String> idNameMap, AbstractCategory[] categories, Pipeline[] datareaders) throws Exception {
        super(conf, string, category);
        this.key = title;
        this.idNameMap = idNameMap;
        this.categories = categories;
        this.datareaders = datareaders;

        makeMapTypes();
        if (conf.wantML()) {
            if (true) {
                if (conf.wantMLSpark()) {
                    mldaos.add(new MLClassifyDao("spark", conf));
                }
                if (conf.wantMLTensorflow()) {
                    mldaos.add(new MLClassifyDao("tensorflow", conf));
                }
            } else {
                mldaos.add(new MLClassifyDao("RANDOM", conf));
            }
        }
        if (conf.wantMLTimes()) {
            mlTimesTableRows = new ArrayList<>();
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        if (isEnabled()) {
            calculateMe(conf, category, categories, datareaders);    
            cleanMLDaos();
        }
    }

    protected abstract String getNeuralNetConfig();

    protected abstract AfterBeforeLimit getAfterBefore();

    private void calculateMe(MyMyConfig conf,
            int category2, AbstractCategory[] categories, Pipeline[] datareaders) throws Exception {
        AbstractCategory cat = IndicatorUtils.getWantedCategory(categories, category);
        if (cat == null) {
            return;
        }
        log.info("checkthis {}", category == cat.getPeriod());
        log.info("checkthis {}", title.equals(cat.getTitle()));
        log.info("checkthis {}", key.equals(title));
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        Map<String, Double[][]> aListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        Map<String, double[][]> fillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);
        Map<String, double[][]>  base100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);
        this.listMap = conf.wantPercentizedPriceIndex() ? base100FillListMap : fillListMap;

        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        if (!anythingHere(aListMap)) {
            log.debug("empty {}", key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        otherResultMap = new HashMap<>();
        objectMap = new HashMap<>();
        probabilityMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();
        long time2 = System.currentTimeMillis();
        AfterBeforeLimit afterbefore = getAfterBefore();
        makeWantedSubTypes(cat, afterbefore);
        //makeWantedSubTypesMap(wantedSubTypes());
        //log.debug("imap " + objectMap.size());
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        log.debug("listmap {} {}", listMap.size(), listMap.keySet());
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap = createPosNegMaps(conf);
        doMergeLearn(conf, mapMap, cat, afterbefore);
        usedSubTypes = new ArrayList<>(mapMap.keySet());
        fieldSize = fieldSize();
        // map from h/m to model to posnegcom map<model, results>
        Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        log.debug("Period {} {}", title, mapMap.keySet());
        String nnconfigString = getNeuralNetConfig();
        NeuralNetConfigs nnConfigs = null;
        if (nnconfigString != null) {
            ObjectMapper mapper = new ObjectMapper();
            nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);
        }
        if (conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();
            if (conf.wantMLMP()) {
                doLearnTestClassifyFuture(nnConfigs, conf, mapMap, mapResult, labelMapShort);
            } else {
                doLearnTestClassify(nnConfigs, conf, mapMap, mapResult, labelMapShort);
            }
        }
        createResultMap(conf, mapResult);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        handleOtherStats(conf, mapMap);
        handleSpentTime(conf);

    }

    protected boolean anythingHere(Map<String, Double[][]> listMap2) {
        for (Double[][] array : listMap2.values()) {
            for (int i = 0; i < array[0].length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean anythingHere3(Map<String, Double[][]> listMap2) {
        for (Double[][] array : listMap2.values()) {
            if (array.length != 3) {
                return false;
            }
            out:
            for (int i = 0; i < array[0].length; i++) {
                for (int j = 0; j < array.length; j++) {
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
        getTitles(retindex, objs);
        headrow.addarr(objs);
    }

    private void doLearnTestClassify(NeuralNetConfigs nnConfigs, MyMyConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        List<SubType> subTypes = usedSubTypes();
        AfterBeforeLimit afterbefore = getAfterBefore();
        // map from h/m + posnegcom to map<model, results>
        int testCount = 0;
        try {
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
                            Map<String, List<Pair<double[], Pair<double[], Double>>>> offsetMap = mapMap.get(subType).get("offset");
                            String mapName = subType.getType() + mapType;
                            Map<String, List<Pair<double[], Pair<double[], Double>>>> learnMap = mapMap.get(subType).get(mapType);
                            Map<String, Long> countMap = null;
                            if (learnMap != null) {
                                IndicatorUtils.filterNonExistingClassifications2(labelMapShort, learnMap);
                                countMap = getCountMap(labelMapShort, learnMap);
                                //countMap = learnMap.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e.getRight().getRight()), Collectors.counting()));                            
                            }
                            // make OO of this, create object
                            Object[] meta = new Object[9];
                            meta[0] = mldao.getName();
                            meta[1] = model.getName();
                            meta[2] = model.getReturnSize();
                            meta[3] = subType.getType();
                            meta[4] = mapType;
                            meta[5] = countMap;
                            resultMetaArray.add(meta);
                            ResultMeta resultMeta = new ResultMeta();
                            resultMeta.setMlName(mldao.getName());
                            resultMeta.setModelName(model.getName());
                            resultMeta.setReturnSize(model.getReturnSize());
                            resultMeta.setSubType(subType.getType());
                            resultMeta.setSubSubType(mapType);
                            resultMeta.setLearnMap(countMap);
                            getResultMetas().add(resultMeta);

                            Map<String, List<Pair<double[], Pair<double[], Double>>>> classifyMap = mapMap.get(subType).get("fresh");
                            log.debug("map name {}", mapName);
                            if (learnMap == null || classifyMap == null || classifyMap.isEmpty()) {
                                log.error("map null and continue? {}", mapName);
                                continue;
                            }
                            int outcomes; // = (int) map.values().stream().distinct().count();
                            outcomes = 4;
                            log.debug("Outcomes {}", outcomes);
                            Map<String, Pair<double[], Double>> learnMLMap = transformLearnClassifyMap(learnMap, true);
                            Map<String, Pair<double[], Double>> classifyMLMap = transformLearnClassifyMap(classifyMap, false);
                            int size = getValidateSize(learnMLMap);
                            LearnTestClassifyResult result = mldao.learntestclassify(nnConfigs, this, learnMLMap, model, size, key, mapName, outcomes, mapTime, classifyMLMap, labelMapShort);  
                            Map<String, Double[]> classifyResult = result.getCatMap();
                            mapResult2.put(mapType, classifyResult);

                            Map<String, Long> countMap2 = null;
                            if (classifyResult != null && !classifyResult.isEmpty()) {
                                countMap2 = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                            }
                            if (countMap2 == null) {
                                testCount++;
                                continue;
                            }

                            probabilityMap.put("" + model . getId() + key + subType + mapType, result.getAccuracy());
                            meta[6] = result.getAccuracy();
                            resultMeta.setTestAccuracy(result.getAccuracy());

                            addEventRow(subType, countMap2);
                            handleResultMeta(testCount, offsetMap, countMap);
                            testCount++;
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
            Map<String, List<Pair<double[], Pair<double[], Double>>>> learnMap) {
        Map<String, Long> countMap;
        countMap = learnMap.values().stream().map(e -> e).flatMap(Collection::stream).collect(Collectors.groupingBy(e -> labelMapShort.get(e.getRight().getRight()), Collectors.counting()));
        return countMap;
    }

    private Map<String, Pair<double[], Double>> transformLearnClassifyMap(Map<String, List<Pair<double[], Pair<double[], Double>>>> learnMap, boolean classify) {
        Map<String, Pair<double[], Double>> mlMap = new HashMap<>();
        for (Entry<String, List<Pair<double[], Pair<double[], Double>>>> entry : learnMap.entrySet()) {
            List<Pair<double[], Pair<double[], Double>>> list = entry.getValue();
            for (Pair<double[], Pair<double[], Double>> pair : list) {
                boolean classified = pair.getRight().getRight() != null;
                if (classified == classify) {
                    mlMap.put(entry.getKey(), pair.getRight());
                }
            }
        }
        return mlMap;
    }

    private void doLearnTestClassifyFuture(NeuralNetConfigs nnConfigs, MyMyConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        List<SubType> subTypes = usedSubTypes();
        AfterBeforeLimit afterbefore = getAfterBefore();
        // map from h/m + posnegcom to map<model, results>
        List<Future<LearnTestClassifyResult>> futureList = new ArrayList<>();
        Map<Future<LearnTestClassifyResult>, FutureMap> futureMap = new HashMap<>();
        try {
            for (SubType subType : subTypes) {
                if (!subType.useDirectly) {
                    continue;
                }
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                if (mapResult1 == null) {
                    mapResult1 = new HashMap<>();
                    mapResult.put(subType, mapResult1);
                }
                for (MLClassifyDao mldao : mldaos) {
                    // map from posnegcom to map<id, result>
                    for (MLClassifyModel model : mldao.getModels()) {
                        Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                        if (mapResult2 == null) {
                            mapResult2 = new HashMap<>();
                            mapResult1.put(model, mapResult2);
                        }
                        for (int mapTypeInt : getMapTypeList()) {
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            Map<String, List<Pair<double[], Pair<double[], Double>>>> learnMap = mapMap.get(subType).get(mapType);
                            Map<String, Long> countMap = null;
                            if (learnMap != null) {
                                IndicatorUtils.filterNonExistingClassifications3(labelMapShort, learnMap);
                                countMap = getCountMap(labelMapShort, learnMap);
                            }
                            // make OO of this, create object
                            Object[] meta = new Object[9];
                            meta[0] = mldao.getName();
                            meta[1] = model.getName();
                            meta[2] = model.getReturnSize();
                            meta[3] = subType.getType();
                            meta[4] = mapType;
                            meta[5] = countMap;
                            resultMetaArray.add(meta);
                            ResultMeta resultMeta = new ResultMeta();
                            resultMeta.setMlName(mldao.getName());
                            resultMeta.setModelName(model.getName());
                            resultMeta.setReturnSize(model.getReturnSize());
                            resultMeta.setSubType(subType.getType());
                            resultMeta.setSubSubType(mapType);
                            resultMeta.setLearnMap(countMap);
                            getResultMetas().add(resultMeta);

                            Map<String, List<Pair<double[], Pair<double[], Double>>>> classifyMap = mapMap.get(subType).get("fresh");
                            log.debug("map name {}", mapName);
                            if (learnMap == null || classifyMap == null || classifyMap.isEmpty()) {
                                log.warn("Map null and continue? {}", mapName);
                                continue;
                            }
                            int outcomes = (int) learnMap.values().stream().distinct().count();
                            outcomes = 4;
                            log.debug("Outcomes {}", outcomes);
                            Map<String, Pair<double[], Double>> learnMLMap = transformLearnClassifyMap(learnMap, true);
                            Map<String, Pair<double[], Double>> classifyMLMap = transformLearnClassifyMap(classifyMap, false);
                            int size = getValidateSize(learnMLMap);
                            Callable callable = new MLClassifyLearnTestPredictCallable(nnConfigs, mldao, this, learnMLMap, model, size, key, mapName, outcomes, mapTime, classifyMLMap, labelMapShort);  
                            Future<LearnTestClassifyResult> future = MyExecutors.run(callable, 1);
                            futureList.add(future);
                            futureMap.put(future, new FutureMap(subType, model, mapType, resultMetaArray.size() - 1, mapMap));
                        }
                    }
                }
            }
            for (Future<LearnTestClassifyResult> future: futureList) {
                FutureMap futMap = futureMap.get(future);
                SubType subType = futMap.getSubType();
                MLClassifyModel model = futMap.getModel();
                String mapType = futMap.getMapType();
                int testCount = futMap.getTestCount();
                LearnTestClassifyResult result = future.get();
                Map<String, Double[]> classifyResult = result.getCatMap();
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                mapResult2.put(mapType, classifyResult);
                Map<String, Long> countMap2 = null;
                if (classifyResult != null && !classifyResult.isEmpty()) {
                    countMap2 = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                }
                if (countMap2 == null) {
                    continue;
                }

                probabilityMap.put("" + model . getId() + key + subType + mapType, result.getAccuracy());
                handleResultMetaAccuracy(testCount, result);

                addEventRow(subType, countMap2);
                Map<String, List<Pair<double[], Pair<double[], Double>>>> offsetMap = mapMap.get(subType).get("offset");
                handleResultMeta(testCount, offsetMap, countMap2);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private int getValidateSize(Map<String, Pair<double[], Double>> map) {
        int size = -1;
        for (Entry<String, Pair<double[], Double>> entry : map.entrySet()) {
            Pair<double[], Double> value = entry.getValue();
            if (size < 0) {
                size = value.getLeft().length;
            }
            if (size != value.getLeft().length) {
                return -1;
            }
        }
        return size;
    }

    private void handleResultMetaAccuracy(int testCount, LearnTestClassifyResult result) {
        Object[] meta = resultMetaArray.get(testCount);
        ResultMeta resultMeta = getResultMetas().get(testCount);
        meta[6] = result.getAccuracy();
        resultMeta.setTestAccuracy(result.getAccuracy());
    }

    private void createResultMap(MyMyConfig conf,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult) {
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[fieldSize];
            resultMap.put(id, fields);
            int retindex = 0; //tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);

            // make OO of this
            if (conf.wantML()) {
                Map<Double, String> labelMapShort2 = createLabelMapShort();
                //int momidx = 6;
                Double[] type;
                List<SubType> subTypes2 = usedSubTypes();
                for (SubType subType : subTypes2) {
                    Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
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
                                Map<String, Double[]> resultMap1 = mapResult2.get(mapType);
                                Double[] aType = null;
                                if (resultMap1 != null) {
                                    aType = resultMap1.get(id);
                                } else {
                                    log.info("map null {}", mapType);
                                }
                                int modelSize = model.getSizes(this);
                                if (retindex > 28) {
                                    int jj = 0;
                                }
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
            //log.debug("ri" + retindex);
            if (retindex != fieldSize) {
                log.error("Field size too small {} < {}", retindex, fieldSize);
            }
        }
    } 

    private void doClassifications(MyMyConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap,
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
                        Map<String, List<Pair<double[], Pair<double[], Double>>>> offsetMap = mapMap.get(subType).get("offset");
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
                        handleResultMeta(testCount, offsetMap, countMap);
                        testCount++;
                    }
                    mapResult1.put(model, mapResult2);
                }
            }
            mapResult.put(subType, mapResult1);
        }
    }

    private Map<String, Double[]> doClassifications(MyMyConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap,
            Map<Double, String> labelMapShort, SubType subType,
            MLClassifyDao mldao, Map<String, Map<String, Double[]>> mapResult2, MLClassifyModel model, int mapTypeInt) {
        AfterBeforeLimit afterbefore = getAfterBefore();
        String mapType = mapTypes.get(mapTypeInt);
        String mapName = subType.getType() + mapType;
        Map<String, List<Pair<double[], Pair<double[], Double>>>> map = mapMap.get(subType).get(mapType);
        log.debug("map name {}", mapName);
        if (map == null || mapMap.get(mapName) == null) {
            log.error("map null and continue? {}", mapName);
            return null;
        }
        int outcomes = (int) map.values().stream().distinct().count();
        outcomes = 4;
        log.debug("Outcomes {}", outcomes);
        Map<String, List<Pair<double[], Pair<double[], Double>>>> learnMap = mapMap.get(subType).get(mapType);
        Map<String, Pair<double[], Double>> classifyMLMap = transformLearnClassifyMap(learnMap, true);
        Map<String, Double[]> classifyResult = mldao.classify(this, classifyMLMap, model, afterbefore.before, key, mapName, outcomes, labelMapShort, mapTime);
        mapResult2.put(mapType, classifyResult);
        return classifyResult;
    }

    private void handleResultMeta(int testCount, Map<String, List<Pair<double[], Pair<double[], Double>>>> offsetMap, Map<String, Long> countMap) {
        Object[] meta = resultMetaArray.get(testCount);
        meta[7] = countMap;
        meta[8] = transformOffsetMap(offsetMap);
        ResultMeta resultMeta = getResultMetas().get(testCount);
        resultMeta.setClassifyMap(countMap);
    }

    private Map<String, double[]> transformOffsetMap(Map<String, List<Pair<double[], Pair<double[], Double>>>> offsetMap) {
        Map<String, double[]> map = new HashMap<>();
        for (Entry<String, List<Pair<double[], Pair<double[], Double>>>> entry : offsetMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue().get(0).getLeft());
        }
        return map;
    }

    private void addEventRow(SubType subType, Map<String, Long> countMap) {
        StringBuilder counts = new StringBuilder();
        counts.append("classified ");
        for (Entry<String, Long> entry : countMap.entrySet()) {
            counts.append(entry.getKey() + " : " + entry.getValue() + " ");
        }
        addEventRow(counts.toString(), subType.getName(), "");
    }

    private void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add(getName() + " " + key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }

    private void doLearningAndTests(NeuralNetConfigs nnConfigs, MyMyConfig conf, Map<String, Map<String, Pair<double[], Double>>> mapMap,
            Map<Double, String> labelMapShort) {
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
                        Map<String, Pair<double[], Double>> map = mapMap.get(mapName);
                        if (map == null) {
                            log.error("map null {}", mapName);
                            continue;
                        }
                        int outcomes = (int) map.values().stream().distinct().count();
                        outcomes = 4;
                        log.debug("Outcomes {}", outcomes);
                        int size = getValidateSize(map);
                        Double testaccuracy = mldao.learntest(nnConfigs, this, map, model, size, key, mapName, outcomes, mapTime);  
                        probabilityMap.put("" + model . getId() + key + subType + mapType, testaccuracy);
                        IndicatorUtils.filterNonExistingClassifications2(labelMapShort, map);
                        Map<String, Long> countMap = map.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e), Collectors.counting()));                            
                        // make OO of this, create object
                        Object[] meta = new Object[9];
                        meta[0] = mldao.getName();
                        meta[1] = model.getName();
                        meta[2] = model.getReturnSize();
                        meta[3] = subType.getType();
                        meta[4] = mapType;
                        meta[5] = countMap;
                        meta[6] = testaccuracy;
                        resultMetaArray.add(meta);
                        ResultMeta resultMeta = new ResultMeta();
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

    protected Map<String, double[][]> getListMap() {
        return listMap;
    }

    private void makeMapTypes() {
        mapTypes.put(CMNTYPE, CMNTYPESTR);
        mapTypes.put(POSTYPE, POSTYPESTR);
        mapTypes.put(NEGTYPE, NEGTYPESTR);
    }

    private void handleSpentTime(MyMyConfig conf) {
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

    private void handleOtherStats(MyMyConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap) {
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
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> myMap = mapMap.get(subType).get(name);
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

    private Map<Double, Long> getCountMap(Map<String, List<Pair<double[], Pair<double[], Double>>>> myMap) {
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
        if (idNameMap == null) {
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

    protected void makeWantedSubTypes(AbstractCategory cat, AfterBeforeLimit afterbefore) {
        this.wantedSubTypes = getWantedSubTypes(cat, afterbefore);
    }
    
    protected List<SubType> wantedSubTypes() {
        return wantedSubTypes;
    }

    protected abstract List<SubType> getWantedSubTypes(AbstractCategory cat, AfterBeforeLimit afterbefore);

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

    private void getPosNegMap3(Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap, SubType subType, String commonType, String posnegType , String id,
            double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String[] labels, String[] startlabels, AfterBeforeLimit afterbefore) {
        //boolean endOnly = subType.filters[0].limit == subType.filters[1].limit;
        //Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, afterbefore.before, afterbefore.after, listsize, endOnly);
        Object[] objs = subType.taMap.get(id);
        //int begOfArray = (int) objs[subType.range[0]];
        int endOfArray = (int) objs[subType.range[1]];
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
                    start, end);
            for (Triple<Integer, Integer, String> triple : triples) {
                start = (int) triple.getLeft();
                end = (int) triple.getMiddle();
                textlabel = (String) triple.getRight();
                log.debug("{}: {} at {}", textlabel, id, end);
                //printme(textlabel, end, list, array, afterbefore);
                double[] truncArray = ArraysUtil.getSub(array, start, end);
                Double doublelabel = labelMap2.get(textlabel);
                Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>> subtypeMap = mapGetter3(mapMap, subType);
                if (textlabel != null) {
                    //String commonMapName = subType.getType() + commonType;
                    //String posnegMapName = subType.getType() + posnegType;
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> commonMap = mapGetter(subtypeMap, commonType);
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> posnegMap = mapGetter(subtypeMap, posnegType);
                    //commonMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    //posnegMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    if (doublelabel == null) {
                        int jj = 0;
                    }
                    mapGetter4(commonMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                    mapGetter4(posnegMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                } else {
                    //String subNameShort = subType.getType();
                    //String freshMapName = subType.getType() + posnegType + "fresh";
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> offsetMap = mapGetter(subtypeMap, "offset");
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> freshMap = mapGetter(subtypeMap, "fresh");
                    double[] doubleArray = new double[] { endOfArray - end };
                    mapGetter4(offsetMap, id).add(new MutablePair(doubleArray, null));
                    mapGetter4(freshMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                }
            }
        }
    }

    private void getPosNegMap4(
            Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap,
            SubType subType, String commonType, String posnegType, String id, double[] list,
            Map<String, Double> labelMap2, double[][] arrays, int listsize, Map<Integer, Integer> posneg, String[] labels,
            Object object, AfterBeforeLimit afterbefore, SubType[] subs) {
        Object[] objs = subType.taMap.get(id);
        int begOfArray = (int) objs[subType.range[0]];
        int endOfArray = (int) objs[subType.range[1]];
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
                    start, end);
            for (Triple<Integer, Integer, String> triple : triples) {
                start = (int) triple.getLeft();
                end = (int) triple.getMiddle();
                textlabel = (String) triple.getRight();
                log.debug("{}: {} at {}", textlabel, id, end);
                //printme(textlabel, end, list, array, afterbefore);
                double[] array = new double[0];
                for (int i = 0; i < arrays.length; i++) {
                    double[] anArray = arrays[i];
                    double[] aTruncArray = ArraysUtil.getSub(anArray, start, end);
                    array = (double[]) ArrayUtils.addAll(array, aTruncArray);                    
                }
                double[] truncArray = array;
                Double doublelabel = labelMap2.get(textlabel);
                Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>> subtypeMap = mapGetter3(mapMap, subType);
                if (textlabel != null) {
                    //String commonMapName = subType.getType() + commonType;
                    //String posnegMapName = subType.getType() + posnegType;
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> commonMap = mapGetter(subtypeMap, commonType);
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> posnegMap = mapGetter(subtypeMap, posnegType);
                    //commonMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    //posnegMap.put(id, new ImmutablePair(truncArray, doublelabel));
                    if (doublelabel == null) {
                        int jj = 0;
                    }
                    mapGetter4(commonMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                    mapGetter4(posnegMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                } else {
                    //String subNameShort = subType.getType();
                    //String freshMapName = subType.getType() + posnegType + "fresh";
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> offsetMap = mapGetter(subtypeMap, "offset");
                    Map<String, List<Pair<double[], Pair<double[], Double>>>> freshMap = mapGetter(subtypeMap, "fresh");
                    double[] doubleArray = new double[] { endOfArray - end };
                    mapGetter4(offsetMap, id).add(new MutablePair(doubleArray, null));
                    mapGetter4(freshMap, id).add(new ImmutablePair(new double[] { start, end }, new ImmutablePair(truncArray, doublelabel)));
                }
            }
        }
        
    }

    private List<Triple<Integer, Integer, String>> getRangeLabel(double[] list, int listsize, String[] labels, String[] startlabels,
            AfterBeforeLimit afterbefore, boolean endOnly, int start, int end) {
        String textlabel = null;
        List<Triple<Integer, Integer, String>> triples = new ArrayList<>();
        if (end + afterbefore.after < listsize) {
            int mystart = start;
            int myend = end;
            if (myend - mystart + 1 >= afterbefore.before) {
                mystart = myend - afterbefore.before + 1;
                if (mystart < 0) {
                    mystart = 0;
                }
            }
            if (list[myend] < list[myend + afterbefore.after]) {
                textlabel = labels[0];
            } else {
                textlabel = labels[1];
            }
            triples.add(new ImmutableTriple(mystart, myend, textlabel));
        }
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
            AfterBeforeLimit afterbefore, int start, int end) {
        String textlabel = null;
        List<Triple<Integer, Integer, String>> triples = new ArrayList<>();
        if (end == listsize - 1) {
            return triples;
        }
        int mystart = start;
        int myend = end;
        if (myend - mystart + 1 >= afterbefore.before) {
            mystart = myend - afterbefore.before + 1;
            if (mystart < 0) {
                mystart = 0;
            }
            if (end + afterbefore.after < listsize) {
                if (list[myend] < list[myend + afterbefore.after]) {
                    textlabel = labels[0];
                } else {
                    textlabel = labels[1];
                }
            }
            triples.add(new ImmutableTriple(mystart, myend, textlabel));
        }
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
                triples.add(new ImmutableTriple(mystart, myend, textlabel));
            }
        }
         */
        return triples;
    }

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
     * 
     */

    private void getPosNegMap(Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>> mapMap, SubType subType, String commonType, String posnegType , String id,
            double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String[] labels, String[] startlabels, AfterBeforeLimit afterbefore) {
        //Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, afterbefore.after, listsize);
        boolean endOnly = true; // subType.filters[0].limit == subType.filters[1].limit;
        for (Entry<Integer, Integer> entry : posneg.entrySet()) {
            int start = entry.getKey();
            int end = entry.getValue();
            String textlabel;
            List<Triple<Integer, Integer, String>> triples = getRangeLabel(list, listsize, labels, startlabels, afterbefore,
                    start, end);
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

    private Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> createPosNegMaps(MyMyConfig conf) {
        Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap = new HashMap<>();
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
                        getPosNegMap3(mapMap, subType, CMNTYPESTR, posneg[i], id, trunclist, labelMap2, anArray, trunclist.length, map[i], subType.filters[i].texts, null, subType.afterbefore);
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
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] objs = new Object[fieldSize];
        Object[] fields = objs; 
        if (resultMap != null) {
            fields = resultMap.get(stock.getId());
        }

        row.addarr(fields);
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        String market = conf.getMarket();
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

    private void doMergeLearn(MyMyConfig conf, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap,
            AbstractCategory cat,
            AfterBeforeLimit afterbefore) {
        Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> newMapMap = new HashMap<>();
        Map<String, Double> labelMap2 = createShortLabelMap2();
        List<SubType> subTypes = getWantedSubTypes(cat, afterbefore);
        List<SubType> mergelist = wantedMergeSubTypes();
        // for each wanted merge trigger
        for (SubType mergeSubType : mergelist) {
            int wantedSize = 0;
            int wanteds = 0;
            //for (String mapType : mapTypes.values()) {
            Map<String, Pair<double[], Double>> resultMap = new HashMap<>();
            for (int i = 0; i < subTypes.size(); i++) {
                SubType aSubType = subTypes.get(i);
                if (aSubType.useMerged) {
                    wanteds++;
                    wantedSize += getAfterBefore().before;
                }
            }
            //Map<String, Map<Pair<SubType, String>, Map<Pair<Integer, Integer>, Double>>> m = getPosNeg(labelMapShort, afterbefore);
            Map<Double, String> reverseClassification = createLabelMapShort();
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
                double[][] arrays = new double[wanteds][];
                SubType[] subs = new SubType[wanteds];
                int count = 0;
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
                    Map<String, Object[]> taObjectMap = subType.taMap;
                    Object[] taObject = taObjectMap.get(id);
                    double[] anArray = (double[]) taObject[subType.getArrIdx()];
                    int begOfArray = (int) taObject[subType.range[0]];
                    int endOfArray = (int) taObject[subType.range[1]];
                    log.debug("beg end {} {} {}", id, begOfArray, endOfArray);
                    if (endOfArray <= 0) {
                        log.error("error arrayend 0");
                        //continue;
                    }
                    subs[count] = subType;
                    arrays[count++] = anArray;
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
                    double[] anArray = (double[]) taObject[subType.getArrIdx()];
                    for (int i = 0; i < posneg.length; i++) {
                        Map<Integer, Integer>[] map = ArraysUtil.searchForwardLimit(anArray, endOfArray, subType.filters[i].limit);
                        //Map<Integer, Integer>[] map = rangeMap.get(i);
                        // instead of posneg, take from filter                  
                        try {
                        getPosNegMap4(newMapMap, subType, CMNTYPESTR, posneg[i], id, trunclist, labelMap2, arrays, trunclist.length, map[i], subType.filters[i].texts, null, mergeSubType.afterbefore, subs);
                        } catch (Exception e) {
                            log.error(Constants.EXCEPTION, e);
                        }
                    }
                }
            }
        }
        mapMap.putAll(newMapMap);
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

    private int getTitles(int retindex, Object[] objs) {
        // make OO of this
        List<SubType> subTypes = usedSubTypes();
        if (subTypes == null || subTypes.isEmpty()) {
            subTypes = wantedSubTypes();
        }
        for (SubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {
                    List<Integer> typeList = getTypeList();
                    for (int mapTypeInt : typeList) {
                        String mapType = mapTypes.get(mapTypeInt);
                        String val = "";
                        // workaround
                        try {
                            val = "" + MLClassifyModel.roundme((Double) probabilityMap.get("" + model . getId() + key + subType + mapType));
                            //val = "" + MLClassifyModel.roundme(mldao.eval(model . getId(), key, subType + mapType));
                        } catch (Exception e) {
                            log.error("Exception fix later, refactor", e);
                        }
                        String merge = subType.isMerge ? "MRG " : "";
                        objs[retindex++] = title + " " + merge + subType.getName() + Constants.WEBBR +  subType.getType() + model.getName() + mapType + " " + val;
                        if (model.getReturnSize() > 1) {
                            objs[retindex++] = title + " " + merge + subType.getName() + Constants.WEBBR +  subType.getType() + model.getName() + mapType + " prob ";
                        }
                        //retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
                    }
                }
            }
        }
        return retindex;
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
        protected Map<String, Object[]> taMap;
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

        private MLClassifyModel model;

        private String mapType;

        private int testCount;

        private Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap;

        public FutureMap(SubType subType, MLClassifyModel model, String mapType, int testCount, Map<SubType, Map<String, Map<String, List<Pair<double[], Pair<double[], Double>>>>>> mapMap) {
            super();
            this.subType = subType;
            this.model = model;
            this.mapType = mapType;
            this.testCount = testCount;
            this.mapMap = mapMap;
        }

        public SubType getSubType() {
            return subType;
        }

        public void setSubType(SubType subType) {
            this.subType = subType;
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

        public int getTestCount() {
            return testCount;
        }

        public void setTestCount(int testCount) {
            this.testCount = testCount;
        }

    }

}

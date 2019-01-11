package roart.aggregate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tictactec.ta.lib.MInteger;

import roart.category.Category;
import roart.common.config.MyMyConfig;
import roart.common.ml.NNConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.db.dao.DbDao;
import roart.common.constants.Constants;
import roart.indicator.IndicatorUtils;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.common.MLClassifyModel;
import roart.model.StockItem;
import roart.queue.MyExecutors;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.model.data.PeriodData;
import roart.util.TaUtil;
import roart.pipeline.common.aggregate.Aggregator;

public class MLMACD extends Aggregator {

    Map<String, PeriodData> periodDataMap;
    String key;
    Map<String, Double[][]> listMap;
    Map<String, double[][]> truncListMap;
    // TODO save and return this map
    // TODO need getters for this and not? buy/sell
    Map<String, Double[]> momMap;
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();

    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;

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
        List<ResultItemTable> otherTables = new ArrayList<>();
        if (mlTimesTableRows != null) {
            retMap.put(ControlService.MLTIMES, mlTimesTableRows);
        }
        if (eventTableRows != null) {
            retMap.put(ControlService.EVENT, eventTableRows);
        }
        return retMap;
    }

    List<MLClassifyDao> mldaos = new ArrayList<>();

    public MLMACD(MyMyConfig conf, String string, List<StockItem> stocks, Map<String, PeriodData> periodDataMap, 
            String title, int category, Category[] categories) throws Exception {
        super(conf, string, category);
        this.periodDataMap = periodDataMap;
        this.key = title;
        makeWantedSubTypes();
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
            calculateMomentums(conf, periodDataMap, category, categories);    
            cleanMLDaos();
        }
    }

    private void cleanMLDaos() {
        for (MLClassifyDao mldao : mldaos) {
            mldao.clean();
        }        
    }

    private interface MacdSubType {
        public abstract  String getType();
        public abstract  String getName();
        public abstract  int getArrIdx();
    }

    private class MacdSubTypeHist implements MacdSubType {
        @Override
        public String getType() {
            return "H";
        }
        @Override
        public String getName() {
            return "Hist";
        }
        @Override
        public int getArrIdx() {
            return TaUtil.MACDIDXHIST;
        }
    }

    private class MacdSubTypeMacd implements MacdSubType {
        @Override
        public String getType() {
            return "M";
        }
        @Override
        public String getName() {
            return "Macd";
        }
        @Override
        public int getArrIdx() {
            return TaUtil.MACDIDXMACD;
        }
    }

    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }

    private List<Integer> getMapTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(CMNTYPE);
        retList.add(POSTYPE);
        retList.add(NEGTYPE);
        return retList;
    }

    @Override
    public List<Integer> getTypeList() {
        return getMapTypeList();
    }

    private Map<Integer, String> mapTypes = new HashMap<>();

    private void makeMapTypes() {
        mapTypes.put(CMNTYPE, CMNTYPESTR);
        mapTypes.put(POSTYPE, POSTYPESTR);
        mapTypes.put(NEGTYPE, NEGTYPESTR);
    }
    private static final int CMNTYPE = 0;
    private static final int NEGTYPE = 1;
    private static final int POSTYPE = 2;
    private static final String CMNTYPESTR = "cmn";
    private static final String NEGTYPESTR = "neg";
    private static final String POSTYPESTR = "pos";

    private List<MacdSubType> wantedSubTypes = new ArrayList<>();

    private List<MacdSubType> wantedSubTypes() {
        return wantedSubTypes;
    }

    private void makeWantedSubTypes() {
        if (conf.wantMLHist()) {
            wantedSubTypes.add(new MacdSubTypeHist());
        }
        if (conf.wantMLMacd()) {
            wantedSubTypes.add(new MacdSubTypeMacd());
        }
    }
    // TODO make an oo version of this
    private void calculateMomentums(MyMyConfig conf, Map<String, PeriodData> periodDataMap,
            int category2, Category[] categories) throws Exception {
        Category cat = IndicatorUtils.getWantedCategory(categories);
        if (cat == null) {
            return;
        }
        category = cat.getPeriod();
        title = cat.getTitle();
        key = title;
        Object macd = cat.getResultMap().get(PipelineConstants.INDICATORMACDRESULT);
        Object list0 = cat.getResultMap().get(PipelineConstants.INDICATORMACDLIST);
        Object object = cat.getResultMap().get(PipelineConstants.INDICATORMACDOBJECT);
        Object mom0 = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD).get(PipelineConstants.RESULT);
        momMap = (Map<String, Double[]>) mom0;
        truncListMap = (Map<String, double[][]>) cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD).get(PipelineConstants.TRUNCLIST);

        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        this.listMap = (Map<String, Double[][]>) list0;
        if (conf.wantPercentizedPriceIndex()) {

        }
        if (!anythingHere(listMap)) {
            System.out.println("empty"+key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        otherResultMap = new HashMap<>();
        objectMap = new HashMap<>();
        probabilityMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();
        List<Double>[] macdLists = new ArrayList[4];
        for (int i = 0; i < 4; i ++) {
            macdLists[i] = new ArrayList<>();
        }
        long time2 = System.currentTimeMillis();
        objectMap = (Map<String, Object[]>) object;
        //System.out.println("imap " + objectMap.size());
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        log.info("listmap {} {}", listMap.size(), listMap.keySet());
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = createPosNegMaps(conf);
        // map from h/m to model to posnegcom map<model, results>
        Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        log.info("Period {} {}", title, mapMap.keySet());
        String nnconfigString = conf.getMLMACDMLConfig();
        NNConfigs nnConfigs = null;
        if (nnconfigString != null) {
            ObjectMapper mapper = new ObjectMapper();
            nnConfigs = mapper.readValue(nnconfigString, NNConfigs.class);
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

    private void doLearnTestClassify(NNConfigs nnConfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        List<MacdSubType> subTypes = wantedSubTypes();
        Map<String, Map<String, double[]>> mapIdMap = getNewestPosNeg(labelMapShort);
        // map from h/m + posnegcom to map<model, results>
        int testCount = 0;
        try {
            for (MacdSubType subType : subTypes) {
                Map<String, double[]> offsetMap = mapIdMap.get(subType.getType());
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
                for (MLClassifyDao mldao : mldaos) {
                    // map from posnegcom to map<id, result>
                    Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                    for (MLClassifyModel model : mldao.getModels()) {
                        for (int mapTypeInt : getMapTypeList()) {
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            Map<double[], Double> map = mapMap.get(mapName);
                            if (map == null) {
                                log.error("map null {}", mapName);
                                continue;
                            }
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
                            resultMetaArray.add(meta);
                            ResultMeta resultMeta = new ResultMeta();
                            resultMeta.setMlName(mldao.getName());
                            resultMeta.setModelName(model.getName());
                            resultMeta.setReturnSize(model.getReturnSize());
                            resultMeta.setSubType(subType.getType());
                            resultMeta.setSubSubType(mapType);
                            resultMeta.setLearnMap(countMap);

                            Map<String, double[]> map2 = mapIdMap.get(mapName);
                            log.info("map name {}", mapName);
                            if (map == null || mapMap.get(mapName) == null) {
                                log.error("map null and continue? {}", mapName);
                                continue;
                            }
                            int outcomes = (int) map.values().stream().distinct().count();
                            outcomes = 4;
                            log.info("Outcomes {}", outcomes);
                            LearnTestClassifyResult result = mldao.learntestclassify(nnConfigs, this, map, model, conf.getMACDDaysBeforeZero(), key, mapName, outcomes, mapTime, map2, labelMapShort);  
                            Map<String, Double[]> classifyResult = result.getCatMap();
                            mapResult2.put(mapType, classifyResult);
                            probabilityMap.put("" + model . getId() + key + subType + mapType, result.getAccuracy());
                            meta[6] = result.getAccuracy();
                            resultMeta.setTestAccuracy(result.getAccuracy());
                            getResultMetas().add(resultMeta);

                            Map<String, Long> countMap2 = null;
                            if (classifyResult != null) {
                                countMap2 = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                            }
                            if (countMap2 == null) {
                                continue;
                            }
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

    private void doLearnTestClassifyFuture(NNConfigs nnConfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        List<MacdSubType> subTypes = wantedSubTypes();
        Map<String, Map<String, double[]>> mapIdMap = getNewestPosNeg(labelMapShort);
        // map from h/m + posnegcom to map<model, results>
        List<Future<LearnTestClassifyResult>> futureList = new ArrayList<>();
        Map<Future<LearnTestClassifyResult>, FutureMap> futureMap = new HashMap<>();
        try {
            for (MacdSubType subType : subTypes) {
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
                            Map<double[], Double> map = mapMap.get(mapName);
                            if (map == null) {
                                log.error("map null {}", mapName);
                                continue;
                            }
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
                            resultMetaArray.add(meta);
                            ResultMeta resultMeta = new ResultMeta();
                            resultMeta.setMlName(mldao.getName());
                            resultMeta.setModelName(model.getName());
                            resultMeta.setReturnSize(model.getReturnSize());
                            resultMeta.setSubType(subType.getType());
                            resultMeta.setSubSubType(mapType);
                            resultMeta.setLearnMap(countMap);
                            getResultMetas().add(resultMeta);

                            Map<String, double[]> map2 = mapIdMap.get(mapName);
                            log.info("map name {}", mapName);
                            if (map == null || map2 == null || map2.isEmpty()|| mapMap.get(mapName) == null) {
                                log.warn("Map null and continue? {}", mapName);
                                continue;
                            }
                            int outcomes = (int) map.values().stream().distinct().count();
                            outcomes = 4;
                            log.info("Outcomes {}", outcomes);
                            Callable callable = new MLClassifyLearnTestPredictCallable(nnConfigs, mldao, this, map, model, conf.getMACDDaysBeforeZero(), key, mapName, outcomes, mapTime, map2, labelMapShort);  
                            Future<LearnTestClassifyResult> future = MyExecutors.mlrun(callable);
                            futureList.add(future);
                            futureMap.put(future, new FutureMap(subType, model, mapType, resultMetaArray.size() - 1));
                        }
                    }
                }
            }
            for (Future<LearnTestClassifyResult> future: futureList) {
                FutureMap futMap = futureMap.get(future);
                MacdSubType subType = futMap.getSubType();
                MLClassifyModel model = futMap.getModel();
                String mapType = futMap.getMapType();
                int testCount = futMap.getTestCount();
                LearnTestClassifyResult result = future.get();
                Map<String, Double[]> classifyResult = result.getCatMap();
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                mapResult2.put(mapType, classifyResult);
                probabilityMap.put("" + model . getId() + key + subType + mapType, result.getAccuracy());
                handleResultMetaAccuracy(testCount, result);
                Map<String, Long> countMap2 = null;
                if (classifyResult != null) {
                    countMap2 = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                }
                if (countMap2 == null) {
                    continue;
                }
                addEventRow(subType, countMap2);
                Map<String, double[]> offsetMap = mapIdMap.get(subType.getType());
                handleResultMeta(testCount, offsetMap, countMap2);
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private void handleResultMetaAccuracy(int testCount, LearnTestClassifyResult result) {
        Object[] meta = resultMetaArray.get(testCount);
        ResultMeta resultMeta = getResultMetas().get(testCount);
        meta[6] = result.getAccuracy();
        resultMeta.setTestAccuracy(result.getAccuracy());
    }

    private void doLearnTestClassifyOld(NNConfigs nnconfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        try {
            doLearningAndTests(nnconfigs, conf, mapMap, labelMapShort);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Map<String, Map<String, double[]>> mapIdMap = getNewestPosNeg(labelMapShort);
        doClassifications(conf, mapMap, mapResult, labelMapShort, mapIdMap);
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

    private void handleOtherStats(MyMyConfig conf, Map<String, Map<double[], Double>> mapMap) {
        // and others done with println
        if (conf.wantOtherStats() && conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();            
            List<MacdSubType> subTypes = wantedSubTypes();
            for (MacdSubType subType : subTypes) {
                List<Integer> list = new ArrayList<>();
                list.add(POSTYPE);
                list.add(NEGTYPE);
                for (Integer type : list) {
                    String name = mapTypes.get(type);
                    String mapName = subType.getType() + name;
                    Map<double[], Double> myMap = mapMap.get(mapName);
                    if (myMap == null) {
                        log.error("map null {}", mapName);
                        continue;
                    }
                    Map<Double, Long> countMap = myMap.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
                    String counts = "";
                    for (Double label : countMap.keySet()) {
                        counts += labelMapShort.get(label) + " : " + countMap.get(label) + " ";
                    }
                    addEventRow(counts, subType.getName(), "");
                }
            }
        }
    }

    private void createResultMap(MyMyConfig conf,
            Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult) {
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            Object[] fields = new Object[fieldSize];
            momMap.put(id, momentum);
            resultMap.put(id, fields);
            if (momentum == null) {
                System.out.println("zero mom for id " + id);
            }
            int retindex = 0; //tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);

            // TODO make OO of this
            if (conf.wantML()) {
                Map<Double, String> labelMapShort2 = createLabelMapShort();
                //int momidx = 6;
                Double[] type;
                List<MacdSubType> subTypes2 = wantedSubTypes();
                for (MacdSubType subType : subTypes2) {
                    Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                    //System.out.println("mapget " + subType + " " + mapResult.keySet());
                    for (MLClassifyDao mldao : mldaos) {
                        for (MLClassifyModel model : mldao.getModels()) {
                            Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                            //for (int mapTypeInt : getMapTypeList()) {
                            //String mapType = mapTypes.get(mapTypeInt);
                            //Map<String, Double[]> mapResult3 = mapResult2.get(mapType);
                            //String mapName = subType.getType() + mapType;
                            //System.out.println("fields " + fields.length + " " + retindex);
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
                                //System.out.println("sizej "+retindex);
                            }
                            //}
                        }   
                    }
                }
            }
            //System.out.println("ri" + retindex);
            if (retindex != fieldSize) {
                log.error("Field size too small {} < {}", retindex, fieldSize);
            }
        }
    }

    private Map<String, Map<String, double[]>> getNewestPosNeg(Map<Double, String> labelMapShort) {
        // calculate sections and do ML
        // a map from h/m + com/neg/sub to map<id, values>
        Map<String, Map<String, double[]>> mapIdMap= new HashMap<>();
        for (Entry<String, Double[][]> entry : listMap.entrySet()) {
            Double[][] list = entry.getValue();
            Double[] origMain = Arrays.copyOf(list[0], list[0].length);
            list[0] = ArraysUtil.getPercentizedPriceIndex(list[0], key);
            Object[] objs = objectMap.get(entry.getKey());
            MInteger begOfArray = (MInteger) objs[TaUtil.MACDIDXBEG];
            MInteger endOfArray = (MInteger) objs[TaUtil.MACDIDXEND];
            Double[] trunclist = ArraysUtil.getSubExclusive(list[0], begOfArray.value, begOfArray.value + endOfArray.value);
            Double[] trunclistOrig = ArraysUtil.getSubExclusive(origMain, begOfArray.value, begOfArray.value + endOfArray.value);
            if (endOfArray.value == 0) {
                continue;
            }
            List<MacdSubType> subTypes = wantedSubTypes();
            for (MacdSubType subType : subTypes) {
                double[] aMacdArray = (double[]) objs[subType.getArrIdx()];
                getMlMappings(subType.getName(), subType.getType(), labelMapShort, mapIdMap, entry.getKey(), aMacdArray, endOfArray, trunclist, trunclistOrig);
            }
        }
        return mapIdMap;
    }

    private void doClassifications(MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap) {
        // map from h/m + posnegcom to map<model, results>
        List<MacdSubType> subTypes = wantedSubTypes();
        int testCount = 0;
        for (MacdSubType subType : subTypes) {
            Map<String, double[]> offsetMap = mapIdMap.get(subType.getType());
            Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
            for (MLClassifyDao mldao : mldaos) {
                // map from posnegcom to map<id, result>
                Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                for (MLClassifyModel model : mldao.getModels()) {
                    for (int mapTypeInt : getMapTypeList()) {
                        Map<String, Double[]> classifyResult = doClassifications(conf, mapMap, labelMapShort, mapIdMap,
                                subType, mldao, mapResult2, model, mapTypeInt);                        
                        Map<String, Long> countMap = null;
                        if (classifyResult != null) {
                            IndicatorUtils.filterNonExistingClassifications(labelMapShort, classifyResult);
                            countMap = classifyResult.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e[0]), Collectors.counting()));
                        }
                        if (countMap == null) {
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

    private Map<String, Double[]> doClassifications(MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap, MacdSubType subType,
            MLClassifyDao mldao, Map<String, Map<String, Double[]>> mapResult2, MLClassifyModel model, int mapTypeInt) {
        String mapType = mapTypes.get(mapTypeInt);
        String mapName = subType.getType() + mapType;
        Map<String, double[]> map = mapIdMap.get(mapName);
        log.info("map name {}", mapName);
        if (map == null || mapMap.get(mapName) == null) {
            log.error("map null and continue? {}", mapName);
            return null;
        }
        int outcomes = (int) map.values().stream().distinct().count();
        outcomes = 4;
        log.info("Outcomes {}", outcomes);
        Map<String, Double[]> classifyResult = mldao.classify(this, map, model, conf.getMACDDaysBeforeZero(), key, mapName, outcomes, labelMapShort, mapTime);
        mapResult2.put(mapType, classifyResult);
        return classifyResult;
    }

    private void handleResultMeta(int testCount, Map<String, double[]> offsetMap, Map<String, Long> countMap) {
        Object[] meta = resultMetaArray.get(testCount);
        meta[7] = countMap;
        meta[8] = offsetMap;
        ResultMeta resultMeta = getResultMetas().get(testCount);
        resultMeta.setClassifyMap(countMap);
    }

    private void addEventRow(MacdSubType subType, Map<String, Long> countMap) {
        StringBuilder counts = new StringBuilder();
        counts.append("classified ");
        for (Entry<String, Long> entry : countMap.entrySet()) {
            counts.append(entry.getKey() + " : " + entry.getValue() + " ");
        }
        addEventRow(counts.toString(), subType.getName(), "");
    }

    private void doLearningAndTests(NNConfigs nnConfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<Double, String> labelMapShort) {
        List<MacdSubType> subTypes = wantedSubTypes();
        for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {
                    for (int mapTypeInt : getMapTypeList()) {
                        String mapType = mapTypes.get(mapTypeInt);
                        String mapName = subType.getType() + mapType;
                        Map<double[], Double> map = mapMap.get(mapName);
                        if (map == null) {
                            log.error("map null {}", mapName);
                            continue;
                        }
                        int outcomes = (int) map.values().stream().distinct().count();
                        outcomes = 4;
                        log.info("Outcomes {}", outcomes);
                        Double testaccuracy = mldao.learntest(nnConfigs, this, map, model, conf.getMACDDaysBeforeZero(), key, mapName, outcomes, mapTime);  
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

    private Map<String, Map<double[], Double>> createPosNegMaps(MyMyConfig conf) {
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            Object[] objs = objectMap.get(id);
            Double[] momentum = momMap.get(id);
            if (momentum == null) {
                log.info("no macd for id {}", id);
            }
            MInteger begOfArray = (MInteger) objs[TaUtil.MACDIDXBEG];
            MInteger endOfArray = (MInteger) objs[TaUtil.MACDIDXEND];

            double[][] list = truncListMap.get(id);
            System.out.println("t " + Arrays.toString(list[0]));
            log.info("listsize {}", list.length);
            if (conf.wantPercentizedPriceIndex() && list[0].length > 0) {
                list[0] = ArraysUtil.getPercentizedPriceIndex(list[0], key, 0);
            }
            log.info("beg end {} {} {}", id, begOfArray.value, endOfArray.value);
            log.info("list {} {} ", list.length, Arrays.asList(list));
            double[] trunclist = ArrayUtils.subarray(list[0], begOfArray.value, begOfArray.value + endOfArray.value);
            log.info("trunclist {} {}", list.length, Arrays.asList(trunclist));
            if (conf.wantML()) {
                if (momentum != null && momentum[0] != null && momentum[1] != null && momentum[2] != null && momentum[3] != null) {
                    Map<String, Double> labelMap2 = createLabelMap2();
                    // TODO also macd
                    if (endOfArray.value > 0) {
                        List<MacdSubType> subTypes = wantedSubTypes();
                        for (MacdSubType subType : subTypes) {
                            double[] aMacdArray = (double[]) objs[subType.getArrIdx()];
                            Map<Integer, Integer>[] map = ArraysUtil.searchForward(aMacdArray, endOfArray.value);
                            getPosNegMap(mapMap, subType.getType(), CMNTYPESTR, POSTYPESTR, id, trunclist, labelMap2, aMacdArray, trunclist.length, map[0], labelFN, labelTN);
                            getPosNegMap(mapMap, subType.getType(), CMNTYPESTR, NEGTYPESTR, id, trunclist, labelMap2, aMacdArray, trunclist.length, map[1], labelTP, labelFP);
                        }
                    } else {
                        log.error("error arrayend 0");
                    }
                }
            }
        }
        return mapMap;
    }

    private void getMlMappings(String name, String subType, Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap,
            String id, double[] array, MInteger endOfArray,
            Double[] valueList, Double[] valueListOrig) {
        Map<String, double[]> offsetMap = mapGetter(mapIdMap, subType);
        Map<String, double[]> commonMap = mapGetter(mapIdMap, subType + CMNTYPESTR);
        Map<String, double[]> posMap = mapGetter(mapIdMap, subType + POSTYPESTR);
        Map<String, double[]> negMap = mapGetter(mapIdMap, subType + NEGTYPESTR);
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, endOfArray.value);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        printSignChange(name, id, newPos, newNeg, endOfArray.value, conf.getMACDDaysAfterZero(), labelMapShort);
        if (!newNeg.isEmpty() || !newPos.isEmpty()) {
            int start = 0;
            int end = 0;
            if (!newNeg.isEmpty()) {
                start = newNeg.keySet().iterator().next();
                end = newNeg.get(start);
            }
            if (!newPos.isEmpty()) {
                start = newPos.keySet().iterator().next();
                end = newPos.get(start);
            }
            if (end + 1 >= endOfArray.value) {
                return;
            }
            double[] doubleArray = new double[] { endOfArray.value - end };
            offsetMap.put(id, doubleArray);
            log.info("t {} {} {}", subType, id, valueListOrig[end]);
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            commonMap.put(id, truncArray);
            if (!newNeg.isEmpty()) {
                negMap.put(id, truncArray); 
            }
            if (!newPos.isEmpty()) {
                posMap.put(id, truncArray);
            }
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

    private boolean anythingHere2(Map<String, Double[]> listMap2) {
        for (Double[] array : listMap2.values()) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    static String labelTP = "TruePositive";
    static String labelFP = "FalsePositive";
    static String labelTN = "TrueNegative";
    static String labelFN = "FalseNegative";

    private void getPosNegMap(Map<String, Map<double[], Double>> mapMap, String subType, String commonType, String posnegType , String id,
            double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String label, String labelopposite) {
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
        for (Entry<Integer, Integer> entry : newPosNeg.entrySet()) {
            int end = entry.getValue();
            String textlabel;
            if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                textlabel = label;
            } else {
                textlabel = labelopposite;
            }
            log.info("{}: {} {} at {}", textlabel, id, ControlService.getName(id), end);
            printme(textlabel, end, list, array);
            double[] truncArray = ArraysUtil.getSub(array, entry.getKey(), end);
            Double doublelabel = labelMap2.get(textlabel);
            String commonMapName = subType + commonType;
            String posnegMapName = subType + posnegType;
            Map<double[], Double> commonMap = mapGetter(mapMap, commonMapName);
            Map<double[], Double> posnegMap = mapGetter(mapMap, posnegMapName);
            commonMap.put(truncArray, doublelabel);
            posnegMap.put(truncArray, doublelabel);
        }
    }

    static <K, V> Map<K, V> mapGetterOrig(Map<String, Map<K, V>> mapMap, String key) {
        Map<K, V> map = mapMap.get(key);
        if (map == null) {
            map = new HashMap<>();
            mapMap.put(key, map);
        }
        return map;
    }

    static <K, V> Map<K, V> mapGetter(Map<String, Map<K, V>> mapMap, String key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    public static void mapAdder(Map<MLClassifyModel, Long> map, MLClassifyModel key, Long add) {
        Long val = map.get(key);
        if (val == null) {
            val = Long.valueOf(0);
        }
        val += add;
        map.put(key, val);
    }

    private void printme(String label, int end, double[] values, double[] array) {
        StringBuilder me1 = new StringBuilder();
        StringBuilder me2 = new StringBuilder();
        for (int i = end - 3; i <= end + conf.getMACDDaysAfterZero(); i++) {
            me1.append(values[i] + " ");
            me2.append(array[i] + " ");
        }
        String m1 = me1.toString();
        String m2 = me2.toString();
        log.info("me1 {}", m1);
        log.info("me2 {}", m2);
    }

    public void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add("MLMACD " + key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }

    private void printSignChange(String txt, String id, Map<Integer, Integer> pos, Map<Integer, Integer> neg, int listsize, int daysAfterZero, Map<Double, String> labelMapShort) {
        if (!pos.isEmpty()) {
            int posmaxind = Collections.max(pos.keySet());
            int posmax = pos.get(posmaxind);
            if (posmax + 1 == listsize) {
                return;
            }
            if (posmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to negative since " + (listsize - posmax), ControlService.getName(id), id);
            }
        }
        if (!neg.isEmpty()) {
            int negmaxind = Collections.max(neg.keySet());
            int negmax = neg.get(negmaxind);
            if (negmax + 1 == listsize) {
                return;
            }
            if (negmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to positive since " + (listsize - negmax), ControlService.getName(id), id);
            }
        }
    }

    public static void printout(Double[] type, String id, Map<Double, String> labelMapShort) {
        if (type != null) {
            //System.out.println("Type " + labelMapShort.get(type[0]) + " id " + id);
        }
    }

    private Map<String, Double> createLabelMap2() {
        Map<String, Double> labelMap2 = new HashMap<>();
        labelMap2.put(labelTP, 1.0);
        labelMap2.put(labelFP, 2.0);
        labelMap2.put(labelTN, 3.0);
        labelMap2.put(labelFN, 4.0);
        return labelMap2;
    }

    private Map<Double, String> createLabelMap1() {
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

    @Override
    public Object calculate(double[] array) {
        TaUtil tu = new TaUtil();
        return tu.getMomAndDeltaFull(array, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
    }

    @Override
    public boolean isEnabled() {
        return conf.wantMLMACD();
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
            log.info("key {} {}", key, periodDataMap.keySet());
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

    private int getTitles(int retindex, Object[] objs) {
        // TODO make OO of this
        List<MacdSubType> subTypes = wantedSubTypes();
        for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {
                    List<Integer> typeList = getTypeList();
                    for (int mapTypeInt : typeList) {
                        String mapType = mapTypes.get(mapTypeInt);
                        String val = "";
                        // TODO workaround
                        try {
                            val = "" + MLClassifyModel.roundme((Double) probabilityMap.get("" + model . getId() + key + subType + mapType));
                            //val = "" + MLClassifyModel.roundme(mldao.eval(model . getId(), key, subType + mapType));
                        } catch (Exception e) {
                            log.error("Exception fix later, refactor", e);
                        }
                        objs[retindex++] = title + Constants.WEBBR +  subType.getType() + model.getName() + mapType + " " + val;
                        if (model.getReturnSize() > 1) {
                            objs[retindex++] = title + Constants.WEBBR +  subType.getType() + model.getName() + mapType + " prob ";
                        }
                        //retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
                    }
                }
            }
        }
        return retindex;
    }

    private int fieldSize() {
        int size = 0;
        List<MacdSubType> subTypes = wantedSubTypes();
        for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
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


    @Override
    public String getName() {
        return PipelineConstants.MLMACD;
    }

    private class FutureMap {
        private MacdSubType subType;

        private MLClassifyModel model;

        private String mapType;

        private int testCount;
        
        public FutureMap(MacdSubType subType, MLClassifyModel model, String mapType, int testCount) {
            super();
            this.subType = subType;
            this.model = model;
            this.mapType = mapType;
            this.testCount = testCount;
        }

        public MacdSubType getSubType() {
            return subType;
        }

        public void setSubType(MacdSubType subType) {
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


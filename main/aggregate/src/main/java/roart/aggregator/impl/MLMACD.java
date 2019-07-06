package roart.aggregator.impl;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.db.dao.DbDao;
import roart.executor.MyExecutors;
import roart.pipeline.Pipeline;
import roart.indicator.util.IndicatorUtils;
import roart.common.constants.Constants;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.common.MLClassifyModel;
import roart.model.StockItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.model.data.PeriodData;
import roart.talib.Ta;
import roart.talib.impl.TalibMACD;
import roart.talib.util.TaConstants;

public class MLMACD extends IndicatorAggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    Map<String, PeriodData> periodDataMap;
    String key;
    // save and return this map
    // need getters for this and not? buy/sell
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

    @Override
    protected Map<String, double[][]> getListMap() {
        return listMap;
    }

    private int fieldSize = 0;

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

    List<MLClassifyDao> mldaos = new ArrayList<>();

    private Map<String, String> idNameMap;

    public String getName(String id) {
        if (idNameMap == null) {
            return id;
        }
        String name = idNameMap.get(id);
        if (name == null) {
            name = id;
        }
        return name;
    }

    public MLMACD(MyMyConfig conf, String string, List<StockItem> stocks, Map<String, PeriodData> periodDataMap, 
            String title, int category, AbstractCategory[] categories, Map<String, String> idNameMap, Pipeline[] datareaders) throws Exception {
        super(conf, string, category);
        this.periodDataMap = periodDataMap;
        this.key = title;
        this.idNameMap = idNameMap;
        makeMapTypes();
        if (conf.wantML()) {
            if (conf.wantMLSpark()) {
                mldaos.add(new MLClassifyDao("spark", conf));
            }
            if (conf.wantMLTensorflow()) {
                mldaos.add(new MLClassifyDao("tensorflow", conf));
            }
        }
        if (conf.wantMLTimes()) {
            mlTimesTableRows = new ArrayList<>();
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        if (isEnabled()) {
            calculateMomentums(conf, periodDataMap, category, categories, datareaders);    
            cleanMLDaos();
        }
    }

    private void cleanMLDaos() {
        for (MLClassifyDao mldao : mldaos) {
            mldao.clean();
        }        
    }

    private abstract class MacdSubType extends SubType {
        public MacdSubType(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            this.listMap = (Map<String, Double[][]>) list;
            this.taMap = (Map<String, Object[]>) taObject;
            this.resultMap = (Map<String, Double[]>) resultObject;
            this.afterbefore = afterbefore;
            this.range = range;
            this.filters = new Filter[] { new Filter(true, 0, pos), new Filter(false, 0, neg) };
        }
    }

    private class MacdSubTypeHist extends MacdSubType {
        public MacdSubTypeHist(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
        }
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
            return TalibMACD.MACDIDXHIST;
        }
    }

    private class MacdSubTypeMacd extends MacdSubType {
        public MacdSubTypeMacd(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
        }
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
            return TalibMACD.MACDIDXMACD;
        }
    }

    private class MacdSubTypeSignal extends MacdSubType {
        public MacdSubTypeSignal(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
        }
        @Override
        public String getType() {
            return "S";
        }
        @Override
        public String getName() {
            return "Sig";
        }
        @Override
        public int getArrIdx() {
            return TalibMACD.MACDIDXSIGN;
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

    private List<SubType> wantedSubTypes = new ArrayList<>();

    @Override
    protected List<SubType> wantedSubTypes() {
        return wantedSubTypes;
    }

    private void makeWantedSubTypes(AbstractCategory cat, AfterBeforeLimit afterbefore) {
        Object list = cat.getResultMap().get(PipelineConstants.INDICATORMACDLIST);
        Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORMACDOBJECT);
        Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD).get(PipelineConstants.RESULT);
        if (conf.wantMLHist()) {
            wantedSubTypes.add(new MacdSubTypeHist(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
        }
        if (conf.wantMLMacd()) {
            wantedSubTypes.add(new MacdSubTypeMacd(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
        }
        if (conf.wantMLSignal()) {
            wantedSubTypes.add(new MacdSubTypeSignal(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
        }
    }
    // make an oo version of this
    private void calculateMomentums(MyMyConfig conf, Map<String, PeriodData> periodDataMap,
            int category2, AbstractCategory[] categories, Pipeline[] datareaders) throws Exception {
        AbstractCategory cat = IndicatorUtils.getWantedCategory(categories, category);
        if (cat == null) {
            return;
        }
        log.info("checkthis {}", category == cat.getPeriod());
        log.info("checkthis {}", title.equals(cat.getTitle()));
        log.info("checkthis {}", key.equals(title));
        //Object macd = cat.getResultMap().get(PipelineConstants.INDICATORMACDRESULT);
        //truncListMap = (Map<String, double[][]>) cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD).get(PipelineConstants.TRUNCBASE100LIST);
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        //this.listMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        //this.fillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.FILLLIST);
                //(Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCLIST);       
        /*
        this.truncFillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);       
        this.base100ListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.BASE100LIST);
        this.truncBase100ListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100LIST);       
        this.truncBase100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);       
*/
        Map<String, Double[][]> aListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        Map<String, double[][]> fillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);
        Map<String, double[][]> base100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);
        this.listMap = conf.wantPercentizedPriceIndex() ? base100FillListMap : fillListMap;
        
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        //for (SubType subType : wantedSubTypes) {
            if (!anythingHere(aListMap)) {
                log.debug("empty {}", key);
                return;
            }
        //}
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        probabilityMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();
        long time2 = System.currentTimeMillis();
        AfterBeforeLimit afterbefore = new AfterBeforeLimit(conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero());
        makeWantedSubTypes(cat, afterbefore);
        fieldSize = fieldSize();
        //log.debug("imap " + objectMap.size());
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        log.debug("listmap {} {}", listMap.size(), listMap.keySet());
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = createPosNegMaps(conf);
        // map from h/m to model to posnegcom map<model, results>
        Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        log.debug("Period {} {}", title, mapMap.keySet());
        String nnconfigString = conf.getMLMACDMLConfig();
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

    private void doLearnTestClassify(NeuralNetConfigs nnConfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        List<SubType> subTypes = wantedSubTypes();
        AfterBeforeLimit afterbefore = new AfterBeforeLimit(conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero());
        // map from h/m + posnegcom to map<model, results>
        int testCount = 0;
        try {
            for (SubType subType : subTypes) {
                Map<String, Map<String, double[]>> mapIdMap = getNewestPosNeg(labelMapShort, TaConstants.THREERANGE, afterbefore, subType.taMap);
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
                            Map<String, Long> countMap = null;
                            if (map != null) {
                                IndicatorUtils.filterNonExistingClassifications2(labelMapShort, map);
                                countMap = map.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e), Collectors.counting()));                            
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

                            Map<String, double[]> map2 = mapIdMap.get(mapName);
                            log.debug("map name {}", mapName);
                            if (map == null || map2 == null || map2.isEmpty()) {
                                log.error("map null and continue? {}", mapName);
                                continue;
                            }
                            int outcomes = (int) map.values().stream().distinct().count();
                            outcomes = 4;
                            log.debug("Outcomes {}", outcomes);
                            LearnTestClassifyResult result = mldao.learntestclassify(nnConfigs, this, map, model, conf.getMACDDaysBeforeZero(), key, mapName, outcomes, mapTime, map2, labelMapShort);  
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

    private void doLearnTestClassifyFuture(NeuralNetConfigs nnConfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        List<SubType> subTypes = wantedSubTypes();
        AfterBeforeLimit afterbefore = new AfterBeforeLimit(conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero());
        // map from h/m + posnegcom to map<model, results>
        List<Future<LearnTestClassifyResult>> futureList = new ArrayList<>();
        Map<Future<LearnTestClassifyResult>, FutureMap> futureMap = new HashMap<>();
        try {
            for (SubType subType : subTypes) {
                Map<String, Map<String, double[]>> mapIdMap = getNewestPosNeg(labelMapShort, TaConstants.THREERANGE, afterbefore, subType.taMap);
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
                            Map<String, Long> countMap = null;
                            if (map != null) {
				IndicatorUtils.filterNonExistingClassifications2(labelMapShort, map);
				countMap = map.values().stream().collect(Collectors.groupingBy(e -> labelMapShort.get(e), Collectors.counting()));
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

                            Map<String, double[]> map2 = mapIdMap.get(mapName);
                            log.debug("map name {}", mapName);
                            if (map == null || map2 == null || map2.isEmpty()) {
                                log.warn("Map null and continue? {}", mapName);
                                continue;
                            }
                            int outcomes = (int) map.values().stream().distinct().count();
                            outcomes = 4;
                            log.debug("Outcomes {}", outcomes);
                            Callable callable = new MLClassifyLearnTestPredictCallable(nnConfigs, mldao, this, map, model, conf.getMACDDaysBeforeZero(), key, mapName, outcomes, mapTime, map2, labelMapShort);  
                            Future<LearnTestClassifyResult> future = MyExecutors.run(callable, 1);
                            futureList.add(future);
                            futureMap.put(future, new FutureMap(subType, model, mapType, resultMetaArray.size() - 1, mapIdMap));
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
                Map<String, Map<String, double[]>> mapIdMap = futMap.mapIdMap;
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

    @Deprecated
    private void doLearnTestClassifyOld(NeuralNetConfigs nnconfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort) {
        try {
            doLearningAndTests(nnconfigs, conf, mapMap, labelMapShort);
        } catch (Exception e) {
            log.error("Exception", e);
        }
        AfterBeforeLimit afterbefore = new AfterBeforeLimit(conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero());
        Map<String, Map<String, double[]>> mapIdMap = getNewestPosNeg(labelMapShort, TaConstants.THREERANGE, afterbefore, null);
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
            List<SubType> subTypes = wantedSubTypes();
            for (SubType subType : subTypes) {
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
                List<SubType> subTypes2 = wantedSubTypes();
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

    private void doClassifications(MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<SubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult,
            Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap) {
        // map from h/m + posnegcom to map<model, results>
        List<SubType> subTypes = wantedSubTypes();
        int testCount = 0;
        for (SubType subType : subTypes) {
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

    private Map<String, Double[]> doClassifications(MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap, SubType subType,
            MLClassifyDao mldao, Map<String, Map<String, Double[]>> mapResult2, MLClassifyModel model, int mapTypeInt) {
        String mapType = mapTypes.get(mapTypeInt);
        String mapName = subType.getType() + mapType;
        Map<String, double[]> map = mapIdMap.get(mapName);
        log.debug("map name {}", mapName);
        if (map == null || mapMap.get(mapName) == null) {
            log.error("map null and continue? {}", mapName);
            return null;
        }
        int outcomes = (int) map.values().stream().distinct().count();
        outcomes = 4;
        log.debug("Outcomes {}", outcomes);
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

    private void addEventRow(SubType subType, Map<String, Long> countMap) {
        StringBuilder counts = new StringBuilder();
        counts.append("classified ");
        for (Entry<String, Long> entry : countMap.entrySet()) {
            counts.append(entry.getKey() + " : " + entry.getValue() + " ");
        }
        addEventRow(counts.toString(), subType.getName(), "");
    }

    private void doLearningAndTests(NeuralNetConfigs nnConfigs, MyMyConfig conf, Map<String, Map<double[], Double>> mapMap,
            Map<Double, String> labelMapShort) {
        List<SubType> subTypes = wantedSubTypes();
        for (SubType subType : subTypes) {
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
                        log.debug("Outcomes {}", outcomes);
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

    private boolean anythingHere(Map<String, Double[][]> listMap) {
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

    static <K, V> Map<K, V> mapGetterOrig(Map<String, Map<K, V>> mapMap, String key) {
        Map<K, V> map = mapMap.get(key);
        if (map == null) {
            map = new HashMap<>();
            mapMap.put(key, map);
        }
        return map;
    }

    public void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add("MLMACD " + key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }

    @Override
    protected void printSignChange(String txt, String id, Map<Integer, Integer> posneg, boolean positive, int listsize, int daysAfterZero, Map<Double, String> labelMapShort) {
        String pnString = positive ? "negative" : "positive";
        if (!posneg.isEmpty()) {
            int posnegmaxind = Collections.max(posneg.keySet());
            int posnegmax = posneg.get(posnegmaxind);
            if (posnegmax + 1 == listsize) {
                return;
            }
            if (posnegmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to " + (pnString) + " since " + (listsize - posnegmax), getName(id), id);
            }
        }
        /*
        if (!neg.isEmpty()) {
            int negmaxind = Collections.max(neg.keySet());
            int negmax = neg.get(negmaxind);
            if (negmax + 1 == listsize) {
                return;
            }
            if (negmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to positive since " + (listsize - negmax), getName(id), id);
            }
        }
        */
    }

    public static void printout(Double[] type, String id, Map<Double, String> labelMapShort) {
        if (type != null) {
            //log.debug("Type " + labelMapShort.get(type[0]) + " id " + id);
        }
    }

    @Override
    public Object calculate(double[][] array) {
        Ta tu = new TalibMACD();
        return tu.calculate(array);
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
        objs[retindex++] = title + Constants.WEBBR + "macd";
        if (conf.isMACDDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        objs[retindex++] = title + Constants.WEBBR + "sig";
        if (conf.isMACDSignalDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        retindex = getTitles(retindex, objs);
        log.debug("fieldsizet {}", retindex);
        return objs;
    }

    private int getTitles(int retindex, Object[] objs) {
        // make OO of this
        List<SubType> subTypes = wantedSubTypes();
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
        List<SubType> subTypes = wantedSubTypes();
        for (SubType subType : subTypes) {
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
        private SubType subType;

        private MLClassifyModel model;

        private String mapType;

        private int testCount;
        
        private Map<String, Map<String, double[]>> mapIdMap;
        
        public FutureMap(SubType subType, MLClassifyModel model, String mapType, int testCount, Map<String, Map<String, double[]>> mapIdMap) {
            super();
            this.subType = subType;
            this.model = model;
            this.mapType = mapType;
            this.testCount = testCount;
            this.mapIdMap = mapIdMap;
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


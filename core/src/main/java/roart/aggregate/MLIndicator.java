package roart.aggregate;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.tictactec.ta.lib.MInteger;

import roart.calculate.CalcNode;
import roart.category.Category;
import roart.category.CategoryConstants;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.db.DbAccess;
import roart.db.DbDao;
import roart.indicator.Indicator;
import roart.indicator.IndicatorUtils;
import roart.ml.MLClassifyDao;
import roart.ml.MLClassifyModel;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.PipelineConstants;
import roart.service.ControlService;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;
import scala.collection.mutable.WrappedArray;

public class MLIndicator extends Aggregator {
/*
    public MLMACD(MyMyConfig conf, String string, int category) {
        super(conf, string, category);
        // TODO Auto-generated constructor stub
    }
*/
    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    String key;
    Map<String, Double[]> listMap;
    // TODO save and return this map
    // TODO need getters for this and not? buy/sell
    Map<String, Object[]> objectMap;
    //Map<String, Double> resultMap;
    Map<String, Object[]> resultMap;
    /*
    Map<String, Double[]> momMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    */
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();
    
    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;
    
    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        //map.put("MACD", momMap);
        return map;
    }
    
    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }
    
    public Map<String, Double[]> getListMap() {
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

    public MLIndicator(MyMyConfig conf, String string, List<StockItem> stocks, Map<String, MarketData> marketdatamap, 
            Map<String, PeriodData> periodDataMap, /*Map<String, Integer>[] periodmap,*/ String title, int category, Category[] categories/*, Indicator[] indicators*/) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
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
        calculateMomentums(conf, marketdatamap, periodDataMap, category, categories);        
    }

    private abstract class MacdSubType {
        public abstract  String getType();
        public abstract  String getName();
        public abstract  int getArrIdx();
    }
    
    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }
    
    @Override
    public List<Integer> getTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(0);
        return retList;
    }
    
    private Map<Integer, String> mapTypes = new HashMap<>();
    
    private List<MacdSubType> wantedSubTypes = new ArrayList<>();
    
   private List<MacdSubType> wantedSubTypes() {
          return wantedSubTypes;
    }
  
   public Map<double[], Double> getEvaluations(MyMyConfig conf, int j, Object retObj[]) throws JsonParseException, JsonMappingException, IOException {
       int listlen = conf.getTableDays();
       List<Map<String, Double[]>> listList = (List<Map<String, Double[]>>) retObj[2];
       Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj[0];
       //List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
       Map<String, Double[]> indicatorMap = dayIndicatorMap.get(j);
       // find recommendations
       double recommend = 0;
       //transform(conf, buyList);
       if (indicatorMap == null) {
           return new HashMap<>();
       }
       Map<double[], Double> retMap = new HashMap<>();
       for (String id : indicatorMap.keySet()) {
           int newlistidx = listlen - 1 - j + conf.getAggregatorsIndicatorFuturedays();
           int curlistidx = listlen - 1 - j;
           Double[] list = listList.get(0).get(id);
           if (list[newlistidx] == null || list[curlistidx] == null) {
               continue;
           }
           double change = list[newlistidx]/list[curlistidx] - 1;
           double[] merged = ArraysUtil.convert(indicatorMap.get(id));
           
           // cat 1.0 is for >= threshold, 2.0 is for belov
           Double cat = 2.0;
           if (change > conf.getAggregatorsIndicatorThreshold()) {
               cat = 1.0;
           }
           retMap.put(merged, cat);
           /*
           for (int i = 0; i < keys.size(); i++) {
               String key = keys.get(i);
               // TODO temp fix
               CalcNode node = (CalcNode) conf.configValueMap.get(key);
               //node.setDoBuy(useMax);
               double value = momrsi[i];
               recommend += node.calc(value, 0) * change;
           }
           */
       }
       return retMap;
   }

    // TODO make an oo version of this
    private void calculateMomentums(MyMyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category2, Category[] categories) throws Exception {
        Category cat = null;
        for (Category category : categories) {
            // TODO fix
            if (category.getTitle().equals(CategoryConstants.PRICE)) {
                cat = category;
                break;
            }
        }
        Map<String, Indicator> newIndicatorMap = new HashMap<>();
        Map<String, Indicator> usedIndicatorMap = cat.getIndicatorMap();

        Map<String, List<AggregatorMLIndicator>> usedIndicators = AggregatorMLIndicator.getUsedAggregatorMLIndicators(conf);
        Set<String> ids = new HashSet<>();
        Map<String, Map<String, Object>> localResultMap = cat.getIndicatorLocalResultMap();
        Map<String, Double[]> list0 = (Map<String, Double[]>) localResultMap.get(localResultMap.keySet().iterator().next()).get(PipelineConstants.LIST);
        ids.addAll(list0.keySet());
        //AggregatorMLIndicator.getI
        TaUtil tu = new TaUtil();
        Map<String, Indicator> indicatorMap = new HashMap<>();
       for (String type : usedIndicators.keySet()) {
            List<AggregatorMLIndicator> list = usedIndicators.get(type);
            for (AggregatorMLIndicator ind : list) {
                String indicator = ind.indicator();
                if (indicator != null) {
                indicatorMap.put(indicator, ind.getIndicator(marketdatamap, category, newIndicatorMap, usedIndicatorMap));
                }
                // TODO fix
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                ids.retainAll(aResult.keySet());
            }
        }
       List<Indicator> indicators = new ArrayList<>(indicatorMap.values());

        DbAccess dbDao = DbDao.instance(conf);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        this.listMap = (Map<String, Double[]>) list0;
        if (conf.wantPercentizedPriceIndex()) {
            
        }
        if (!anythingHere(listMap)) {
            System.out.println("empty"+key);
            return;
        }
        log.info("time0 " + (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        objectMap = new HashMap<>();
        /*
        momMap = new HashMap<>();
        buyMap = new HashMap<>();
        sellMap = new HashMap<>();
        */
        /*
        List<Double> macdLists[] = new ArrayList[4];
        for (int i = 0; i < 4; i ++) {
            macdLists[i] = new ArrayList<>();
        }
        */
        long time2 = System.currentTimeMillis();
        //System.out.println("imap " + objectMap.size());
        log.info("time2 " + (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();

        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
       // System.out.println("allids " + listMap.size());

        //Map<String, Double[]> list0 = (Map<String, Double[]>) cat.getResultMap().get("LIST");

        //Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
        //Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
        //Map<String, Indicator> indicatorMap = new HashMap<>();
        int category = Constants.PRICECOLUMN;
        //Set<String> ids = new HashSet<>();
        ids.addAll(list0.keySet());
        List<String> keys = new ArrayList<>();
        /*
        for (String type : usedRecommenders.keySet()) {
            List<Recommend> list = usedRecommenders.get(type);
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                // TODO fix
                Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicator).get(PipelineConstants.LIST);
                //Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getResultMap().get(indicator);
                ids.retainAll(aResult.keySet());
            }
        }
        */
        Map<String, Object[]> result = new HashMap<>();
        for (String id : ids) {
            Object[] arrayResult = new Object[0];
            for (Indicator indicator : indicators) {
                String indicatorName = indicator.indicatorName();
                    // TODO fix
                    Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getIndicatorLocalResultMap().get(indicatorName).get(PipelineConstants.LIST);
                   //Map<String, Object[]> aResult = (Map<String, Object[]>) cat.getResultMap().get(indicator);
                    arrayResult = (Object[]) ArrayUtils.addAll(arrayResult, aResult.get(id));
            }
            result.put(id, arrayResult);
        }

        int macdlen = conf.getTableDays();
        int listlen = conf.getTableDays();
        double testRecommendQualBuySell = 0;
        Object[] retObj2 = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, conf.getAggregatorsIndicatorFuturedays(), conf.getTableDays(), conf.getAggregatorsIndicatorIntervaldays());
        Map<Integer, Map<String, Double[]>> dayIndicatorMap = (Map<Integer, Map<String, Double[]>>) retObj2[0];
        Map<double[], Double> mergedCatMap = new HashMap<>();
        for (int j = conf.getAggregatorsIndicatorFuturedays(); j < macdlen; j += conf.getAggregatorsIndicatorIntervaldays()) {
            //List<Double> macdLists[] = macdMinMax.get(j);
            //int newmacdidx = macdlen - 1 - j + conf.getTestRecommendFutureDays();
            //int curmacdidx = macdlen - 1 - j;
            Map<String, Double[]> momrsiMap = dayIndicatorMap.get(j);
           //System.out.println("j"+j);
            Map<double[], Double> retMap = getEvaluations(conf, j, retObj2);
            mergedCatMap.putAll(retMap);
            int newlistidx = listlen - 1 - j + conf.getAggregatorsIndicatorFuturedays();
            int curlistidx = listlen - 1 - j;
            //testRecommendQualBuySell += MACDRecommend.getQuality(buy, buysellMap, listMap, curlistidx, newlistidx);
        }
   
        
        // map from h/m to model to posnegcom map<model, results>
        Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        log.info("Period " + title + " " + mapMap.keySet());
        if (conf.wantML()) {
            int arrayLength = mergedCatMap.keySet().iterator().next().length;
            for(double[] array : mergedCatMap.keySet()) {
                if (array.length != arrayLength) {
                    System.out.println("diff length " + arrayLength + " " + array.length);
                }
            }
            Map<Double, String> labelMapShort = createLabelMapShort();
            try {
                //List<MacdSubType> subTypes = wantedSubTypes();
                    for (MLClassifyDao mldao : mldaos) {
                            //String mapType = mapTypes.get(null);
                            //String mapName = mapType;
                            //System.out.println("mapget " + mapName + " " + mapMap.keySet());
                            Map<double[], Double> map = mergedCatMap;
                            System.out.println(map.values());
                            mldao.learntest(this, map, null, arrayLength, key, "mapName", 2, mapTime);  
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            // calculate sections and do ML
            // a map from h/m + com/neg/sub to map<id, values>
            System.out.println(dayIndicatorMap.keySet());
            Map<String, Double[]> indicatorMap2 = dayIndicatorMap.get(conf.getAggregatorsIndicatorFuturedays());
            Map<String, double[]> indicatorMap3 = new HashMap<>();
            for (String id : indicatorMap2.keySet()) {
                indicatorMap3.put(id, ArraysUtil.convert(indicatorMap2.get(id)));
            }
            // map from h/m + posnegcom to map<model, results>
            List<MacdSubType> subTypes = wantedSubTypes();
            for (MacdSubType subType : subTypes) {
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
                for (MLClassifyDao mldao : mldaos) {
                    // map from posnegcom to map<id, result>
                    Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                    for (MLClassifyModel model : mldao.getModels()) {
                        Map<String, double[]> map = indicatorMap3;
                            //log.info("map name " + mapName);
                            if (map == null) {
                                log.error("map null ");
                                continue;
                            } else {
                                log.info("keyset " + map.keySet());
                            }
                            Map<String, Double[]> classifyResult = mldao.classify(this, map, model, arrayLength, key, null, 2, labelMapShort, mapTime);
                            //mapResult2.put(mapType, classifyResult);
                         //mapResult1.put(model, mapResult2);
                    }
                }
                mapResult.put(subType, mapResult1);
            }
        }
        List<Map> maplist = new ArrayList<>();
        for (String id : listMap.keySet()) {
            //Double[] momentum = momMap.get(id);
            Object[] objs = objectMap.get(id);
            Object[] fields = new Object[fieldSize];
            //momMap.put(id, momentum);
            resultMap.put(id, fields);
            /*
            if (momentum == null) {
                System.out.println("zero mom for id " + id);
            }
            */
            int retindex = 0 ; //tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);
            
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
                                retindex = mldao.addResults(fields, retindex, id, model, this, mapResult2, labelMapShort2);
                            //}
                        }   
                    }
                }
            }
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
        // and others done with println
        if (conf.wantOtherStats() && conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();            
            List<MacdSubType> subTypes = wantedSubTypes();
            for (MacdSubType subType : subTypes) {
                List<Integer> list = new ArrayList<>();
                //list.add(POSTYPE);
                //list.add(NEGTYPE);
                for (Integer type : list) {
                    /*
                    String name = mapTypes.get(null);
                    String mapName = subType.getType() + name;
                    Map<double[], Double> myMap = mapMap.get(mapName);
                    if (myMap == null) {
                        log.error("map null " + mapName);
                        continue;
                    }
                    Map<Double, Long> countMap = myMap.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));
                    String counts = "";
                    for (Double label : countMap.keySet()) {
                        counts += labelMapShort.get(label) + " : " + countMap.get(label) + " ";
                    }
                    addEventRow(counts, subType.getName(), "");
*/                
                }
            }
        }
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

    private void getMlMappings(String name, Map<Double, String> labelMapShort, Map<String, double[]> commonMap,
            Map<String, double[]> posMap, Map<String, double[]> negMap, String id, double[] array, MInteger endOfArray,
            Double[] valueList) {
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, endOfArray.value);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        //System.out.println("negpos " + newNeg.size() + " " + newPos.size());
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
/*
    private void getMlMappings(String name, String subType, Map<Double, String> labelMapShort, Map<String, Map<String, double[]>> mapIdMap,
            String id, double[] array, MInteger endOfArray,
            Double[] valueList) {
        Map<String, double[]> commonMap = mapGetter(mapIdMap, subType + CMNTYPESTR);
        Map<String, double[]> posMap = mapGetter(mapIdMap, subType + POSTYPESTR);
        Map<String, double[]> negMap = mapGetter(mapIdMap, subType + NEGTYPESTR);
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, endOfArray.value);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), valueList.length);
        //System.out.println("negpos " + newNeg.size() + " " + newPos.size());
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
*/
    private boolean anythingHere(Map<String, Double[]> listMap2) {
        for (Double[] array : listMap2.values()) {
            for (int i = 0; i < array.length; i++) {
                if (array[i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getPosMap(Map<double[], Double> commonMap, Map<double[], Double> posMap, String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer>[] map) {
        Map<Integer, Integer> pos = map[0];
        //System.out.println("Checking " + key + " " + id + " " + listsize + " " + histarr.length);
        if (list.length == 0) {
            //System.out.println("h " + Arrays.asList( histarr));
        }
        Map<Integer, Integer> newPos = ArraysUtil.getAcceptedRanges(pos, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
        for (int start : newPos.keySet()) {
            int end = newPos.get(start);
            String label = null;
            try {
                if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                    label = labelFN;
                    log.info(labelFN + ": " + id + " " + ControlService.getName(id) + " at " + end);
                    printme(label, end, list, array);
                } else {
                    label = labelTN;
                    log.info(labelTN + ": " + id + " " + ControlService.getName(id) + " at " + end);
                    printme(label, end, list, array);
                }
            } catch (Exception e) {
                log.error("myexcept " + pos + " : " + newPos + " " + start + " " + end + " " + list.length + " " + array.length, e);
            }
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double label2 = labelMap2.get(label);
            commonMap.put(truncArray, label2);
            posMap.put(truncArray, label2);
        }
    }

    static String labelTP = "TruePositive";
    static String labelFP = "FalsePositive";
    static String labelTN = "TrueNegative";
    static String labelFN = "FalseNegative";

    private void getNegMap(Map<double[], Double> commonMap, Map<double[], Double> negMap, String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer>[] map) {
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getAcceptedRanges(neg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
        for (int start : newNeg.keySet()) {
            int end = newNeg.get(start);
            String label;
            if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                label = labelTP;
                log.info("TruePositive" + ": " + id + " " + ControlService.getName(id) + " at " + end);
                printme(label, end, list, array);
            } else {
                label = labelFP;
                log.info(labelFP + ": " + id + " " + ControlService.getName(id) + " at " + end);
                printme(label, end, list, array);
            }
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double label2 = labelMap2.get(label);
            commonMap.put(truncArray, label2);
            negMap.put(truncArray, label2);
        }
    }

    private void getPosNegMap(Map<double[], Double> commonMap, Map<double[], Double> posnegMap, String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String label, String labelopposite) {
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
        for (int start : newPosNeg.keySet()) {
            int end = newPosNeg.get(start);
            String textlabel;
            if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                textlabel = label;
            } else {
                textlabel = labelopposite;
            }
            log.info(textlabel + ": " + id + " " + ControlService.getName(id) + " at " + end);
            printme(textlabel, end, list, array);
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double doublelabel = labelMap2.get(textlabel);
            commonMap.put(truncArray, doublelabel);
            posnegMap.put(truncArray, doublelabel);
        }
    }

    private void getPosNegMap(Map<String, Map<double[], Double>> mapMap, String subType, String commonType, String posnegType , String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer> posneg, String label, String labelopposite) {
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero(), listsize);
    //System.out.println("pnmap " + newPosNeg.keySet());
        for (int start : newPosNeg.keySet()) {
            int end = newPosNeg.get(start);
            String textlabel;
            if (list[end] < list[end + conf.getMACDDaysAfterZero()]) {
                textlabel = label;
            } else {
                textlabel = labelopposite;
            }
            log.info(textlabel + ": " + id + " " + ControlService.getName(id) + " at " + end);
            printme(textlabel, end, list, array);
            double[] truncArray = ArraysUtil.getSub(array, start, end);
            Double doublelabel = labelMap2.get(textlabel);
            String commonMapName = subType + commonType;
            String posnegMapName = subType + posnegType;
            Map<double[], Double> commonMap = mapGetter(mapMap, commonMapName);
            Map<double[], Double> posnegMap = mapGetter(mapMap, posnegMapName);
            commonMap.put(truncArray, doublelabel);
            posnegMap.put(truncArray, doublelabel);
        }
    }

    private <K, V> Map<K, V> mapGetter(Map<String, Map<K, V>> mapMap, String key) {
        Map<K, V> map = mapMap.get(key);
        if (map == null) {
            map = new HashMap<>();
            //System.out.println("mapput " + key);
            mapMap.put(key, map);
        }
        return map;
    }

    public static void mapAdder(Map<MLClassifyModel, Long> map, MLClassifyModel key, Long add) {
        Long val = map.get(key);
        if (val == null) {
            val = new Long(0);
        }
        val += add;
        map.put(key, val);
    }

    private void printme(String label, int end, Double[] values, double[] array) {
        String me1 = "";
        String me2 = "";
        for (int i = end - 3; i <= end + conf.getMACDDaysAfterZero(); i++) {
            me1 = me1 + values[i] + " ";
            me2 = me2 + array[i] + " ";
        }
        log.info("me1 " + me1);
        log.info("me2 " + me2);
    }

    public void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add(key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }
    
    private void printSignChange(String txt, String id, Map<Integer, Integer> pos, Map<Integer, Integer> neg, int listsize, int daysAfterZero, Map<Double, String> labelMapShort) {
        if (!pos.isEmpty()) {
            int posmaxind = Collections.max(pos.keySet());
            int posmax = pos.get(posmaxind);
            //System.out.println("truncls " + posmax + " " +  " " + listsize);
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

    public static Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, "Inc");
        labelMap1.put(2.0, "Dec");
        return labelMap1;
    }

    @Override
    public Object calculate(double[] array) {
        TaUtil tu = new TaUtil();
        Object[] objs = tu.getMomAndDeltaFull(array, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
        return objs;
    }

    @Override
    public boolean isEnabled() {
        return conf.isMACDEnabled();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new Pair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        if (perioddata == null) {
            //System.out.println("key " + key + " : " + periodDataMap.keySet());
            log.info("key " + key + " : " + periodDataMap.keySet());
        }
        //double momentum = resultMap.get(id);
        Object[] result = resultMap.get(id);
        if (result == null) {
            /*
            Double[] i = resultMap.values().iterator().next();
            int size = i.length;
            momentum = new Double[size];
             */
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
        // TODO make OO of this
        List<MacdSubType> subTypes = wantedSubTypes();
        for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                //for (int mapTypeInt : getMapTypeList()) {
                    //String mapType = mapTypes.get(mapTypeInt);
                    //String mapName = subType.getType() + mapType;
                    retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
                //}
            }
        }
        //emptyField = new Double[size];
        log.info("fieldsizet " + retindex);
        return objs;
    }

    private int fieldSize() {
        int size = 0;
        List<MacdSubType> subTypes = wantedSubTypes();
        //for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
        //}
        emptyField = new Object[size];
        log.info("fieldsizet " + size);
        return size;
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        // TODO Auto-generated method stub
        
    }
    
}


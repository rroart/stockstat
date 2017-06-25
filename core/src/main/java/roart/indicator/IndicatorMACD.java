package roart.indicator;

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

import org.apache.commons.math3.util.Pair;

import com.tictactec.ta.lib.MInteger;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.db.DbAccess;
import roart.db.DbDao;
import roart.db.DbSpark;
import roart.ml.MLClassifyDao;
import roart.ml.MLClassifyModel;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.recommender.MACDRecommend;
import roart.service.ControlService;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;
import scala.collection.mutable.WrappedArray;

public class IndicatorMACD extends Indicator {

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
    Map<String, Double[]> momMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();
    
    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;
    
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

    public IndicatorMACD(MyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
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
            Object[] objs = new Object[fieldSize];
            int retindex = 0;
            objs[retindex++] = "";
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        calculateMomentums(conf, marketdatamap, periodDataMap, category);        
    }

    private abstract class MacdSubType {
        public abstract  String getType();
        public abstract  String getName();
        public abstract  int getArrIdx();
    }
    
    private class MacdSubTypeHist extends MacdSubType {
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
            return TaUtil.IDXHIST;
        }
    }
    
    private class MacdSubTypeMacd extends MacdSubType {
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
            return TaUtil.IDXMACD;
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
    private int CMNTYPE = 0;
    private int NEGTYPE = 1;
    private int POSTYPE = 2;
    private String CMNTYPESTR = "cmn";
    private String NEGTYPESTR = "neg";
    private String POSTYPESTR = "pos";
    
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
    private void calculateMomentums(MyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category) throws Exception {
        DbAccess dbDao = DbDao.instance(conf);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        this.listMap = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap);
        if (conf.wantPercentizedPriceIndex()) {
            
        }
        if (!anythingHere(listMap)) {
            System.out.println("empty"+key);
            return;
        }
        log.info("time0 " + (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        objectMap = new HashMap<>();
        momMap = new HashMap<>();
        buyMap = new HashMap<>();
        sellMap = new HashMap<>();
        List<Double> macdLists[] = new ArrayList[4];
        for (int i = 0; i < 4; i ++) {
            macdLists[i] = new ArrayList<>();
        }
        /*
        List<Double> macdList = new ArrayList<>();
        List<Double> histList = new ArrayList<>();
        List<Double> macdDList = new ArrayList<>();
        List<Double> histDList = new ArrayList<>();
        */
        long time2 = System.currentTimeMillis();
        objectMap = dbDao.doCalculationsArr(conf, listMap, key, this, conf.wantPercentizedPriceIndex());
        //System.out.println("imap " + objectMap.size());
        log.info("time2 " + (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        log.info("listmap " + listMap.size() + " " + listMap.keySet());
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
       // System.out.println("allids " + listMap.size());
        for (String id : listMap.keySet()) {
            Object[] objs = objectMap.get(id);
            Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs);
            if (momentum != null) {
                momMap.put(id, momentum);
                // TODO and continue?
            } else {
                System.out.println("no macd for id" + id);
            }
            Map<String, List<Double>> retMap = new HashMap<>();
            //Object[] full = tu.getMomAndDeltaFull(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());
            {
                double[] macd = (double[]) objs[TaUtil.IDXMACD];
                double[] hist = (double[]) objs[TaUtil.IDXHIST];
                log.info("outout1 " + Arrays.toString(macd));
                log.info("outout3 " + Arrays.toString(hist));
            }
            MInteger begOfArray = (MInteger) objs[TaUtil.IDXBEG];
            MInteger endOfArray = (MInteger) objs[TaUtil.IDXEND];

            Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            log.info("listsize"+ list.length);
            if (conf.wantPercentizedPriceIndex()) {
            list = ArraysUtil.getPercentizedPriceIndex(list, key);
            }
            log.info("beg end " + id + " "+ begOfArray.value + " " + endOfArray.value);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.info("list " + list.length + " " + Arrays.asList(list));
            Double[] trunclist = ArraysUtil.getSubExclusive(list, begOfArray.value, begOfArray.value + endOfArray.value);
            log.info("trunclist" + list.length + " " + Arrays.asList(trunclist));
            if (conf.wantScore() && momentum != null) {
                MACDRecommend.addToLists(marketdatamap, category, macdLists /*macdList, histList, macdDList, histDList*/, market, momentum);
            }
            if (conf.wantML()) {
                if (momentum != null && momentum[0] != null && momentum[1] != null && momentum[2] != null && momentum[3] != null) {
                    Map<String, Double> labelMap2 = createLabelMap2();
                    //List<Double> list = listMap.get(id);
                    //Double[] momentum = resultMap.get(id);
                     double hist = momentum[0];
                    // TODO also macd
                    double macd = momentum[2];
                    //for (int i = trunclist.size(); i >=0 ; i --) {
                    //for (String id : listMap.keySet()) {
                    //int trunclistsize = trunclist.length;
                    if (endOfArray.value > 0) {
                        List<MacdSubType> subTypes = wantedSubTypes();
                        for (MacdSubType subType : subTypes) {
                            double[] aMacdArray = (double[]) objs[subType.getArrIdx()];
                            //System.out.println("arrlen " + aMacdArray.length);
                            Map<Integer, Integer>[] map = ArraysUtil.searchForward(aMacdArray, endOfArray.value);
                            getPosNegMap(mapMap, subType.getType(), CMNTYPESTR, POSTYPESTR, id, trunclist, labelMap2, aMacdArray, trunclist.length, map[0], labelFN, labelTN);
                            getPosNegMap(mapMap, subType.getType(), CMNTYPESTR, NEGTYPESTR, id, trunclist, labelMap2, aMacdArray, trunclist.length, map[1], labelTP, labelFP);
                       }
                    } else {
                        //log.error("error " + subType.getType());
                    }
                }
            }
        }

        if (conf.wantScore()) {
            //log.info("histlist " + histList);
            List<String> buyList = new MACDRecommend().getBuyList();
            List<String> sellList = new MACDRecommend().getSellList();
            MACDRecommend.getBuySellRecommendations(buyMap, sellMap, conf, macdLists, listMap, momMap, buyList, sellList /*macdList, histList, macdDList, histDList*/);
        }
        // map from h/m to model to posnegcom map<model, results>
        Map<MacdSubType, Map<MLClassifyModel, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        log.info("Period " + title + " " + mapMap.keySet());
        if (conf.wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();
            try {
                List<MacdSubType> subTypes = wantedSubTypes();
                for (MacdSubType subType : subTypes) {
                    for (MLClassifyDao mldao : mldaos) {
                        for (int mapTypeInt : getMapTypeList()) {
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            //System.out.println("mapget " + mapName + " " + mapMap.keySet());
                            Map<double[], Double> map = mapMap.get(mapName);
                            if (map == null) {
                                log.error("map null " + mapName);
                                continue;
                            }
                            mldao.learntest(this, map, null, conf.getMACDDaysBeforeZero(), key, mapName, 4, mapTime);  
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            // calculate sections and do ML
            // a map from h/m + com/neg/sub to map<id, values>
            Map<String, Map<String, double[]>> mapIdMap= new HashMap<>();
            for (String id : listMap.keySet()) {
                Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
                list = ArraysUtil.getPercentizedPriceIndex(list, key);
                Object[] objs = objectMap.get(id);
                double[] macdarr = (double[]) objs[0];
                double[] histarr = (double[]) objs[2];
                MInteger begOfArray = (MInteger) objs[TaUtil.IDXBEG];
                MInteger endOfArray = (MInteger) objs[TaUtil.IDXEND];
                //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
                Double[] trunclist = ArraysUtil.getSubExclusive(list, begOfArray.value, begOfArray.value + endOfArray.value);
                //System.out.println("trunc " + list.length + " " + trunclist.length);
                int trunclistsize = trunclist.length; //endOfArray.value;
                if (endOfArray.value == 0) {
                    continue;
                }
                List<MacdSubType> subTypes = wantedSubTypes();
                for (MacdSubType subType : subTypes) {
                    double[] aMacdArray = (double[]) objs[subType.getArrIdx()];
                    getMlMappings(subType.getName(), subType.getType(), labelMapShort, mapIdMap, id, aMacdArray, endOfArray, trunclist);
                }
                /*
                if (wantMLMacd()) {
                    getMlMappings("Macd", labelMapShort, commonIdMacdMap, posIdMacdMap, negIdMacdMap, id, aMacdArray, endOfArray, trunclist);
                }
                 */
            }
         /*
            for (MLDao mldao : mldaos) {
                int model = 0;
            if (wantMLHist()) {
                    commonIdTypeModelHistMap = mldao.classify(commonIdHistMap, model, getDaysBeforeZero(), key, "common", 4, labelMapShort);
                    posIdTypeModelHistMap = mldao.classify(posIdHistMap, model, getDaysBeforeZero(), key, "pos", 4, labelMapShort);
                    negIdTypeModelHistMap = mldao.classify(negIdHistMap, model, getDaysBeforeZero(), key, "neg", 4, labelMapShort);
            }
            if (wantMLMacd()) {
                    commonIdTypeModelMacdMap = mldao.classify(commonIdMacdMap, model, getDaysBeforeZero(), key, "commonM", 4, labelMapShort);
                    posIdTypeModelMacdMap = mldao.classify(posIdMacdMap, model, getDaysBeforeZero(), key, "posM", 4, labelMapShort);
                    negIdTypeModelMacdMap = mldao.classify(negIdMacdMap, model, getDaysBeforeZero(), key, "negM", 4, labelMapShort);
                }
            }
            */
            // map from h/m + posnegcom to map<model, results>
            List<MacdSubType> subTypes = wantedSubTypes();
            for (MacdSubType subType : subTypes) {
                Map<MLClassifyModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
                for (MLClassifyDao mldao : mldaos) {
                    // map from posnegcom to map<id, result>
                    Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                    for (MLClassifyModel model : mldao.getModels()) {
                        for (int mapTypeInt : getMapTypeList()) {
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            Map<String, double[]> map = mapIdMap.get(mapName);
                            log.info("map name " + mapName);
                            if (mapMap.get(mapName) == null) {
                                log.error("map null and continue? " + mapName);
                                continue;
                            }
                            if (map == null) {
                                log.error("map null " + mapName);
                                continue;
                            } else {
                                log.info("keyset " + map.keySet());
                            }
                            Map<String, Double[]> classifyResult = mldao.classify(this, map, model, conf.getMACDDaysBeforeZero(), key, mapName, 4, labelMapShort, mapTime);
                            mapResult2.put(mapType, classifyResult);
                        }
                        mapResult1.put(model, mapResult2);
                    }
                }
                mapResult.put(subType, mapResult1);
            }
        }
        List<Map> maplist = new ArrayList<>();
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            Object[] objs = objectMap.get(id);
            Object[] fields = new Object[fieldSize];
            momMap.put(id, momentum);
            resultMap.put(id, fields);
            if (momentum == null) {
                System.out.println("zero mom for id " + id);
            }
            int retindex = tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);

            if (conf.wantScore()) {
                Double buy = buyMap.get(id);
                fields[retindex++] = buy;
                Double sell = sellMap.get(id);
                fields[retindex++] = sell;
            }
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
                list.add(POSTYPE);
                list.add(NEGTYPE);
                for (Integer type : list) {
                    String name = mapTypes.get(type);
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
        labelMap1.put(1.0, "TP");
        labelMap1.put(2.0, "FP");
        labelMap1.put(3.0, "TN");
        labelMap1.put(4.0, "FN");
        return labelMap1;
    }

    @Override
    public Object calculate(Double[] array) {
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
        if (conf.wantScore()) {
            objs[retindex++] = title + Constants.WEBBR + "buy";
            objs[retindex++] = title + Constants.WEBBR + "sell";
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
        int size = 2;
        if (conf.isMACDDeltaEnabled()) {
            size++;
        }
        if (conf.isMACDHistogramDeltaEnabled()) {
            size++;
        }
        if (conf.wantScore()) {
            size += 2;
        }
        List<MacdSubType> subTypes = wantedSubTypes();
        for (MacdSubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
        }
        emptyField = new Object[size];
        log.info("fieldsizet " + size);
        return size;
    }
    
}


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

import org.apache.commons.math3.util.Pair;

import com.tictactec.ta.lib.MInteger;

import roart.config.ConfigConstants;
import roart.config.MyConfig;
import roart.db.DbSpark;
import roart.ml.MLDao;
import roart.ml.MLModel;
import roart.model.StockItem;
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
    Map<String, Object[]> objectMap;
    //Map<String, Double> resultMap;
    Map<String, Object[]> resultMap;
    Map<String, Double[]> momMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    Object[] emptyField;

    private int fieldSize = 0;

    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;

    /**
     *  days before positive/negative change
     * @return
     */
    private int getDaysBeforeZero() {
        return 25;
    }

    /**
     *  days after positive/negative change
     * @return
     */
    private int getDaysAfterZero() {
        return 10;
    }

    public static  boolean wantScore() {
        return true;
    }

    public static  boolean wantML() {
        return true;
    }

    public static  boolean wantMLSpark() {
        return false;
    }

    public static  boolean wantMLTensorflow() {
        return true;
    }

    public static  boolean wantMCP() {
        return true;
    }

    public static  boolean wantLR() {
        return true;
    }

    public static  boolean wantDNN() {
        return true;
    }

    public static  boolean wantL() {
        return true;
    }

    public static  int weightBuyHist() {
        return 40;
    }

    public static  int weightBuyHistDelta() {
        return 20;
    }

    public static  int weightBuyMacd() {
        return 20;
    }

    public static  int weightBuyMacdDelta() {
        return 20;
    }

    public static  int weightSellHist() {
        return 40;
    }

    public static  int weightSellHistDelta() {
        return 20;
    }

    public static  int weightSellMacd() {
        return 20;
    }

    public static  int weightSellMacdDelta() {
        return 20;
    }

    public static  boolean wantMLHist() {
        return true;
    }

    public static  boolean wantMLMacd() {
        return true;
    }

    List<MLDao> mldaos = new ArrayList<>();

    public IndicatorMACD(MyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        makeWantedSubTypes();
        makeMapTypes();
        if (wantML()) {
            if (wantMLSpark()) {
                mldaos.add(new MLDao("spark"));
            }
            if (wantMLTensorflow()) {
                mldaos.add(new MLDao("tensorflow"));
            }
        }
        fieldSize = fieldSize();
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
            return 2;
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
            return 0;
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
       if (wantMLHist()) {
           wantedSubTypes.add(new MacdSubTypeHist());
       }
       if (wantMLMacd()) {
           wantedSubTypes.add(new MacdSubTypeMacd());
       }
   }
    // TODO make an oo version of this
    private void calculateMomentums(MyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        this.listMap = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap);
        if (!anythingHere(listMap)) {
            return;
        }
        log.info("time0 " + (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        objectMap = new HashMap<>();
        momMap = new HashMap<>();
        buyMap = new HashMap<>();
        sellMap = new HashMap<>();
        List<Double> macdList = new ArrayList<>();
        List<Double> histList = new ArrayList<>();
        List<Double> macdDList = new ArrayList<>();
        List<Double> histDList = new ArrayList<>();
        //int daysAfterZero = 10;
        //int daysBeforeZero = 25;
        Map<String, Object[]> testobjmap = null;
        try {
            long time2 = System.currentTimeMillis();
            Map<String, Object[]> m; 
            m = DbSpark.doCalculationsArr(listMap, this);
            if (m != null) {
                log.info("time2 " + (System.currentTimeMillis() - time2));
                for (String key : m.keySet()) {
                    log.info("key " + key);
                    log.info("value " + Arrays.toString(m.get(key)));
                }
                //objectMap = m;
                testobjmap = m;
                //if (true) return;
            }
        } catch(Exception e) {
            log.info("Exception", e);
        }
        long time1 = System.currentTimeMillis();
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        log.info("listmap " + listMap.size() + " " + listMap.keySet());
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
        /*
        Map<double[], Double> commonHistTypeMap = new HashMap<>();
        Map<double[], Double> posHistTypeMap = new HashMap<>();
        Map<double[], Double> negHistTypeMap = new HashMap<>();
        Map<double[], Double> commonMacdTypeMap = new HashMap<>();
        Map<double[], Double> posMacdTypeMap = new HashMap<>();
        Map<double[], Double> negMacdTypeMap = new HashMap<>();
        */
        nonSparkMACDCalculations(conf, marketdatamap, tu, market, periodstr, perioddata);
        for (String id : listMap.keySet()) {
            Object[] objs = objectMap.get(id);
            Object[] testobjs = testobjmap.get(id);
            Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs);
            momMap.put(id, momentum);
            Map<String, List<Double>> retMap = new HashMap<>();
            //Object[] full = tu.getMomAndDeltaFull(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());
            {
                double[] macd = (double[]) objs[0];
                double[] hist = (double[]) objs[2];
                log.info("outout1 " + Arrays.toString(macd));
                log.info("outout3 " + Arrays.toString(hist));
                log.info("testoutout3 " + Arrays.toString((double[]) testobjs[2]));
            }
            MInteger begOfArray = (MInteger) objs[3];
            MInteger endOfArray = (MInteger) objs[4];

            Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            log.info("beg end " + id + " "+ begOfArray.value + " " + endOfArray.value);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.info("list " + list.length + " " + Arrays.asList(list));
            Double[] trunclist = ArraysUtil.getSubExclusive(list, begOfArray.value, begOfArray.value + endOfArray.value);
            log.info("trunclist" + list.length + " " + Arrays.asList(trunclist));
            if (wantScore()) {
                addToLists(marketdatamap, category, macdList, histList, macdDList, histDList, market, momentum, retMap);
            }
            if (wantML()) {
                if (momentum[0] != null && momentum[1] != null && momentum[2] != null && momentum[3] != null) {
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
                            Map<Integer, Integer>[] map = ArraysUtil.searchForward(aMacdArray, endOfArray.value);
                            getPosNegMap(mapMap, subType.getType(), CMNTYPESTR, POSTYPESTR, id, trunclist, labelMap2, aMacdArray, trunclist.length, map[0], labelFN, labelTN);
                            getPosNegMap(mapMap, subType.getType(), CMNTYPESTR, NEGTYPESTR, id, trunclist, labelMap2, aMacdArray, trunclist.length, map[1], labelTP, labelFP);
                       }
                        /*
                        if (wantMLHist()) {
                            double[] histarr = (double[]) objs[2];
                             Map<Integer, Integer>[] map = ArraysUtil.searchForward(histarr, endOfArray.value);
                            getPosNegMap(commonHistTypeMap, posHistTypeMap, id, trunclist, labelMap2, histarr, trunclist.length, map[0], labelFN, labelTN);
                            getPosNegMap(commonHistTypeMap, negHistTypeMap, id, trunclist, labelMap2, histarr, trunclist.length, map[1], labelTP, labelFP);
                        }
                        if (wantMLMacd()) {
                            double[] macdarr = (double[]) objs[0];
                            Map<Integer, Integer>[] map = ArraysUtil.searchForward(macdarr, endOfArray.value);
                            getPosNegMap(commonMacdTypeMap, posMacdTypeMap, id, trunclist, labelMap2, macdarr, trunclist.length, map[0], labelFN, labelTN);
                            getPosNegMap(commonMacdTypeMap, negMacdTypeMap, id, trunclist, labelMap2, macdarr, trunclist.length, map[1], labelTP, labelFP);
                        }
                        */
                    }
                }
            }
        }

        if (wantScore()) {
            log.info("histlist " + histList);
            getBuySellRecommendations(macdList, histList, macdDList, histDList);
        }
        /*
        Map<Integer, Map<String, Double[]>> commonIdTypeModelHistMap = new HashMap<>(); 
        Map<Integer, Map<String, Double[]>> posIdTypeModelHistMap = new HashMap<>(); 
        Map<Integer, Map<String, Double[]>> negIdTypeModelHistMap = new HashMap<>(); 
        Map<Integer, Map<String, Double[]>> commonIdTypeModelMacdMap = new HashMap<>(); 
        Map<Integer, Map<String, Double[]>> posIdTypeModelMacdMap = new HashMap<>(); 
        Map<Integer, Map<String, Double[]>> negIdTypeModelMacdMap = new HashMap<>(); 
       */
        /*
        Map<String, Double[]> commonIdTypeMCPHistMap = null; 
        Map<String, Double[]> posIdTypeMCPHistMap = null; 
        Map<String, Double[]> negIdTypeMCPHistMap = null; 
        Map<String, Double[]> commomIdTypeLRHistMap = null; 
        Map<String, Double[]> posIdTypeLRHistMap = null; 
        Map<String, Double[]> negIdTypeLRHistMap = null; 
        Map<String, Double[]> commonIdTypeMCPMacdMap = null; 
        Map<String, Double[]> posIdTypeMCPMacdMap = null; 
        Map<String, Double[]> negIdTypeMCPMacdMap = null; 
        Map<String, Double[]> commonIdTypeLRMacdMap = null; 
        Map<String, Double[]> posIdTypeLRMacdMap = null; 
        Map<String, Double[]> negIdTypeLRMacdMap = null; 
        */
        // map from h/m to model to posnegcom map<model, results>
        Map<MacdSubType, Map<MLModel, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        if (wantML()) {
            Map<Double, String> labelMapShort = createLabelMapShort();
            try {
                List<MacdSubType> subTypes = wantedSubTypes();
                for (MacdSubType subType : subTypes) {
                    for (MLDao mldao : mldaos) {
                        for (int mapTypeInt : getMapTypeList()) {
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            System.out.println("mapget " + mapName);
                           Map<double[], Double> map = mapMap.get(mapName);
                            mldao.learntest(this, map, null, getDaysBeforeZero(), key, mapName, 4);  
                        }
                    }
                }
                    /*
                    if (wantMLHist()) {
                    mldao.learntest(commonHistTypeMap, model, getDaysBeforeZero(), key, "common", 4);
                    mldao.learntest(posHistTypeMap, model, getDaysBeforeZero(), key, "pos", 4);
                    mldao.learntest(negHistTypeMap, model, getDaysBeforeZero(), key, "neg", 4);
                }
                if (wantMLMacd()) {
                        mldao.learntest(commonMacdTypeMap, model, getDaysBeforeZero(), key, "commonM", 4);
                        mldao.learntest(posMacdTypeMap, model, getDaysBeforeZero(), key, "posM", 4);
                        mldao.learntest(negMacdTypeMap, model, getDaysBeforeZero(), key, "negM", 4);
                    }
                }
                     */
                } catch (Exception e) {
                    log.error("Exception", e);
                }
            // calculate sections and do ML
            /*
            Map<String, double[]> commonIdHistMap = new HashMap<>();
            Map<String, double[]> posIdHistMap = new HashMap<>();
            Map<String, double[]> negIdHistMap = new HashMap<>();
            Map<String, double[]> commonIdMacdMap = new HashMap<>();
            Map<String, double[]> posIdMacdMap = new HashMap<>();
            Map<String, double[]> negIdMacdMap = new HashMap<>();
            */
        // a map from h/m + com/neg/sub to map<id, values>
            Map<String, Map<String, double[]>> mapIdMap= new HashMap<>();
            for (String id : listMap.keySet()) {
                Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
                Object[] objs = objectMap.get(id);
                double[] macdarr = (double[]) objs[0];
                double[] histarr = (double[]) objs[2];
                MInteger begOfArray = (MInteger) objs[3];
                MInteger endOfArray = (MInteger) objs[4];
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
                Map<MLModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
                for (MLDao mldao : mldaos) {
                    // map from posnegcom to map<id, result>
                    Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                    for (MLModel model : mldao.getModels()) {
                        for (int mapTypeInt : getMapTypeList()) {
                            String mapType = mapTypes.get(mapTypeInt);
                            String mapName = subType.getType() + mapType;
                            Map<String, double[]> map = mapIdMap.get(mapName);
                            Map<String, Double[]> classifyResult = mldao.classify(this, map, model, getDaysBeforeZero(), key, mapName, 4, labelMapShort);
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
            int retindex = tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);

            if (wantScore()) {
                Double buy = buyMap.get(id);
                fields[retindex++] = buy;
                Double sell = sellMap.get(id);
                fields[retindex++] = sell;
            }
            // TODO make OO of this
            if (wantML()) {
                Map<Double, String> labelMapShort2 = createLabelMapShort();
                //int momidx = 6;
                Double[] type;
                List<MacdSubType> subTypes2 = wantedSubTypes();
                for (MacdSubType subType : subTypes2) {
                    Map<MLModel, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                    System.out.println("mapget " + subType + " " + mapResult.keySet());
                    for (MLDao mldao : mldaos) {
                        for (MLModel model : mldao.getModels()) {
                            Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                            //for (int mapTypeInt : getMapTypeList()) {
                                //String mapType = mapTypes.get(mapTypeInt);
                                //Map<String, Double[]> mapResult3 = mapResult2.get(mapType);
                                //String mapName = subType.getType() + mapType;
                                System.out.println("fields " + fields.length + " " + retindex);
                                retindex = mldao.addResults(fields, retindex, id, model, this, mapResult2, labelMapShort2);
                            //}
                        }   
                    }
                }
            }
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));

    }

    private void getMlMappings(String name, Map<Double, String> labelMapShort, Map<String, double[]> commonMap,
            Map<String, double[]> posMap, Map<String, double[]> negMap, String id, double[] array, MInteger endOfArray,
            Double[] valueList) {
        Map<Integer, Integer>[] map = ArraysUtil.searchForward(array, endOfArray.value);
        Map<Integer, Integer> pos = map[0];
        Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, getDaysBeforeZero(), getDaysAfterZero(), valueList.length);
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, getDaysBeforeZero(), getDaysAfterZero(), valueList.length);
        //System.out.println("negpos " + newNeg.size() + " " + newPos.size());
        printSignChange(name, id, newPos, newNeg, endOfArray.value, getDaysAfterZero(), labelMapShort);
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
        Map<Integer, Integer> newPos = ArraysUtil.getFreshRanges(pos, getDaysBeforeZero(), getDaysAfterZero(), valueList.length);
        Map<Integer, Integer> neg = map[1];
        Map<Integer, Integer> newNeg = ArraysUtil.getFreshRanges(neg, getDaysBeforeZero(), getDaysAfterZero(), valueList.length);
        //System.out.println("negpos " + newNeg.size() + " " + newPos.size());
        printSignChange(name, id, newPos, newNeg, endOfArray.value, getDaysAfterZero(), labelMapShort);
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

    private void addToLists(Map<String, MarketData> marketdatamap, int category, List<Double> macdList,
            List<Double> histList, List<Double> macdDList, List<Double> histDList, String market, Double[] momentum,
            Map<String, List<Double>> retMap) throws Exception {
        {

            if (momentum[0] != null) {
                histList.add(momentum[0]);
            }
            if (momentum[1] != null) {
                histDList.add(momentum[1]);
            }
            if (momentum[2] != null) {
                macdList.add(momentum[2]);
            }
            if (momentum[3] != null) {
                macdDList.add(momentum[3]);
            }
            //System.out.println("outout2 " + Arrays.toString(sig));
            List<StockItem> datedstocklists[] = marketdatamap.get(market).datedstocklists;
            int index = 0;
            if (false && index >= 0) {
                for (int i = index; i < datedstocklists.length; i++) {
                    List<StockItem> stocklist = datedstocklists[i];
                    for (StockItem stock : stocklist) {
                        String stockid = stock.getId();
                        Double value = StockDao.getValue(stock, category);
                        if (value != null) {
                            StockDao.mapAdd(retMap, stockid, value);
                        }
                    }
                }
            }
        }
    }

    private void nonSparkMACDCalculations(MyConfig conf, Map<String, MarketData> marketdatamap, TaUtil tu,
            String market, String periodstr, PeriodData perioddata) {
        for (String id : listMap.keySet()) {
            if (id.equals("EUCA000520")) {
                Double[] list = listMap.get(id);
                Pair<String, String> pair = new Pair<>(market, id);
                Set<Pair<String, String>> ids = new HashSet<>();
                ids.add(pair);
                double momentum = tu.getMom(conf.getDays(), market, id, ids, marketdatamap, perioddata, periodstr);
            }
            Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            log.info("beg end " + id + " "+ key);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.info("list " + list.length + " " + Arrays.asList(list));
            if (id.equals("EUCA000520")) {
                log.info("india list " + list);
            }
            //double momentum = tu.getMom(list, conf.getDays());
            Object[] objs = tu.getMomAndDeltaFull(list, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
            objectMap.put(id, objs);
        }
    }

    private void getPosMap(Map<double[], Double> commonMap, Map<double[], Double> posMap, String id,
            Double[] list, Map<String, Double> labelMap2, double[] array, int listsize,
            Map<Integer, Integer>[] map) {
        Map<Integer, Integer> pos = map[0];
        //System.out.println("Checking " + key + " " + id + " " + listsize + " " + histarr.length);
        if (list.length == 0) {
            //System.out.println("h " + Arrays.asList( histarr));
        }
        Map<Integer, Integer> newPos = ArraysUtil.getAcceptedRanges(pos, getDaysBeforeZero(), getDaysAfterZero(), listsize);
        for (int start : newPos.keySet()) {
            int end = newPos.get(start);
            String label = null;
            try {
                if (list[end] < list[end + getDaysAfterZero()]) {
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
        Map<Integer, Integer> newNeg = ArraysUtil.getAcceptedRanges(neg, getDaysBeforeZero(), getDaysAfterZero(), listsize);
        for (int start : newNeg.keySet()) {
            int end = newNeg.get(start);
            String label;
            if (list[end] < list[end + getDaysAfterZero()]) {
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
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, getDaysBeforeZero(), getDaysAfterZero(), listsize);
        for (int start : newPosNeg.keySet()) {
            int end = newPosNeg.get(start);
            String textlabel;
            if (list[end] < list[end + getDaysAfterZero()]) {
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
        Map<Integer, Integer> newPosNeg = ArraysUtil.getAcceptedRanges(posneg, getDaysBeforeZero(), getDaysAfterZero(), listsize);
        for (int start : newPosNeg.keySet()) {
            int end = newPosNeg.get(start);
            String textlabel;
            if (list[end] < list[end + getDaysAfterZero()]) {
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
            System.out.println("mapput " + key);
            mapMap.put(key, map);
        }
        return map;
    }

    private void printme(String label, int end, Double[] values, double[] array) {
        String me1 = "";
        String me2 = "";
        for (int i = end - 3; i <= end + getDaysAfterZero(); i++) {
            me1 = me1 + values[i] + " ";
            me2 = me2 + array[i] + " ";
        }
        log.info("me1 " + me1);
        log.info("me2 " + me2);
    }

    private void getBuySellRecommendations(List<Double> macdList, List<Double> histList, List<Double> macdDList,
            List<Double> histDList) {
        Double maxhist = 0.0;
        Double maxdhist = 0.0;
        Double maxmacd = 0.0;
        Double maxdmacd = 0.0;
        Double minhist = 0.0;
        Double mindhist = 0.0;
        Double minmacd = 0.0;
        Double mindmacd = 0.0;
        log.info("listsize" + histList.size());
        log.info("listsize" + histDList.size());
        log.info("listsize" + macdList.size());
        log.info("listsize" + macdDList.size());
        if (!histList.isEmpty()) {
            maxhist = Collections.max(histList);
        }
        if (!histDList.isEmpty()) {
            maxdhist = Collections.max(histDList);
        }
        if (!macdList.isEmpty()) {
            maxmacd = Collections.max(macdList);
        }
        if (!macdDList.isEmpty()) {
            maxdmacd = Collections.max(macdDList);
        }
        if (!histList.isEmpty()) {
            minhist = Collections.min(histList);
        }
        if (!histDList.isEmpty()) {
            mindhist = Collections.min(histDList);
        }
        if (!macdList.isEmpty()) {
            minmacd = Collections.min(macdList);
        }
        if (!macdDList.isEmpty()) {
            mindmacd = Collections.min(macdDList);
        }
        // find recommendations
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            if (momentum[0] == null || momentum[1] == null || momentum[2] == null || momentum[3] == null) {
                continue;
            }
            double hist = momentum[0];
            double histd = momentum[1];
            double macd = momentum[2];
            double macdd = momentum[3];
            if (hist >= 0) {
                double recommend = weightBuyHist()*(maxhist - hist)/maxhist;
                if (histd >= 0) {
                    recommend += weightBuyHistDelta()*(histd)/maxdhist;
                }
                if (macd >= 0) {
                    recommend += weightBuyMacd()*(macd)/maxmacd;
                }
                if (macdd >= 0) {
                    recommend += weightBuyMacdDelta()*(macdd)/maxdmacd;
                }
                buyMap.put(id, recommend);
            }
            if (hist < 0) {
                double recommend = weightSellHist()*(minhist - hist)/minhist;
                if (histd < 0) {
                    recommend += weightSellHistDelta()*(histd)/mindhist;
                }
                if (macd < 0) {
                    recommend += weightSellMacd()*(macd)/minmacd;
                }
                if (macdd < 0) {
                    recommend += weightSellMacdDelta()*(macdd)/mindmacd;
                }
                sellMap.put(id, recommend);
            }
        }
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
                System.out.println(txt + " sign changed to negative for id " + id + " " + ControlService.getName(id) + " " + key + " since " + (listsize - posmax));
            }
        }
        if (!neg.isEmpty()) {
            int negmaxind = Collections.max(neg.keySet());
            int negmax = neg.get(negmaxind);
            if (negmax + 1 == listsize) {
                return;
            }
            if (negmax + daysAfterZero >= listsize) {
                System.out.println(txt + " sign changed to positive for id " + id + " " + ControlService.getName(id) + " " + key + " since " + (listsize - negmax));
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
    public Object calculate(Object as) {
        TaUtil tu = new TaUtil();
        //log.info("myclass " + as.getClass().getName());
        WrappedArray wa = (WrappedArray) as;
        Double[] arr2 = (Double[]) wa.array();
        //log.info("myclass " + arr2.getClass().getName());
        //Double[] arr = (Double[]) as;
        //List<Double> list = Arrays.asList(arr2);
        //Double[] momentum = tu.getMomAndDelta(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());
        Object[] objs = tu.getMomAndDeltaFull(arr2, conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
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
            System.out.println("key " + key + " : " + periodDataMap.keySet());
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
        if (wantScore()) {
            objs[retindex++] = title + Constants.WEBBR + "buy";
            objs[retindex++] = title + Constants.WEBBR + "sell";
        }
        // TODO make OO of this
        List<MacdSubType> subTypes = wantedSubTypes();
        for (MacdSubType subType : subTypes) {
            for (MLDao mldao : mldaos) {
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
        if (wantScore()) {
            size += 2;
        }
        List<MacdSubType> subTypes = wantedSubTypes();
        for (MacdSubType subType : subTypes) {
            for (MLDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
        }
        emptyField = new Object[size];
        log.info("fieldsizet " + size);
        return size;
    }
}


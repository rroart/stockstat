package roart.predictor;

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

import com.tictactec.ta.lib.MInteger;

import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.db.DbAccess;
import roart.db.DbDao;
import roart.db.DbSpark;
import roart.ml.MLPredictDao;
import roart.ml.MLPredictModel;
import roart.model.LearnTestPredict;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.service.ControlService;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
//import roart.util.TaUtil;
import scala.collection.mutable.WrappedArray;

public class PredictorLSTM extends Predictor {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    Map<String, Integer>[] periodmap;
    String key;
    Map<String, Double[][]> listMap;
    Map<String, double[][]> truncListMap;
    Map<String, Object[]> resultMap;
    Object[] emptyField;
    Map<MLPredictModel, Long> mapTime = new HashMap<>();

    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;

    private int fieldSize = 0;

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

    List<MLPredictDao> mldaos = new ArrayList<>();

    public PredictorLSTM(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        if (!isEnabled()) {
            return;
        }
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        makeWantedSubTypes();
        makeMapTypes();
        if (conf.wantML()) {
            if (conf.wantMLSpark()) {
                mldaos.add(new MLPredictDao("spark", conf));
            }
            if (conf.wantMLTensorflow()) {
                mldaos.add(new MLPredictDao("tensorflow", conf));
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
        calculate(conf, marketdatamap, periodDataMap, category);        
    }

    private abstract class PredSubType {
        public abstract  String getType();
        public abstract  String getName();
    }

    private class PredSubTypeFull extends PredSubType {
        @Override
        public String getType() {
            return "F";
        }
        @Override
        public String getName() {
            return "Full";
        }
    }

    private class PredSubTypeSingle extends PredSubType {
        @Override
        public String getType() {
            return "S";
        }
        @Override
        public String getName() {
            return "Single";
        }
    }

    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }

    private List<Integer> getMapTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(0);
        return retList;
    }

    @Override
    public List<Integer> getTypeList() {
        return getMapTypeList();
    }

    private Map<Integer, String> mapTypes = new HashMap<>();

    private void makeMapTypes() {
        mapTypes.put(0, "me");
    }

    private List<PredSubType> wantedSubTypes = new ArrayList<>();

    private List<PredSubType> wantedSubTypes() {
        return wantedSubTypes;
    }

    private void makeWantedSubTypes() {
        wantedSubTypes.add(new PredSubTypeSingle());
    }
    // TODO make an oo version of this
    private void calculate(MyMyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category) throws Exception {
        DbAccess dbDao = DbDao.instance(conf);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        Map<String, Double[][]> retArray = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.listMap = retArray;
        this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        if (conf.wantPercentizedPriceIndex()) {

        }
        if (!anythingHere(listMap)) {
            System.out.println("empty"+key);
            return;
        }
        log.info("time0 " + (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();

        long time2 = System.currentTimeMillis();
        //objectMap = dbDao.doCalculationsArr(conf, listMap, key, null /*this*/, conf.wantPercentizedPriceIndex());
        //System.out.println("imap " + objectMap.size());
        log.info("time2 " + (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        //TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        log.info("listmap " + listMap.size() + " " + listMap.keySet());
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
        // System.out.println("allids " + listMap.size());
        //Map<String, List<Double[]>> splits = new HashMap<>();
        for (String id : listMap.keySet()) {
            //Object[] objs = objectMap.get(id);
            //Double[] momentum = tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs);
            //momMap.put(id, momentum);
            Map<String, List<Double>> retMap = new HashMap<>();
            //Object[] full = tu.getMomAndDeltaFull(list, conf.getDays(), conf.isMACDDeltaEnabled(), conf.getMACDDeltaDays(), conf.isMACDHistogramDeltaEnabled(), conf.getMACDHistogramDeltaDays());

            //Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            double[][] list0 = truncListMap.get(id);
            double[] list = list0[0];
            log.info("listsize"+ list.length);
            // TODO do not need?
            if (conf.wantPercentizedPriceIndex()) {
                //list = ArraysUtil.getPercentizedPriceIndex(list, key);
            }
            //log.info("beg end " + id + " "+ begOfArray.value + " " + endOfArray.value);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.info("list " + list.length + " " + Arrays.asList(list));
        }
        // map from h/m to model to posnegcom map<model, results>
        //Map<PredSubType, Map<MLModel2, Map<String, Map<String, Double[]>>>> mapResult = new HashMap<>();
        Map<String, LearnTestPredict> mapResult = new HashMap<>();
        if (conf.wantML()) {
            try {
                List<PredSubType> subTypes = wantedSubTypes();
                for (PredSubType subType : subTypes) {
                    for (MLPredictDao mldao : mldaos) {
                        //System.out.println("mapget " + mapName + " " + mapMap.keySet());
                        //Map<double[], Double> map = mapMap.get(mapName);
                        for (String id : listMap.keySet()) {
                            // TODO configure file
                            int horizon = conf.getPredictorLSTMHorizon();
                            int windowsize = conf.getPredictorLSTMWindowsize();
                            int epochs = conf.getPredictorLSTMEpochs();
                            double[][] list0 = truncListMap.get(id);
                            double[] list = list0[0];
                            // TODO check reverse. move up before if?
                            //list = ArraysUtil.getArrayNonNullReverse(list);
                            log.info("bla " + list.length + " " + windowsize);
                            if (list != null && list.length > 2 * windowsize ) {
                                Map map = null;
                                String mapName = null;
                                List next = null;
                                Double[] list3 = ArrayUtils.toObject(list);
                                LearnTestPredict result = mldao.learntestpredict(this, list3, next, map, null, conf.getMACDDaysBeforeZero(), key, mapName, 4, mapTime, windowsize, horizon, epochs);  
                                mapResult.put(id, result);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Exception", e);
            }
            // calculate sections and do ML
            // a map from h/m + com/neg/sub to map<id, values>
            // map from h/m + posnegcom to map<model, results>
            List<PredSubType> subTypes = wantedSubTypes();
            for (PredSubType subType : subTypes) {
                Map<MLPredictModel, Map<String, Map<String, Double[]>>> mapResult1 = new HashMap<>();
                for (MLPredictDao mldao : mldaos) {
                    // map from posnegcom to map<id, result>
                    Map<String, Map<String, Double[]>> mapResult2 = new HashMap<>();
                    for (MLPredictModel model : mldao.getModels()) {
                    }
                }
                //mapResult.put(subType, mapResult1);
            }
        }
        List<Map> maplist = new ArrayList<>();
        log.info("here00");
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[1];
            resultMap.put(id, fields);
            int retindex = 0;
            log.info("here0");
            if (conf.wantML()) {
                log.info("here1");
                //int momidx = 6;
                Double[] type;
                List<PredSubType> subTypes2 = wantedSubTypes();
                for (PredSubType subType : subTypes2) {
                    //Map<MLModel2, Map<String, Map<String, Double[]>>> mapResult1 = mapResult.get(subType);
                    log.info("here11");
                    //System.out.println("mapget " + subType + " " + mapResult.keySet());
                    for (MLPredictDao mldao : mldaos) {
                        log.info("here111");
                        for (MLPredictModel model : mldao.getModels()) {
                            log.info("here1111");
                            //Map<String, Map<String, Double[]>> mapResult2 = mapResult1.get(model);
                            //for (int mapTypeInt : getMapTypeList()) {
                            //String mapType = mapTypes.get(mapTypeInt);
                            //Map<String, Double[]> mapResult3 = mapResult2.get(mapType);
                            //String mapName = subType.getType() + mapType;
                            //System.out.println("fields " + fields.length + " " + retindex);
                            retindex = mldao.addResults(fields, retindex, id, model, this, mapResult, null);
                            //}
                        }   
                    }
                }
            }
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
        // and others done with println
        if (conf.wantOtherStats()) {
        }
        if (conf.wantMLTimes()) {
            //Map<MLModel2, Long> mapTime = new HashMap<>();
            for (MLPredictModel model : mapTime.keySet()) {
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(key);
                row.add(model.getEngineName());
                row.add(model.getName());
                row.add(mapTime.get(model));
                mlTimesTableRows.add(row);
            }
        }

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

  public void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add(key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }

    public static void printout(Double[] type, String id, Map<Double, String> labelMapShort) {
        if (type != null) {
            //System.out.println("Type " + labelMapShort.get(type[0]) + " id " + id);
        }
    }

    @Override
    public Object calculate(Double[] array) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorLSTM();
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
        // TODO make OO of this
        List<PredSubType> subTypes = wantedSubTypes();
        for (PredSubType subType : subTypes) {
            for (MLPredictDao mldao : mldaos) {
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
        if (true) return 1;
        int size = 0;
        List<PredSubType> subTypes = wantedSubTypes();
        for (PredSubType subType : subTypes) {
            for (MLPredictDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
        }
        emptyField = new Object[size];
        log.info("fieldsizet " + size);
        return size;
    }

    public static void mapAdder(Map<MLPredictModel, Long> map, MLPredictModel key, Long add) {
        Long val = map.get(key);
        if (val == null) {
            val = new Long(0);
        }
        val += add;
        map.put(key, val);
    }

}


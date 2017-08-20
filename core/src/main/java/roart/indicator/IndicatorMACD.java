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

import roart.pipeline.PipelineConstants;
import roart.config.ConfigConstants;
import roart.config.MyMyConfig;
import roart.db.DbAccess;
import roart.db.DbDao;
import roart.db.DbSpark;
import roart.evaluation.MACDRecommend;
import roart.ml.MLClassifyDao;
import roart.ml.MLClassifyModel;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
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
    Map<String, double[]> truncListMap;
    // TODO save and return this map
    // TODO need getters for this and not? buy/sell
    Map<String, Object[]> objectMap;
    Map<String, Object[]> objectFixedMap;
    //Map<String, Double> resultMap;
    Map<String, Object[]> resultMap;
    Map<String, Double[]> momMap;
    Map<String, Double> buyMap;
    Map<String, Double> sellMap;
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();
    
    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;
    
    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORMACD;
    }
    
    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORMACDRESULT, momMap);
        map.put(PipelineConstants.INDICATORMACDOBJECT, objectMap);
        map.put(PipelineConstants.INDICATORMACDLIST, listMap);
        return map;
    }
    
    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.RESULT, momMap);
        map.put(PipelineConstants.OBJECT, objectMap);
        map.put(PipelineConstants.OBJECTFIXED, objectFixedMap);
        map.put(PipelineConstants.LIST, listMap);
        map.put(PipelineConstants.TRUNCLIST, truncListMap);
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

    public IndicatorMACD(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        fieldSize = fieldSize();
        calculateMomentums(conf, marketdatamap, periodDataMap, category);        
    }

    // TODO make an oo version of this
    private void calculateMomentums(MyMyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category) throws Exception {
        DbAccess dbDao = DbDao.instance(conf);
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        boolean currentYear = "cy".equals(key);
        this.listMap = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, currentYear);
        this.truncListMap = ArraysUtil.getTruncList(this.listMap);
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
        long time2 = System.currentTimeMillis();
        objectMap = dbDao.doCalculationsArr(conf, truncListMap, key, this, conf.wantPercentizedPriceIndex());
        //objectFixedMap = ArraysUtil.makeFixedMap(objectMap, conf.getDays());
        //System.out.println("imap " + objectMap.size());
        log.info("time2 " + (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        TaUtil tu = new TaUtil();
        String market = conf.getMarket();
        String periodstr = key;
        //PeriodData perioddata = periodDataMap.get(periodstr);
        log.info("listmap " + listMap.size() + " " + listMap.keySet());
        // a map from subtype h/m + maptype com/neg/pos to a map<values, label>
        Map<String, Map<double[], Double>> mapMap = new HashMap<>();
       // System.out.println("allids " + listMap.size());
        boolean done = false;
        for (String id : listMap.keySet()) {
            Object[] objs = objectMap.get(id);
            //Object[] objs2 = objectFixedMap.get(id);
            if (!done) {
                //System.out.println("s " + objs.length + " " + objs2.length);
                done = true;
            }
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
                double[] macd = (double[]) objs[TaUtil.MACDIDXMACD];
                double[] hist = (double[]) objs[TaUtil.MACDIDXHIST];
                log.info("outout1 " + Arrays.toString(macd));
                log.info("outout3 " + Arrays.toString(hist));
            }
            MInteger begOfArray = (MInteger) objs[TaUtil.MACDIDXBEG];
            MInteger endOfArray = (MInteger) objs[TaUtil.MACDIDXEND];

            //Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            Double[] list = listMap.get(id);
            log.info("listsize"+ list.length);
            if (conf.wantPercentizedPriceIndex()) {
            list = ArraysUtil.getPercentizedPriceIndex(list, key);
            }
            log.info("beg end " + id + " "+ begOfArray.value + " " + endOfArray.value);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.info("list " + list.length + " " + Arrays.asList(list));
            Double[] trunclist = ArraysUtil.getSubExclusive(list, begOfArray.value, begOfArray.value + endOfArray.value);
            log.info("trunclist" + list.length + " " + Arrays.asList(trunclist));
        }

        log.info("Period " + title + " " + mapMap.keySet());
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
        }
        log.info("time1 " + (System.currentTimeMillis() - time1));
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
        emptyField = new Object[size];
        log.info("fieldsizet " + size);
        return size;
    }
    
    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs, offset);

    }
    
    // TODO call tautil
    @Override
    public int getResultSize() {
        return 4;        
    }
}


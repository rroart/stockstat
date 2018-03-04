package roart.indicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.db.DbAccess;
import roart.db.DbDao;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public abstract class Indicator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    protected String key;
    protected int fieldSize = 0;
    protected Map<String, MarketData> marketdatamap;
    protected Map<String, PeriodData> periodDataMap;
    protected Map<String, Integer>[] periodmap;
    protected Object[] emptyField;

    protected Map<String, Double[][]> listMap;
    protected Map<String, double[][]> truncListMap;
    // TODO save and return this map
    // TODO need getters for this and not? buy/sell
    protected Map<String, Object[]> objectMap;
    protected Map<String, Object[]> objectFixedMap;
    protected Map<String, Double[]> calculatedMap;
    protected Map<String, Object[]> resultMap;

    protected Map<String, Map<String, Object[]>> marketObjectMap;
    protected Map<String, Map<String, Object[]>> marketResultMap;
    protected Map<String, Map<String, Double[]>> marketCalculatedMap;

    public Indicator(MyMyConfig conf, String string, int category) {
        this.title = string;
        this.conf = conf;
        this.category = category;
    }

    public abstract boolean isEnabled();
    protected abstract Double[] getCalculated(MyMyConfig conf, Map<String, Object[]> objectMap, String id);
    protected abstract void getFieldResult(MyMyConfig conf, TaUtil tu, Double[] momentum, Object[] fields);

    public Object[] getResultItemTitle() {
        Object[] titleArray = new Object[1];
        titleArray[0] = title;
        return titleArray;
    }

    public Object calculate(double[][] array) {
        return null;
    }

    public Object calculate(Double[][] array) {
        double[][] newArray = new double[array.length][];
        for (int i = 0; i < array.length; i ++) {
            newArray[i] = ArrayUtils.toPrimitive(array[i]);
        }
        return calculate(newArray);
    }

    public Object calculate(scala.collection.Seq[] objArray) {
        double[][] newArray = new double[objArray.length][];
        for (int i = 0; i < objArray.length; i++) {
            List list = scala.collection.JavaConversions.seqAsJavaList(objArray[0]);
            Double[] array = new Double[list.size()];
            array = (Double[]) list.toArray(array);
            newArray[i] = ArrayUtils.toPrimitive(array);
        }
        return calculate(newArray);
    }

    public List<Integer> getTypeList() {
        return null;
    }

    public Map<Integer, String> getMapTypes() {
        return null;
    }

    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        return null;
    }

    public Map<String, Object> getResultMap() {
        return null;
    }

    public Object[] getDayResult(Object[] objs, int offset) {
        return null;
    }

    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.RESULT, calculatedMap);
        map.put(PipelineConstants.OBJECT, objectMap);
        map.put(PipelineConstants.OBJECTFIXED, objectFixedMap);
        map.put(PipelineConstants.LIST, listMap);
        map.put(PipelineConstants.TRUNCLIST, truncListMap);
        map.put(PipelineConstants.RESULT, calculatedMap);
        map.put(PipelineConstants.MARKETOBJECT, marketObjectMap);
        map.put(PipelineConstants.MARKETCALCULATED, marketCalculatedMap);
        map.put(PipelineConstants.MARKETRESULT, marketResultMap);
        return map;
    }

    public int getResultSize() {
        return 0;
    }

    public String indicatorName() {
        return null;
    }

    public int getCategory() {
        return category;
    }

    public boolean wantForExtras() {
        return false;        
    }


    // TODO make an oo version of this
    protected void calculateAll(MyMyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category, Pipeline[] datareaders) throws Exception {
        DbAccess dbDao = DbDao.instance(conf);
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        this.listMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        this.truncListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCLIST);       
        if (!anythingHere(listMap)) {
            log.info("empty {}", key);
            return;
        }
        List<Map> resultList = getMarketCalcResults(conf, dbDao, truncListMap);
        objectMap = resultList.get(0);
        calculatedMap = resultList.get(1);
        resultMap = resultList.get(2);
    }

    private boolean anythingHere(Map<String, Double[][]> listMap2) {
        for (Double[][] array : listMap2.values()) {
            for (int i = 0; i < array[0].length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected void calculateForExtras(Pipeline[] datareaders) {
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);

        Pipeline extrareader = pipelineMap.get(PipelineConstants.EXTRAREADER);
        if (extrareader == null) {
            return;
        }
        Map<String, Object> localResults =  extrareader.getLocalResultMap();
        Map<Pair<String, String>, List<StockItem>> pairStockMap = (Map<Pair<String, String>, List<StockItem>>) localResults.get(PipelineConstants.PAIRSTOCK);
        Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap = (Map<Pair<String, String>, Map<Date, StockItem>>) localResults.get(PipelineConstants.PAIRDATE);
        Map<Pair<String, String>, String> pairCatMap = (Map<Pair<String, String>, String>) localResults.get(PipelineConstants.PAIRCAT);
        Map<Pair<String, String>, Double[][]> pairListMap = (Map<Pair<String, String>, Double[][]>) localResults.get(PipelineConstants.PAIRLIST);
        Map<Pair<String, String>, List<Date>> pairDateListMap = (Map<Pair<String, String>, List<Date>>) localResults.get(PipelineConstants.PAIRDATELIST);
        Map<Pair<String, String>, double[][]> pairTruncListMap = (Map<Pair<String, String>, double[][]>) localResults.get(PipelineConstants.PAIRTRUNCLIST);
        log.info("lockeys {}", localResults.keySet());
        Map<Pair<String, String>, List<StockItem>> pairMap = pairStockMap;
        Map<String, Map<String, double[][]>> marketListMap = new HashMap<>();
        for(Pair<String, String> pair : pairMap.keySet()) {
            String market = pair.getFirst();
            Map<String, double[][]> aListMap = marketListMap.get(market);
            if (aListMap == null) {
                aListMap = new HashMap<>();
                marketListMap.put(market, aListMap);
            }
            String id = pair.getSecond();
            double[][] truncList = pairTruncListMap.get(pair);
            if (truncList != null) {
                aListMap.put(id, truncList);
            }
        }
        marketObjectMap = new HashMap<>();
        marketCalculatedMap = new HashMap<>();
        marketResultMap = new HashMap<>();

        DbAccess dbDao = DbDao.instance(conf);
        for (Entry<String, Map<String, double[][]>> entry : marketListMap.entrySet()) {
            String market = entry.getKey();
            Map<String, double[][]> myTruncListMap = entry.getValue();
            List<Map> resultList = getMarketCalcResults(conf, dbDao, myTruncListMap);
            if (resultList == null || resultList.isEmpty()) {
                continue;
            }
            Map anObjectMap = resultList.get(0);
            Map aCalculatedMap = resultList.get(1);
            Map aResultMap = resultList.get(2);
            marketObjectMap.put(market, anObjectMap);
            marketCalculatedMap.put(market, aCalculatedMap);
            marketResultMap.put(market, aResultMap);
        }
    }

    protected List getMarketCalcResults(MyMyConfig conf, DbAccess dbDao, Map<String, double[][]> truncListMap) {
        List<Map> resultList = new ArrayList<>();
        if (truncListMap == null || truncListMap.isEmpty()) {
            return resultList;
        }
        long time0 = System.currentTimeMillis();
        log.info("time0 {}", (System.currentTimeMillis() - time0));

        long time2 = System.currentTimeMillis();
        Map<String, Object[]> myObjectMap = dbDao.doCalculationsArr(conf, truncListMap, key, this, conf.wantPercentizedPriceIndex());

        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        TaUtil tu = new TaUtil();
        log.info("listmap {} {}", truncListMap.size(), truncListMap.keySet());
        Map<String, Double[]> myCalculatedMap = getCalculatedMap(conf, tu, myObjectMap, truncListMap);

        Map<String, Object[]> myResultMap = getResultMap(conf, tu, myObjectMap, myCalculatedMap);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        resultList.add(myObjectMap);
        resultList.add(myCalculatedMap);
        resultList.add(myResultMap);
        return resultList;
    }

    protected Map<String, Double[]> getCalculatedMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap, Map<String, double[][]> truncListMap) {
        Map<String, Double[]> result = new HashMap<>();
        for (String id : truncListMap.keySet()) {
            Double[] calculated = getCalculated(conf, objectMap, id);
            if (calculated != null) {
                result.put(id, calculated);
                // TODO and continue?
            } else {
                log.info("nothing for id {}", id);
            }
        }
        return result;
    }

    protected Map<String, Object[]> getResultMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap, Map<String, Double[]> momMap) {
        Map<String, Object[]> result = new HashMap<>();
        if (listMap == null) {
            return result;
        }
        for (String id : listMap.keySet()) {
            Double[] momentum = momMap.get(id);
            Object[] fields = new Object[fieldSize];
            result.put(id, fields);
            if (momentum == null) {
                log.info("zero mom for id {}", id);
            }
            getFieldResult(conf, tu, momentum, fields);
        }
        return result;
    }

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
        if (resultMap == null) {
            return emptyField;
        }
        Object[] result = resultMap.get(id);
        if (result == null) {
            result = emptyField;
        }
        return result;
    }

}


package roart.indicator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.db.DbAccess;
import roart.db.DbDao;
import roart.model.ResultItemTable;
import roart.model.ResultItemTableRow;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.TaUtil;

public abstract class Indicator {

    protected static Logger log = LoggerFactory.getLogger(Indicator.class);

    protected String title;
    protected MyMyConfig conf;
    protected int category;
    protected String key;
    
    protected Map<String, Double[]> listMap;
    protected Map<String, double[]> truncListMap;
    // TODO save and return this map
    // TODO need getters for this and not? buy/sell
    protected Map<String, Object[]> objectMap;
    protected Map<String, Object[]> objectFixedMap;
    //Map<String, Double> resultMap;
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

    abstract public boolean isEnabled();

    public Object[] getResultItemTitle() {
    	Object[] titleArray = new Object[1];
    	titleArray[0] = title;
        return titleArray;
    }

    abstract public Object[] getResultItem(StockItem stock);

    public Object calculate(double[] array) {
        return null;
    }

    public Object calculate(Double[] array) {
        return calculate(ArrayUtils.toPrimitive(array));
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
        return null;
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
    
    protected void calculateForExtras(Pipeline[] datareaders) {
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);

        Pipeline extrareader = pipelineMap.get(PipelineConstants.EXTRAREADER);
        Map<String, Object> localResults =  extrareader.getLocalResultMap();
        Map<Pair, List<StockItem>> pairStockMap = (Map<Pair, List<StockItem>>) localResults.get(PipelineConstants.PAIRSTOCK);
        Map<Pair, Map<Date, StockItem>> pairDateMap = (Map<Pair, Map<Date, StockItem>>) localResults.get(PipelineConstants.PAIRDATE);
        Map<Pair, String> pairCatMap = (Map<Pair, String>) localResults.get(PipelineConstants.PAIRCAT);
        Map<Pair, Double[]> pairListMap = (Map<Pair, Double[]>) localResults.get(PipelineConstants.PAIRLIST);
        Map<Pair, List<Date>> pairDateListMap = (Map<Pair, List<Date>>) localResults.get(PipelineConstants.PAIRDATELIST);
        Map<Pair, double[]> pairTruncListMap = (Map<Pair, double[]>) localResults.get(PipelineConstants.PAIRTRUNCLIST);
        System.out.println("lockeys" + localResults.keySet());
        Map<Pair, List<StockItem>> pairMap = pairStockMap;
        Map<String, Map<String, double[]>> marketListMap = new HashMap<>();
        for(Pair pair : pairMap.keySet()) {
            String market = (String) pair.getFirst();
            Map<String, double[]> aListMap = marketListMap.get(market);
            if (aListMap == null) {
                aListMap = new HashMap<>();
                marketListMap.put(market, aListMap);
            }
            String id = (String) pair.getSecond();
            double[] truncList = pairTruncListMap.get(pair);
            if (truncList == null) {
                System.out.println("bl");
            }
            //double[] aTruncList = truncListMap.get(id);
            if (truncList != null) {
                aListMap.put(id, truncList);
            }
        }
        Pipeline datareader = pipelineMap.get("" + category);

        marketObjectMap = new HashMap<>();
        marketCalculatedMap = new HashMap<>();
        marketResultMap = new HashMap<>();
        
        //this.listMap = (Map<String, Double[]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        //this.truncListMap = (Map<String, double[]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCLIST);       
        DbAccess dbDao = DbDao.instance(conf);
        for (String market : marketListMap.keySet()) {
            Map<String, double[]> truncListMap = marketListMap.get(market);
            List<Map> resultList = getMarketCalcResults(conf, dbDao, truncListMap);
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
 
    protected List getMarketCalcResults(MyMyConfig conf, DbAccess dbDao, Map<String, double[]> truncListMap) {
        List<Map> resultList = new ArrayList<>();
        if (truncListMap == null || truncListMap.isEmpty()) {
            return resultList;
        }
        long time0 = System.currentTimeMillis();
        log.info("time0 " + (System.currentTimeMillis() - time0));

        long time2 = System.currentTimeMillis();
        Map<String, Object[]> objectMap = dbDao.doCalculationsArr(conf, truncListMap, key, this, conf.wantPercentizedPriceIndex());

        log.info("time2 " + (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        TaUtil tu = new TaUtil();
        //PeriodData perioddata = periodDataMap.get(periodstr);
        log.info("listmap " + truncListMap.size() + " " + truncListMap.keySet());
        Map<String, Double[]> calculatedMap = getCalculatedMap(conf, tu, objectMap, truncListMap);

        Map<String, Object[]> resultMap = getResultMap(conf, tu, objectMap, calculatedMap);
        log.info("time1 " + (System.currentTimeMillis() - time1));
        resultList.add(objectMap);
        resultList.add(calculatedMap);
        resultList.add(resultMap);
        return resultList;
    }

    protected abstract Map<String, Object[]> getResultMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap, Map<String, Double[]> momMap);
    protected abstract Map<String, Double[]> getCalculatedMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap, Map<String, double[]> truncListMap);
}


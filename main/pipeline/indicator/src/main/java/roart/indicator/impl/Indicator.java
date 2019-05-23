package roart.indicator.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Pair;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.db.common.DbAccess;
import roart.db.dao.DbDao;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.model.StockItem;
import roart.pipeline.Pipeline;

public class Indicator extends AbstractIndicator {

    public Indicator(MyMyConfig conf, String string, int category) {
        super(conf, string, category);
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
            List<Map> resultList = getMarketCalcResults(dbDao, myTruncListMap);
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

    protected void calculateAll(int category, Pipeline[] datareaders) throws Exception {
        DbAccess dbDao = DbDao.instance(conf);
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        this.listMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        this.fillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.FILLLIST);
        this.truncListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCLIST);       
        this.truncFillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);       
        this.base100ListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.BASE100LIST);
        this.base100FillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.BASE100FILLLIST);
        this.truncBase100ListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100LIST);       
        this.truncBase100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);       
        if (!anythingHere(listMap)) {
            log.info("empty {}", key);
            return;
        }
        List<Map> resultList = getMarketCalcResults(dbDao, conf.wantPercentizedPriceIndex() ? truncBase100FillListMap : truncFillListMap);
        objectMap = resultList.get(0);
        calculatedMap = resultList.get(1);
        resultMap = resultList.get(2);
    }

    protected List getMarketCalcResults(DbAccess dbDao, Map<String, double[][]> aListMap) {
        List<Map> resultList = new ArrayList<>();
        if (aListMap == null || aListMap.isEmpty()) {
            return resultList;
        }
        long time0 = System.currentTimeMillis();
        log.info("time0 {}", (System.currentTimeMillis() - time0));

        long time2 = System.currentTimeMillis();
        Map<String, Object[]> myObjectMap = dbDao.doCalculationsArr(conf, aListMap, key, this, conf.wantPercentizedPriceIndex());

        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        log.info("listmap {} {}", aListMap.size(), aListMap.keySet());
        Map<String, Double[]> myCalculatedMap = getCalculatedMap(myObjectMap, aListMap);

        Map<String, Object[]> myResultMap = getResultMap(conf, myObjectMap, myCalculatedMap);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        resultList.add(myObjectMap);
        resultList.add(myCalculatedMap);
        resultList.add(myResultMap);
        return resultList;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    protected Double[] getCalculated(Map<String, Object[]> objectMap, String id) {
        return null;
    }

    @Override
    protected void getFieldResult(Double[] momentum, Object[] fields) {
    }

}

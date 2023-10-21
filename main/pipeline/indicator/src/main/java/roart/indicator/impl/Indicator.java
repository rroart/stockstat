package roart.indicator.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.MarketStock;
import roart.iclij.config.IclijConfig;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.db.common.DbAccess;
import roart.db.dao.DbDao;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;

public abstract class Indicator extends AbstractIndicator {

    public Indicator(IclijConfig conf, String string, int category) {
        super(conf, string, category);
    }

    protected void calculateForExtras(Pipeline[] datareaders) {
        if (category != 42 && fieldSize == 0) {
            return;
        }
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);

        Pipeline extrareader = pipelineMap.get(PipelineConstants.EXTRAREADER);
        if (extrareader == null) {
            return;
        }
        Map<String, Map<String, double[][]>> marketListMap = getMarketListMap(extrareader);
        marketObjectMap = new HashMap<>();
        marketCalculatedMap = new HashMap<>();
        marketResultMap = new HashMap<>();

        for (Entry<String, Map<String, double[][]>> entry : marketListMap.entrySet()) {
            String market = entry.getKey();
            Map<String, double[][]> myTruncListMap = entry.getValue();
            Map<String, Double[][]> newMyTruncListMap = convert(myTruncListMap);
            if (!anythingHere(newMyTruncListMap)) {
                continue;
            }
            List<Map> resultList = getMarketCalcResults(myTruncListMap);
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

    public Map<String, Map<String, double[][]>> getMarketListMap(Pipeline extrareader) {
        Map<String, Map<String, double[][]>> marketListMap = new HashMap<>();
        Map<String, Object> localResults =  extrareader.getLocalResultMap();
        /*
        Map<Pair<String, String>, List<StockItem>> pairStockMap = null; // (Map<Pair<String, String>, List<StockItem>>) localResults.get(PipelineConstants.PAIRSTOCK);
        //Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap = (Map<Pair<String, String>, Map<Date, StockItem>>) localResults.get(PipelineConstants.PAIRDATE);
        //Map<Pair<String, String>, String> pairCatMap = (Map<Pair<String, String>, String>) localResults.get(PipelineConstants.PAIRCAT);
        //Map<Pair<String, String>, Double[][]> pairListMap = (Map<Pair<String, String>, Double[][]>) localResults.get(PipelineConstants.PAIRLIST);
        //Map<Pair<String, String>, List<Date>> pairDateListMap = (Map<Pair<String, String>, List<Date>>) localResults.get(PipelineConstants.PAIRDATELIST);
        Map<Pair<String, String>, double[][]> pairTruncListMap = null; // (Map<Pair<String, String>, double[][]>) localResults.get(PipelineConstants.PAIRTRUNCLIST);
        */
        Set<String> commonDates = (Set<String>) localResults.get(PipelineConstants.DATELIST);
        LinkedHashSet<MarketStock> marketStocks = (LinkedHashSet<MarketStock>) localResults.get(PipelineConstants.MARKETSTOCKS);
        Map<String, Pipeline[]> dataReaderMap = (Map<String, Pipeline[]>) localResults.get(PipelineConstants.DATAREADER);
        Map<String, StockData>  stockDataMap = (Map<String, StockData>) localResults.get(PipelineConstants.STOCKDATA);
        log.debug("lockeys {}", localResults.keySet());
        //Map<Pair<String, String>, List<StockItem>> pairMap = pairStockMap;
        for(MarketStock ms : marketStocks) {
            String market = ms.getMarket();
            String id = ms.getId();
            String catName = ms.getCategory();
            Map<String, double[][]> aListMap = marketListMap.get(market);
            if (aListMap == null) {
                aListMap = new HashMap<>();
                marketListMap.put(market, aListMap);
            }
            Pipeline[] datareaders2 = dataReaderMap.get(market);
            Map<String, Pipeline> pipelineMap2 = IndicatorUtils.getPipelineMap(datareaders2);
            int category = 0; // extraData.category;
            String cat = ms.getCategory();
            StockData stockData = stockDataMap.get(market);
            if (cat == null) {
                cat = stockData.catName;
            }
            int mycat = stockData.cat;
            Pipeline[] mydatareaders = dataReaderMap.get(market);
            Map<String, Pipeline> mypipelineMap = IndicatorUtils.getPipelineMap(mydatareaders);
            Pipeline datareader = mypipelineMap.get("" + mycat);
            //Pipeline datareader = pipelineMap.get("" + category);
            Map<String, Double[][]> fillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.FILLLIST);
            Map<String, double[][]> truncFillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);
            Object[] arr = null;
            Double[][] fillList0 = fillListMap.get(ms.getId());
            double[][] fillList = truncFillListMap.get(ms.getId());
            if (fillList != null) {
                aListMap.put(ms.getId(), fillList);
            }
        }
        return marketListMap;
    }

    public Map<String, Double[][]> convert(Map<String, double[][]> myTruncListMap) {
        Map<String, Double[][]> newMyTruncListMap = new HashMap<>();
        for (Entry<String, double[][]> entry2 : myTruncListMap.entrySet()) {
            double[][] val = entry2.getValue();
            int length = 0;
            for (int i = 0; i < val.length; i++) {
                if (val[i] != null /*&& val[i].length > 0*/) {
                    length++;
                }
            }
            Double doubles[][] = new Double[length][];
            for (int i = 0; i < length; i++) {
                doubles[i] = ArraysUtil.convert(val[i]);
            }
            newMyTruncListMap.put(entry2.getKey(), doubles);
        }
        return newMyTruncListMap;
    }

    protected void calculateAll(int category, Pipeline[] datareaders) throws Exception {
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        /*
        this.listMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        this.fillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.FILLLIST);
        this.truncListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCLIST);       
        this.truncFillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);       
        this.base100ListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.BASE100LIST);
        this.base100FillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.BASE100FILLLIST);
        this.truncBase100ListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100LIST);       
        this.truncBase100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);
        */
        this.datareader = datareader;
        if (!anythingHere(getListMap())) {
            log.info("empty {}", key);
            return;
        }
        fieldSize = fieldSize();
        boolean wantPercentizedPriceIndex = conf.wantPercentizedPriceIndex();
        if (wantPercentizedPriceIndex() != null) {
            //wantPercentizedPriceIndex = wantPercentizedPriceIndex();
        }
        Map<String, double[][]> truncFillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);       
        Map<String, double[][]> truncBase100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);
        List<Map> resultList = getMarketCalcResults(wantPercentizedPriceIndex ? truncBase100FillListMap : truncFillListMap);
        objectMap = resultList.get(0);
        calculatedMap = resultList.get(1);
        resultMap = resultList.get(2);
    }

    protected Boolean wantPercentizedPriceIndex() {
        return null;
    }

    protected List getMarketCalcResults(Map<String, double[][]> aListMap) {
        List<Map> resultList = new ArrayList<>();
        if (aListMap == null || aListMap.isEmpty()) {
            return resultList;
        }
        log.info("time for {}", title);
        long time0 = System.currentTimeMillis();
        log.info("time0 {}", (System.currentTimeMillis() - time0));

        long time2 = System.currentTimeMillis();
        Map<String, Object[]> myObjectMap = IndicatorUtils.doCalculationsArrNonNull(conf, aListMap, key, this, conf.wantPercentizedPriceIndex());

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
    
    protected int fieldSize() {
        return 0;
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
    
    protected int getAnythingHereRange() {
        return 1;
    }
}

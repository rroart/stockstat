package roart.indicator.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import roart.common.config.MarketStock;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialList;
import roart.common.pipeline.data.SerialMap;
import roart.common.pipeline.data.SerialMarketStock;
import roart.common.pipeline.data.SerialObject;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.data.TwoDimd;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.ArraysUtil;
import roart.iclij.config.IclijConfig;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;

public abstract class Indicator extends AbstractIndicator {

    private PipelineData[] datareaders;
    private boolean onlyExtra;

    public Indicator(IclijConfig conf, String string, int category, PipelineData[] datareaders, boolean onlyExtra, Inmemory inmemory) {
        super(conf, string, category, inmemory);
        this.datareaders = datareaders;
        this.onlyExtra = onlyExtra;
    }

    protected void calculateForExtras(PipelineData[] datareaders) {
        if (category != Constants.NOCOLUMN && fieldSize == 0) {
            return;
        }
        PipelineData extrareader = PipelineUtils.getPipeline(datareaders, PipelineConstants.EXTRAREADER, inmemory);
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
            Map<String, SerialTA> anObjectMap = resultList.get(0);
            Map<String, Double[]> aCalculatedMap = resultList.get(1);
            Map<String, Object[]> aResultMap = resultList.get(2);
            marketObjectMap.put(market, new SerialMap(anObjectMap));
            marketCalculatedMap.put(market, aCalculatedMap);
            marketResultMap.put(market, aResultMap);
        }
    }

    public Map<String, Map<String, double[][]>> getMarketListMap(PipelineData extrareader) {
        Map<String, Map<String, double[][]>> marketListMap = new HashMap<>();
        PipelineData localResults =  extrareader;
        /*
        Map<Pair<String, String>, List<StockDTO>> pairStockMap = null; // (Map<Pair<String, String>, List<StockDTO>>) localResults.get(PipelineConstants.PAIRSTOCK);
        //Map<Pair<String, String>, Map<Date, StockDTO>> pairDateMap = (Map<Pair<String, String>, Map<Date, StockDTO>>) localResults.get(PipelineConstants.PAIRDATE);
        //Map<Pair<String, String>, String> pairCatMap = (Map<Pair<String, String>, String>) localResults.get(PipelineConstants.PAIRCAT);
        //Map<Pair<String, String>, Double[][]> pairListMap = (Map<Pair<String, String>, Double[][]>) localResults.get(PipelineConstants.PAIRLIST);
        //Map<Pair<String, String>, List<Date>> pairDateListMap = (Map<Pair<String, String>, List<Date>>) localResults.get(PipelineConstants.PAIRDATELIST);
        Map<Pair<String, String>, double[][]> pairTruncListMap = null; // (Map<Pair<String, String>, double[][]>) localResults.get(PipelineConstants.PAIRTRUNCLIST);
        */
        //Set<String> commonDates = (Set<String>) localResults.get(PipelineConstants.DATELIST);
        List<SerialMarketStock> marketStocks = PipelineUtils.getMarketstocks(localResults);
        // all from here is already read from eventual inmemory
        Map<String, SerialList<PipelineData>> dataReaderMap = PipelineUtils.getDatareader(localResults);
        log.debug("lockeys {}", localResults.keySet());
        //Map<Pair<String, String>, List<StockDTO>> pairMap = pairStockMap;
        for(SerialMarketStock ms : marketStocks) {
            String market = ms.getMarket();
            String id = ms.getId();
            String catName = ms.getCategory();
            Map<String, double[][]> aListMap = marketListMap.get(market);
            if (aListMap == null) {
                aListMap = new HashMap<>();
                marketListMap.put(market, aListMap);
            }
            SerialList datareaders2 = (SerialList) dataReaderMap.get(market);
            Map<String, PipelineData> pipelineMap2 = IndicatorUtils.getPipelineMap(datareaders2);
            int category = 0; // extraData.category;
            String cat = ms.getCategory();
            if (cat == null) {
                cat = Constants.EXTRA;
            }
            SerialList mydatareaders = (SerialList) dataReaderMap.get(market);
            Map<String, PipelineData> mypipelineMap = IndicatorUtils.getPipelineMap(mydatareaders);
            PipelineData datareader = mypipelineMap.get(cat);
            //Pipeline datareader = pipelineMap.get("" + category);
            if (datareader == null) {
                int jj = 0;
            }
            if (datareader == null) {
                datareader = mypipelineMap.get(Constants.PRICE);
                log.debug("TODO temp workaround");
            }
            Map<String, Double[][]> fillListMap = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.FILLLIST));
            Map<String, double[][]> truncFillListMap = PipelineUtils.sconvertMapdd(datareader.get(PipelineConstants.TRUNCFILLLIST));
            Object[] arr = null;
            //Double[][] fillList0 = fillListMap.get(ms.getId());
            double[][] fillList = truncFillListMap != null ? truncFillListMap.get(ms.getId()) : null;
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

    public void calculate() throws Exception {
        if (isEnabled() && !onlyExtra) {
            calculateAll(category, datareaders);
        }
        if (wantForExtras()) {
            calculateForExtras(datareaders);
        }
    }
    
    protected void calculateAll(int category, PipelineData[] datareaders) throws Exception {
        PipelineData datareader = PipelineUtils.getPipeline(datareaders, key, inmemory);
        log.info("preempty {}", category);
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
        Map<String, double[][]> truncFillListMap = PipelineUtils.sconvertMapdd(datareader.get(PipelineConstants.TRUNCFILLLIST));       
        Map<String, double[][]> truncBase100FillListMap = PipelineUtils.sconvertMapdd(datareader.get(PipelineConstants.TRUNCBASE100FILLLIST));
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
        Map<String, SerialTA> myObjectMap = IndicatorUtils.doCalculationsArrNonNull(conf, aListMap, key, this, conf.wantPercentizedPriceIndex());

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
    protected Double[] getCalculated(Map<String, SerialTA> objectMap, String id) {
        return null;
    }

    @Override
    protected void getFieldResult(Double[] momentum, Object[] fields) {
    }
    
    protected int getAnythingHereRange() {
        return 1;
    }
}

package roart.pipeline.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.Extra;
import roart.common.config.MarketStock;
import roart.common.config.MarketStockExpression;
import roart.common.constants.Constants;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.TwoDimD;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.PipelineUtils;
import roart.common.util.TimeUtil;
import roart.etl.ComplexETL;
import roart.etl.ValueETL;
import roart.iclij.config.IclijConfig;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.data.ExtraData;
import roart.stockutil.StockDao;

public class ExtraReader extends Pipeline {

    //Map<MarketStock, List<StockItem>> pairStockMap;
    //Map<MarketStock, Map<Date, StockItem>> pairDateMap;
    //Map<Pair<String, String>, String> pairCatMap;
    //Map<Pair<String, String>, Double[][]> pairListMap;
    //Map<Pair<String, String>, List<Date>> pairDateListMap;
    //Map<Pair<String, String>, double[][]> pairTruncListMap;
    public Set<String> commonDates;
    public Set<MarketStock> allMarketStocks = new LinkedHashSet<>();
    private Set<String> markets = new HashSet<>();
    private Set<MarketStock> marketStocks = new HashSet<>();
    private Extra extra;
    private Map<String, Pipeline[]> dataReaderMap;
    private Map<String, StockData> stockDataMap;

    public ExtraReader(IclijConfig conf, Map<String, MarketData> marketdatamap, int category, StockData stockData) throws Exception {
        super(conf, category);
        setMarkets(conf, marketdatamap, category, stockData);        
    }
    
    
    public Set<String> getMarkets() {
        return markets;
    }


    public void setMarkets(Set<String> markets) {
        this.markets = markets;
    }


    private void setMarkets(IclijConfig conf, Map<String, MarketData> marketdatamap, int category, StockData stockData2) throws Exception {
        String dateme = TimeUtil.format(conf.getConfigData().getDate());
        //pairListMap = new HashMap<>();
        //pairDateMap = new HashMap<>();
        //pairCatMap = new HashMap<>();
        //pairStockMap = new HashMap<>();
        //pairDateListMap = new HashMap<>();
        //pairTruncListMap = new HashMap<>();

        String mseString = conf.getAggregatorsIndicatorExtrasList();
        String bits = conf.getAggregatorsIndicatorExtrasBits();
        Extra[] extras = new Extra[0];
        if (mseString != null && !mseString.isEmpty()) {
            extras = JsonUtil.convert(mseString, Extra[].class);
        }
        
        String str = conf.getAggregatorsIndicatorExtras();
        if (str == null || str.isEmpty()) {
            return;
        }
        String date = TimeUtil.convertDate2(conf.getConfigData().getDate());
        MarketData marketdata = marketdatamap.get(conf.getConfigData().getMarket());
        extra = JsonUtil.convert(str, Extra.class);
        if (extras.length > 0) {
            extra.getSimple().clear();
            extra.getComplex().clear();
            for (int i = 0; i < extras.length; i++) {
                if ('1' == bits.charAt(i)) {
                    if (extras[i].getSimple() != null) {
                        extra.getSimple().addAll(extras[i].getSimple());
                    }
                    if (extras[i].getComplex() != null) {
                        extra.getComplex().addAll(extras[i].getComplex());
                    }
                }
            }
        }
        //ObjectMapper mapper = new ObjectMapper();
        //List<LinkedHashMap> list = (List<LinkedHashMap>) mapper.readValue(str, List.class);
        //List<Pair<String, String>> pairs = new ArrayList<>();
        allMarketStocks = new LinkedHashSet<>();
        if (!extra.getSimple().isEmpty()) {
            for (MarketStock ms : extra.getSimple()) {
                if (conf.getConfigData().getMarket().equals(ms.getMarket())) {
                    //continue;
                }
                markets.add(ms.getMarket());
                marketStocks.add(ms);
                allMarketStocks.add(ms);
                //MarketStock aMs = new MarketStock(ms.getMarket(), null, ms.getCategory());
                //Pair<String, String> pair = new ImmutablePair(ms.getMarket(), ms.getId());
                //pairs.add(pair);
                //String cat = (String) ms.getCategory();
                //pairCatMap.put(pair, cat);

            }
        }
        if (!extra.getComplex().isEmpty()) {
            for (MarketStockExpression mse : extra.getComplex()) {
                for (MarketStock ms : mse.getItems()) {
                    // skip if the same market
                    if (conf.getConfigData().getMarket().equals(ms.getMarket())) {
                        //continue;
                    }
                    markets.add(ms.getMarket());
                    marketStocks.add(ms);
                    //Pair<String, String> pair = new ImmutablePair(ms.getMarket(), ms.getId());
                    //pairs.add(pair);
                }
                //System.out.println("mi"+mse+" " + mse.toString());
                //String cat = (String) mse.get("category");
                //pairCatMap.put(pair, cat);
            }
        }
    }

    public void readData(IclijConfig conf, Map<String, MarketData> marketdatamap, int category, StockData stockData2, Map<String, StockData> stockDataMap, Map<String, Pipeline[]> dataReaderMap) throws Exception {
        this.dataReaderMap = dataReaderMap;
        this.stockDataMap = stockDataMap;
        
        List<String> dateList = StockDao.getDateList(conf.getConfigData().getMarket(), marketdatamap);
        commonDates = new HashSet<>(dateList);
        for (MarketStock ms : marketStocks) {
            String market = ms.getMarket();
            List<StockItem> stocksId = new ArrayList<>();
            StockData stockData = stockDataMap.get(market);
            Set<String> aDateSet = new HashSet<>(StockDao.getDateList(market, stockData.marketdatamap));
            commonDates.retainAll(aDateSet);
            //Pipeline[] datareaders = dataReaderMap.get(market);
            //List<StockItem> mystocks = new ArrayList<>();
            Map<Date, StockItem> mymap = new HashMap<>();
            for (StockItem stock : stocksId) {
                //mystocks.add(stock);
                //mymap.put(stock.getDate(), stock);
            }
            //pairStockMap.put(ms, mystocks);
            //pairDateMap.put(ms, mymap);
            /*
            Map<String, MarketData> marketDataMap = getMarketdatamap(conf.getDays(), market, conf, stocksId);

            MarketData marketData = marketDataMap.get(market);

            boolean currentYear = false;
            if (category >= 0) {
                currentYear = MetaUtil.currentYear(marketData, marketData.periodtext[category]);
            }
             */
        }
        if (!extra.getComplex().isEmpty()) {
            Map<String, List<Double>> newMap = new HashMap<>();
            for (MarketStockExpression mse : extra.getComplex()) {
                new ComplexETL().method(mse, commonDates, stockDataMap, dataReaderMap, newMap);
            }
            for (Entry<String, List<Double>> entry : newMap.entrySet()) {
                String id = entry.getKey();
                List<Double> list = entry.getValue();
                Double[] myArray = new Double[list.size()];
                Double[] emptyArray = new Double[0];
                list.toArray(myArray);
                Map<String, Double[][]> listMap = new HashMap<>();
                listMap.put(id, new Double[][] { myArray });

                StockData newStockData = new StockData();
                newStockData.cat = Constants.EXTRACOLUMN;
                stockDataMap.put(id, newStockData);
                DataReader newDataReader = new DataReader(conf, Constants.EXTRACOLUMN);
                newDataReader.listMap = listMap;
                newDataReader.calculateOtherListMaps(conf, Constants.EXTRACOLUMN, null);
                List<String> sortedCommonDates = new ArrayList<>(commonDates);
                Collections.sort(sortedCommonDates);
                newDataReader.dateList = sortedCommonDates;

                Pipeline[] newDataReaders = new Pipeline[] { newDataReader };
                dataReaderMap.put(id, newDataReaders);
                MarketStock complexMs = new MarketStock();
                complexMs.setMarket(id);
                complexMs.setId(id);
                // TODO setcat extra?
                //MarketStock aMs = new MarketStock(ms.getMarket(), null, ms.getCategory());
                //.allMarketStocks..
                allMarketStocks.add(complexMs);
            }
        }
    }

    @Override
    public PipelineData putData() {
        PipelineData map = getData();
        //map.put(PipelineConstants.PAIRLIST, new HashMap() /*pairListMap*/);
        //map.put(PipelineConstants.PAIRDATE, new HashMap() /*pairDateMap*/);
        //map.put(PipelineConstants.PAIRCAT, new HashMap() /*pairCatMap*/);
        //map.put(PipelineConstants.PAIRSTOCK, new HashMap() /*pairStockMap*/);
        //map.put(PipelineConstants.PAIRDATELIST, pairDateListMap);
        //map.put(PipelineConstants.PAIRTRUNCLIST, pairTruncListMap);
        map.setName(PipelineConstants.EXTRAREADER);
        map.put(PipelineConstants.DATELIST, commonDates);
        Map<String, PipelineData[]> dataReaderMap2 = new HashMap<>();
        for (Entry<String, Pipeline[]> entry : dataReaderMap.entrySet()) {
            Pipeline[] pipeline = entry.getValue();
            PipelineData[] pipelinedata = new PipelineData[pipeline.length];
            for (int i = 0; i < pipeline.length; i++) {
                pipelinedata[i] = pipeline[i].putData();
            }
            dataReaderMap2.put(entry.getKey(), pipelinedata );
        }
        map.put(PipelineConstants.DATAREADER, dataReaderMap2);
        map.put(PipelineConstants.MARKETSTOCKS, allMarketStocks);
        return map;
    }
    
    @Override
    public String pipelineName() {
        return PipelineConstants.EXTRAREADER;
    }

    /*
    private Map<String, MarketData> getMarketdatamap(int days,
            String market, IclijConfig conf, List<StockItem> stocksId) throws Exception {
        Map<String, MarketData> marketdatamap = new HashMap();
        log.info("prestocks");
        log.info("stocks {}", stocksId.size());
        MarketData marketdata = new MarketData();
        marketdata.stocks = stocksId;
        String[] periodText = DbDaoUtil.getPeriodText(market, conf, dbDao);
        marketdata.periodtext = periodText;
        marketdata.meta = dbDao.getById(market, conf);
        Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocksId);
        stockdatemap = StockUtil.filterFew(stockdatemap, conf.getFilterDate());
        if (days == 0) {
            days = stockdatemap.keySet().size();
        }
        // the main list, based on freshest or specific date.

        List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getConfigData().getDate(), days, conf.getTableIntervalDays());
        marketdata.datedstocklists = datedstocklists;

        marketdatamap.put(market,  marketdata);

        return marketdatamap;
    }
    */

    public static Double[] getExtraData3(IclijConfig conf, List<Date> dateList,
            Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap, Map<Pair<String, String>, String> pairCatMap, int j, String id,
            Double[] result) throws Exception {
        int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
        int size = dateList.size() - 1;
        if (size - j < 0) {
            int jj = 0;
        }
        if ((size - (j + (deltas - 1))) < 0) {
            int jj = 0;
        }
        Date date = dateList.get(size - j);
        Date prevDate = dateList.get(size - (j + (deltas - 1)));
        for (Entry<Pair<String, String>, Map<Date, StockItem>> entry : pairDateMap.entrySet()) {
            Pair<String, String> pairKey = entry.getKey();
            Object[] arr = null;
            Map<Date, StockItem> dateMap = entry.getValue(); 
            StockItem stock = dateMap.get(date);
            StockItem prevStock = dateMap.get(prevDate);
            if (stock != null && prevStock != null) {
                int category = IndicatorUtils.getCategoryFromString(pairCatMap, pairKey);
                Double value = StockDao.getMainValue(stock, category);
                Double prevValue = StockDao.getMainValue(prevStock, category);
                if (value != null && prevValue != null) {
                    arr = new Object[2];
                    arr[0] = value;
                    arr[1] = (value - prevValue) / (deltas - 1);
                }
            }
            if (arr != null && arr.length > 0) {
                result = (Double[]) ArrayUtils.addAll(result, arr);
            }
        }
        return result;
    }

    // for ARI
    
    public static Double[] getExtraData(IclijConfig conf, ExtraData extraData,
            int j, String id,
            Double[] result) throws Exception {
        int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
        int size = extraData.dateList.size() - 1;
        if (size - j < 0) {
            int jj = 0;
        }
        if ((size - (j + (deltas - 1))) < 0) {
            int jj = 0;
        }
        String date = extraData.dateList.get(size - j);
        String prevDate = extraData.dateList.get(size - (j + (deltas - 1)));
        Map<String, PipelineData[]> dataReaderMap = (Map<String, PipelineData[]>) extraData.extrareader.get(PipelineConstants.DATAREADER);
        LinkedHashSet<MarketStock> allMarketStocks = (LinkedHashSet<MarketStock>) extraData.extrareader.get(PipelineConstants.MARKETSTOCKS);
        for (MarketStock entry : allMarketStocks) {
            String market = entry.getMarket();
            PipelineData[] datareaders = dataReaderMap.get(market);
            Map<String, PipelineData> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
            String cat = entry.getCategory();
            if (cat == null) {
                cat = Constants.EXTRA;
            }
            PipelineData datareader = pipelineMap.get(cat);
            if (datareader == null) {
                datareader = pipelineMap.get(Constants.PRICE);
                log.debug("TODO temp workaround");
            }
            List<String> dateList = (List<String>) datareader.get(PipelineConstants.DATELIST);
            int dateIndex = dateList.indexOf(date);
            int prevDateIndex = dateList.indexOf(prevDate);
            Map<String, Double[][]> fillListMap = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.FILLLIST));
            Object[] arr = null;
            Double[][] fillList = fillListMap.get(entry.getId());
            Double value = fillList[0][dateIndex];
            Double prevValue = fillList[0][prevDateIndex];
            if (value != null && prevValue != null) {
                arr = new Object[2];
                arr[0] = value;
                arr[1] = (value - prevValue) / (deltas - 1);
            }
            if (arr != null && arr.length > 0) {
                result = (Double[]) ArrayUtils.addAll(result, arr);
            }
        }
        return result;
    }

    // for MLI
    
    public static Double[] getExtraData(IclijConfig conf, ExtraData extraData,
            int j, String id,
            Double[] result, String commonDate) throws Exception {
        int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
        int size = extraData.dateList.size() - 1;
        if (size - j < 0) {
            int jj = 0;
        }
        if ((size - (j + (deltas - 1))) < 0) {
            int jj = 0;
        }
        String date = extraData.dateList.get(size - j);
        String prevDate = extraData.dateList.get(size - (j + (deltas - 1)));
        Map<String, PipelineData[]> dataReaderMap = (Map<String, PipelineData[]>) extraData.extrareader.get(PipelineConstants.DATAREADER);
        Collection<MarketStock> allMarketStocks = (Collection<MarketStock>) extraData.extrareader.get(PipelineConstants.MARKETSTOCKS);
        for (MarketStock entry : allMarketStocks) {
            String market = entry.getMarket();
            PipelineData[] datareaders = dataReaderMap.get(market);
            Map<String, PipelineData> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
            String cat = entry.getCategory();
             if (cat == null) {
                cat = Constants.EXTRA;
            }
            PipelineData datareader = pipelineMap.get(cat);
            if (datareader == null) {
                datareader = pipelineMap.get(Constants.PRICE);
                log.debug("TODO temp workaround");
            }
            List<String> dateList = (List<String>) datareader.get(PipelineConstants.DATELIST);
            int dateIndex = dateList.size() - dateList.indexOf(commonDate);
            int prevDateIndex = dateList.indexOf(prevDate);
            Map<String, Double[][]> fillListMap = PipelineUtils.sconvertMapDD(datareader.get(PipelineConstants.FILLLIST));
            Object[] arr = null;
            Double[][] fillList = fillListMap.get(entry.getId());
            dateIndex = fillList[0].length - dateIndex;
            Double value = fillList[0][dateIndex];
            Double prevValue = fillList[0][dateIndex - (deltas - 1)];
            if (value != null && prevValue != null) {
                arr = new Object[2];
                arr[0] = value;
                arr[1] = (value - prevValue) / (deltas - 1);
            }
            if (arr != null && arr.length > 0) {
                result = (Double[]) ArrayUtils.addAll(result, arr);
            }
        }
        return result;
    }
// ?
    @Deprecated
    public Map<Pair<String, String>, Double[][]> getExtraData2(IclijConfig conf, List<Date> dateList,
            Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap, Map<Pair<String, String>, String> pairCatMap, int j2, String id,
            Double[] result) throws Exception {
        int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
        int size = dateList.size() - 1;
        Map<Pair<String, String>, Double[][]> retMap = new HashMap<>();
        for (Entry<Pair<String, String>, Map<Date, StockItem>> entry : pairDateMap.entrySet()) {
            Pair<String, String> pairKey = entry.getKey();
            int category = IndicatorUtils.getCategoryFromString(pairCatMap, pairKey);

            Map<Date, StockItem> dateMap = entry.getValue();
            for (int j = 0; j < size; j++) {
                Date date = dateList.get(size - j);
                StockItem stock = dateMap.get(date);
                if (stock != null) {
                    Double[] value = StockDao.getValue(stock, category);
                    mapAdd(retMap, pairKey, size - 1 - j, value, size);
                }
            }
        }
        retMap = getReverseArrSparseFillHolesArr(conf, retMap);
        //pairListMap = retMap;
        //pairTruncListMap = ArraysUtil.getTruncList22(this.pairListMap);
        return retMap;
    }

    public static void mapAdd(Map<Pair<String, String>, Double[]> aMap, Pair<String, String> id, int index, Double value, int length) {
        Double[] array = aMap.get(id);
        if (array == null) {
            array = new Double[length];
            aMap.put(id, array);
        }
        array[index] = value;
    }

    public static void mapAdd(Map<Pair<String, String>, Double[][]> aMap, Pair<String, String> id, int index, Double[] value, int length) {
        Double[][] array = aMap.get(id);
        for (int i = 0; i < value.length; i++) {
            if (array == null) {
                array = new Double[value.length][length];
                aMap.put(id, array);
            }
            array[i][index] = value[i];
        }
    }

    public static Map<Pair<String, String>, Double[]> getReverseArrSparseFillHoles(IclijConfig conf, Map<Pair<String, String>, Double[]> listMap) {
        Map<Pair<String, String>, Double[]> retMap = new HashMap<>();
        //System.out.println("carn " + Arrays.asList(listMap.get("F00000NMNP")));
        for (Entry<Pair<String, String>, Double[]> entry : listMap.entrySet()) {
            Double[] array = entry.getValue();
            Double[] newArray = new Double[array.length];
            String interpolationmethod = conf.getInterpolationmethod();
            boolean acceptLastNull = conf.getInterpolateLastNull();
            retMap.put(entry.getKey(), ArraysUtil.fixMapHoles(array, newArray, maxHoleNumber(conf), interpolationmethod, acceptLastNull));
        }      
        return retMap;
    }

    public static int maxHoleNumber(IclijConfig conf) {
        return ValueETL.maxHoleNumber(conf);
    }

    // only dup due to maxholes and lacking parametrization of string/pair
    public static Map<Pair<String, String>, Double[][]> getReverseArrSparseFillHolesArr(IclijConfig conf, Map<Pair<String, String>, Double[][]> listMap) {
        Map<Pair<String, String>, Double[][]> retMap = new HashMap<>();
        for (Entry<Pair<String, String>, Double[][]> entry : listMap.entrySet()) {
            Pair<String, String> id = entry.getKey();
            Double[][] array = entry.getValue();
            Double[][] newArray = new Double[array.length][];
            for (int i = 0; i < array.length; i ++) {
                newArray[i] = new Double[array[i].length];
                String interpolationmethod = conf.getInterpolationmethod();
                boolean acceptLastNull = conf.getInterpolateLastNull();
                newArray[i] = ArraysUtil.fixMapHoles(array[i], newArray[i], maxHoleNumber(conf), interpolationmethod, acceptLastNull);
            }
            retMap.put(id, newArray);
        }      
        return retMap;
    }

    public Pipeline[] getDataReaders(IclijConfig conf, String[] periodText,
            Map<String, MarketData> marketdatamap, String market) throws Exception {
        Pipeline[] datareaders = new Pipeline[Constants.PERIODS + 2];
        datareaders[0] = new DataReader(conf, marketdatamap, Constants.INDEXVALUECOLUMN, market);
        datareaders[1] = new DataReader(conf, marketdatamap, Constants.PRICECOLUMN, market);
        for (int i = 0; i < Constants.PERIODS; i++) {
            datareaders[i + 2] = new DataReader(conf, marketdatamap, i, market);
        }
        return datareaders;
    }

}

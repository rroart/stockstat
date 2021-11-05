package roart.pipeline.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.Extra;
import roart.common.config.MarketStock;
import roart.common.config.MarketStockExpression;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.etl.ComplexETL;
import roart.etl.ValueETL;
import roart.etl.db.Extract;
import roart.indicator.util.IndicatorUtils;
import roart.common.constants.Constants;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.data.ExtraData;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockDao;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.stockutil.StockUtil;

public class ExtraReader extends Pipeline {

    //Map<MarketStock, List<StockItem>> pairStockMap;
    //Map<MarketStock, Map<Date, StockItem>> pairDateMap;
    //Map<Pair<String, String>, String> pairCatMap;
    //Map<Pair<String, String>, Double[][]> pairListMap;
    //Map<Pair<String, String>, List<Date>> pairDateListMap;
    //Map<Pair<String, String>, double[][]> pairTruncListMap;
    public Set<String> commonDates;
    Map<String, StockData> stockDataMap;
    Map<String, Pipeline[]> dataReaderMap;
    public Set<MarketStock> allMarketStocks;
    
    public ExtraReader(MyMyConfig conf, Map<String, MarketData> marketdatamap, int category, StockData stockData) throws Exception {
        super(conf, category);
        readData(conf, marketdatamap, category, stockData);        

    }
    private void readData(MyMyConfig conf, Map<String, MarketData> marketdatamap, int category, StockData stockData2) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
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
        String date = TimeUtil.convertDate3(conf.getdate());
        MarketData marketdata = marketdatamap.get(conf.getMarket());
        Extra extra = JsonUtil.convert(str, Extra.class);
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
        Set<MarketStock> marketStocks = new HashSet<>();
        Set<String> markets = new HashSet<>();
        if (!extra.getSimple().isEmpty()) {
            for (MarketStock ms : extra.getSimple()) {
                if (conf.getMarket().equals(ms.getMarket())) {
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
                    if (conf.getMarket().equals(ms.getMarket())) {
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

        stockDataMap = new HashMap<>();
        dataReaderMap = new HashMap<>();
        for (String market : markets) {
            StockData stockData = new Extract().getStockData(conf, market);
            stockDataMap.put(market, stockData);
            Pipeline[] datareaders = getDataReaders(conf, stockData.periodText,
                    stockData.marketdatamap, market);
            dataReaderMap.put(market, datareaders);
        }
        List<String> dateList = StockDao.getDateList(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
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
                newStockData.cat = -3;
                stockDataMap.put(id, newStockData);
                DataReader newDataReader = new DataReader(conf, -3);
                newDataReader.listMap = listMap;
                newDataReader.calculateOtherListMaps(conf, -3, null);
                List<String> sortedCommonDates = new ArrayList<>(commonDates);
                Collections.sort(sortedCommonDates);
                newDataReader.dateList = sortedCommonDates;

                Pipeline[] newDataReaders = new Pipeline[] { newDataReader };
                dataReaderMap.put(id, newDataReaders);
                MarketStock complexMs = new MarketStock();
                complexMs.setMarket(id);
                complexMs.setId(id);
                //MarketStock aMs = new MarketStock(ms.getMarket(), null, ms.getCategory());
                //.allMarketStocks..
                allMarketStocks.add(complexMs);
            }
        }
    }

    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        //map.put(PipelineConstants.PAIRLIST, new HashMap() /*pairListMap*/);
        //map.put(PipelineConstants.PAIRDATE, new HashMap() /*pairDateMap*/);
        //map.put(PipelineConstants.PAIRCAT, new HashMap() /*pairCatMap*/);
        //map.put(PipelineConstants.PAIRSTOCK, new HashMap() /*pairStockMap*/);
        //map.put(PipelineConstants.PAIRDATELIST, pairDateListMap);
        //map.put(PipelineConstants.PAIRTRUNCLIST, pairTruncListMap);
        map.put(PipelineConstants.DATELIST, commonDates);
        map.put(PipelineConstants.DATAREADER, dataReaderMap);
        map.put(PipelineConstants.MARKETSTOCKS, allMarketStocks);
        map.put(PipelineConstants.STOCKDATA, stockDataMap);
        return map;
    }
    
    @Override
    public Map<Integer, Map<String, Object>> getResultMap() {
        return null;
    }

    @Override
    public String pipelineName() {
        return PipelineConstants.EXTRAREADER;
    }

    private Map<String, MarketData> getMarketdatamap(int days,
            String market, MyMyConfig conf, List<StockItem> stocksId) throws Exception {
        Map<String, MarketData> marketdatamap = new HashMap();
        log.info("prestocks");
        log.info("stocks {}", stocksId.size());
        MarketData marketdata = new MarketData();
        marketdata.stocks = stocksId;
        String[] periodText = DbDaoUtil.getPeriodText(market, conf);
        marketdata.periodtext = periodText;
        marketdata.meta = DbDao.getById(market, conf);
        Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocksId);
        if (days == 0) {
            days = stockdatemap.keySet().size();
        }
        // the main list, based on freshest or specific date.

        /*
         * For all days with intervals
         * Make stock lists based on the intervals
         */

        List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableIntervalDays());
        marketdata.datedstocklists = datedstocklists;

        marketdatamap.put(market,  marketdata);

        return marketdatamap;
    }

    public static Double[] getExtraData3(MyMyConfig conf, List<Date> dateList,
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

    public static Double[] getExtraData(MyMyConfig conf, ExtraData extraData,
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
        Map<String, Pipeline[]> dataReaderMap = (Map<String, Pipeline[]>) extraData.extrareader.getLocalResultMap().get(PipelineConstants.DATAREADER);
        Map<String, StockData>  stockDataMap = (Map<String, StockData>) extraData.extrareader.getLocalResultMap().get(PipelineConstants.STOCKDATA);
        LinkedHashSet<MarketStock> allMarketStocks = (LinkedHashSet<MarketStock>) extraData.extrareader.getLocalResultMap().get(PipelineConstants.MARKETSTOCKS);
        for (MarketStock entry : allMarketStocks) {
            String market = entry.getMarket();
            Pipeline[] datareaders = dataReaderMap.get(market);
            Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
            int category = extraData.category;
            String cat = entry.getCategory();
            StockData stockData = stockDataMap.get(market);
            if (cat == null) {
                cat = stockData.catName;
            }
            int mycat = stockData.cat;
            Pipeline datareader = pipelineMap.get("" + mycat);
            List<String> dateList = (List<String>) datareader.getLocalResultMap().get(PipelineConstants.DATELIST);
            int dateIndex = dateList.indexOf(date);
            int prevDateIndex = dateList.indexOf(prevDate);
            Map<String, Double[][]> fillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.FILLLIST);
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

    public static Double[] getExtraData(MyMyConfig conf, ExtraData extraData,
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
        Map<String, Pipeline[]> dataReaderMap = (Map<String, Pipeline[]>) extraData.extrareader.getLocalResultMap().get(PipelineConstants.DATAREADER);
        Map<String, StockData>  stockDataMap = (Map<String, StockData>) extraData.extrareader.getLocalResultMap().get(PipelineConstants.STOCKDATA);
        LinkedHashSet<MarketStock> allMarketStocks = (LinkedHashSet<MarketStock>) extraData.extrareader.getLocalResultMap().get(PipelineConstants.MARKETSTOCKS);
        for (MarketStock entry : allMarketStocks) {
            String market = entry.getMarket();
            Pipeline[] datareaders = dataReaderMap.get(market);
            Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
            int category = extraData.category;
            String cat = entry.getCategory();
            StockData stockData = stockDataMap.get(market);
            if (cat == null) {
                cat = stockData.catName;
            }
            int mycat = stockData.cat;
            Pipeline datareader = pipelineMap.get("" + mycat);
            List<String> dateList = (List<String>) datareader.getLocalResultMap().get(PipelineConstants.DATELIST);
            int dateIndex = dateList.size() - dateList.indexOf(commonDate);
            int prevDateIndex = dateList.indexOf(prevDate);
            Map<String, Double[][]> fillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.FILLLIST);
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
    public Map<Pair<String, String>, Double[][]> getExtraData2(MyMyConfig conf, List<Date> dateList,
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

    public static Map<Pair<String, String>, Double[]> getReverseArrSparseFillHoles(MyMyConfig conf, Map<Pair<String, String>, Double[]> listMap) {
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

    public static int maxHoleNumber(MyMyConfig conf) {
        return ValueETL.maxHoleNumber(conf);
    }

    // only dup due to maxholes and lacking parametrization of string/pair
    public static Map<Pair<String, String>, Double[][]> getReverseArrSparseFillHolesArr(MyMyConfig conf, Map<Pair<String, String>, Double[][]> listMap) {
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

    private Pipeline[] getDataReaders(MyMyConfig conf, String[] periodText,
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

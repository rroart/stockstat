package roart.pipeline.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.indicator.util.IndicatorUtils;
import roart.common.constants.Constants;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.stockutil.MetaUtil;
import roart.stockutil.StockDao;
import roart.model.data.MarketData;
import roart.stockutil.StockUtil;

public class ExtraReader extends Pipeline {

    Map<Pair<String, String>, List<StockItem>> pairStockMap;
    Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap;
    Map<Pair<String, String>, String> pairCatMap;
    Map<Pair<String, String>, Double[][]> pairListMap;
    Map<Pair<String, String>, List<Date>> pairDateListMap;
    Map<Pair<String, String>, double[][]> pairTruncListMap;

    public ExtraReader(MyMyConfig conf, int category) throws Exception {
        super(conf, category);
        readData(conf, null, category);        

    }
    private void readData(MyMyConfig conf, Map<String, MarketData> marketdatamap, int category) throws Exception {
        pairListMap = new HashMap<>();
        pairDateMap = new HashMap<>();
        pairCatMap = new HashMap<>();
        pairStockMap = new HashMap<>();
        pairDateListMap = new HashMap<>();
        pairTruncListMap = new HashMap<>();

        String str = conf.getAggregatorsIndicatorExtras();
        if (str == null || str.isEmpty()) {
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        List<LinkedHashMap> list = (List<LinkedHashMap>) mapper.readValue(str, List.class);
        List<Pair<String, String>> pairs = new ArrayList<>();
        for (LinkedHashMap mi : list) {
            // skip if the same market
            if (conf.getMarket().equals(mi.get("market"))) {
                continue;
            }
            System.out.println("mi"+mi+" " + mi.toString());
            Pair<String, String> pair = new ImmutablePair(mi.get("market"), mi.get("id"));
            String cat = (String) mi.get("category");
            pairs.add(pair);
            pairCatMap.put(pair, cat);
        }

        for (Pair<String, String> pair : pairs) {
            String market = pair.getLeft();
            List<StockItem> stocks = null;
            try {
                stocks = DbDao.getAll(market, conf);
                log.info("stocks {}", stocks.size());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (stocks == null) {
                continue;
            }
            List<StockItem> stocksId = new ArrayList<>();
            for (StockItem stock : stocks) {
                String id = pair.getRight();
                if (stock.getId().equals(id)) {
                    stocksId.add(stock);
                }
            }
            stocks = null;

            List<StockItem> mystocks = new ArrayList<>();
            Map<Date, StockItem> mymap = new HashMap<>();
            for (StockItem stock : stocksId) {
                mystocks.add(stock);
                mymap.put(stock.getDate(), stock);
            }
            pairStockMap.put(pair, mystocks);
            pairDateMap.put(pair, mymap);

            Map<String, MarketData> marketDataMap = getMarketdatamap(conf.getDays(), market, conf, stocksId);

            MarketData marketData = marketDataMap.get(market);

            boolean currentYear = false;
            if (category >= 0) {
                currentYear = MetaUtil.currentYear(marketData, marketData.periodtext[category]);
            }
        }
    }
    
    @Override
    public Map<String, Object> getLocalResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.PAIRLIST, pairListMap);
        map.put(PipelineConstants.PAIRDATE, pairDateMap);
        map.put(PipelineConstants.PAIRCAT, pairCatMap);
        map.put(PipelineConstants.PAIRSTOCK, pairStockMap);
        map.put(PipelineConstants.PAIRDATELIST, pairDateListMap);
        map.put(PipelineConstants.PAIRTRUNCLIST, pairTruncListMap);
        return map;
    }
    @Override
    public Map<Integer, Map<String, Object>> getResultMap() {
        return null;
    }

    private class MarketAndId {
        String market;
        String id;
        public String getMarket() {
            return market;
        }
        public void setMarket(String market) {
            this.market = market;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }

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

    public static Double[] getExtraData(MyMyConfig conf, List<Date> dateList,
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
        pairListMap = retMap;
        pairTruncListMap = ArraysUtil.getTruncList22(this.pairListMap);
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
            retMap.put(entry.getKey(), ArraysUtil.fixMapHoles(array, newArray, maxHoleNumber(conf)));
        }      
        return retMap;
    }

    public static int maxHoleNumber(MyMyConfig conf) {
        return DataReader.maxHoleNumber(conf);
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
                newArray[i] = ArraysUtil.fixMapHoles(array[i], newArray[i], maxHoleNumber(conf));
            }
            retMap.put(id, newArray);
        }      
        return retMap;
    }

}

package roart.aggregate;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.config.MyConfig;
import roart.config.MyMyConfig;
import roart.indicator.IndicatorUtils;
import roart.model.MetaItem;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.ArraysUtil;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.MetaDao;
import roart.util.StockDao;
import roart.util.StockUtil;

public class ExtraReader extends Pipeline {

    Map<Pair, List<StockItem>> pairStockMap;
    Map<Pair, Map<Date, StockItem>> pairDateMap;
    Map<Pair, String> pairCatMap;
    Map<Pair, Double[]> pairListMap;
    Map<Pair, List<Date>> pairDateListMap;
    Map<Pair, double[]> pairTruncListMap;
    
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
        
        String str0 = conf.getAggregatorsIndicatorExtras();
        //String str = "{ [ 'market' : 'cboevol', 'id' : 'VIX'], [ 'market' : 'tradcomm' , 'id' : 'CL1:COM' ], [ 'market' : 'tradcomm', 'id' : 'XAUUSD:CUR' ] ]   }";
        String str = "[ { \"market\" : \"cboevol\", \"id\" : \"VIX\", \"category\" : \"Index\"}, { \"market\" : \"tradcomm\" , \"id\" : \"CL1:COM\" , \"category\" : \"Price\" }, { \"market\" : \"tradcomm\", \"id\" : \"XAUUSD:CUR\" , \"category\" : \"Price\" } ]";
        ObjectMapper mapper = new ObjectMapper();
        List<LinkedHashMap> list = (List<LinkedHashMap>) mapper.readValue(str, List.class);
        List<Pair> pairs = new ArrayList<>();
        for (LinkedHashMap mi : list) {
            System.out.println("mi"+mi+" " + mi.toString());
            Pair pair = new Pair(mi.get("market"), mi.get("id"));
            String cat = (String) mi.get("category");
            pairs.add(pair);
            pairCatMap.put(pair, cat);
            //pairs.add(new Pair(mi.getMarket(), mi.getId()));
        }

        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        for (Pair pair : pairs) {
            String market = (String) pair.getFirst();
            List<StockItem> stocks = null;
            try {
                stocks = StockItem.getAll(market, conf);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            log.info("stocks " + stocks.size());
            if (stocks == null) {
                continue;
            }
            List<StockItem> stocksId = new ArrayList<>();
            for (StockItem stock : stocks) {
                String id = (String) pair.getSecond();
                if (stock.getId().equals(id)) {
                    stocksId.add(stock);
                }
            }
            stocks = null;

            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocksId);

            List<StockItem> mystocks = new ArrayList<>();
            Map<Date, StockItem> mymap = new HashMap<>();
            String id = (String) pair.getSecond();
            for (StockItem stock : stocksId) {
                mystocks.add(stock);
                mymap.put(stock.getDate(), stock);
            }
            List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), conf.getDays(), conf.getTableIntervalDays());
            pairStockMap.put(pair, mystocks);
            pairDateMap.put(pair, mymap);

            Map<String, MarketData> marketDataMap = getMarketdatamap(conf.getDays(), market, conf, stocksId);

            MarketData marketData = marketDataMap.get(market);

            String categoryString = pairCatMap.get(pair);
            boolean currentYear = false;
            if (category >= 0) {
                currentYear = "cy".equals(marketData.periodtext[category]);
            }
            // can not use this: Map<String, Double[]> listMap = StockDao.getArrSparse(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketDataMap, currentYear);
            //pairListMap.put(pair, listMap);
            //pairDateListMap.put(pair, StockDao.getDateList(conf, market, dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketDataMap, false));
            //Map<String, double[]> truncList = ArraysUtil.getTruncList(listMap);
            //pairTruncListMap.put(pair, truncList);
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
        // TODO Auto-generated method stub
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

    // TODO duplicated
    private String[] getPeriodText(String market, MyMyConfig conf) {
        String[] periodText = { "Period1", "Period2", "Period3", "Period4", "Period5", "Period6" };
        MetaItem meta = null;
        try {
            meta = MetaItem.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        try {
            if (meta != null) {
                for (int i = 0; i < StockUtil.PERIODS; i++) {
                    if (MetaDao.getPeriod(meta, i) != null) {
                        periodText[i] = MetaDao.getPeriod(meta, i);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return periodText;
    }

   private Map<String, MarketData> getMarketdatamap(int days,
            String market, MyMyConfig conf, List<StockItem> stocksId) throws Exception {
        Map<String, MarketData> marketdatamap = new HashMap();
        log.info("prestocks");
        log.info("stocks " + stocksId.size());
        MarketData marketdata = new MarketData();
        marketdata.stocks = stocksId;
        String[] periodText = getPeriodText(market, conf);
        marketdata.periodtext = periodText;
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
           Map<Pair, Map<Date, StockItem>> pairDateMap, Map<Pair, String> pairCatMap, int j, String id,
           Double[] result) throws Exception {
       int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
       int size = dateList.size() - 1;
       Date date = dateList.get(size - j);
       Date prevDate = dateList.get(size - (j + (deltas - 1)));
       for (Pair pairKey : pairDateMap.keySet()) {
           String market = (String) pairKey.getFirst();
           String id2 = (String) pairKey.getSecond();
           Object[] arr = null;
           Map<Date, StockItem> dateMap = pairDateMap.get(pairKey); 
           StockItem stock = dateMap.get(date);
           StockItem prevStock = dateMap.get(prevDate);
           if (stock != null && prevStock != null) {
               int category = IndicatorUtils.getCategoryFromString(pairCatMap, pairKey);
               Double value = StockDao.getValue(stock, category);
               Double prevValue = StockDao.getValue(prevStock, category);
                if (value != null && prevValue != null) {
                    arr = new Object[2];
                    arr[0] = value;
                    arr[1] = (value - prevValue) / (deltas - 1);
               }
           }
           if (arr != null && arr.length > 0) {
               result = (Double[]) ArrayUtils.addAll(result, arr);
           } else {
               System.out.println("No obj for id" + id);
           }
       }
       return result;
   }

   public Map<Pair, Double[]> getExtraData2(MyMyConfig conf, List<Date> dateList,
           Map<Pair, Map<Date, StockItem>> pairDateMap, Map<Pair, String> pairCatMap, int j2, String id,
           Double[] result) throws Exception {
       int deltas = conf.getAggregatorsIndicatorExtrasDeltas();
       int size = dateList.size() - 1;
       //Date date = dateList.get(size - j);
       //Date prevDate = dateList.get(size - (j + (deltas - 1)));
       Map<Pair, Double[]> retMap = new HashMap<>();
       for (Pair pairKey : pairDateMap.keySet()) {
           String market = (String) pairKey.getFirst();
           String id2 = (String) pairKey.getSecond();
           int category = IndicatorUtils.getCategoryFromString(pairCatMap, pairKey);
           
           Object[] arr = null;
           Map<Date, StockItem> dateMap = pairDateMap.get(pairKey);
           for (int j = 0; j < size; j++) {
               Date date = dateList.get(size - j);
               StockItem stock = dateMap.get(date);
               if (stock != null) {
                   Double value = StockDao.getValue(stock, category);
                   mapAdd(retMap, pairKey, size - 1 - j, value, size);
               }
           }
       }
       for (Double[] k : retMap.values()) {
           System.out.println("k " +  Arrays.toString(k));
       }
       System.out.println("d " + dateList);
       retMap = getReverseArrSparseFillHoles(conf, retMap);
       for (Double[] k : retMap.values()) {
           System.out.println("k " +  Arrays.toString(k));
       }
       pairListMap = retMap;
       pairTruncListMap = ArraysUtil.getTruncList2(this.pairListMap);
       return retMap;
   }

   public static void mapAdd(Map<Pair, Double[]> aMap, Pair id, int index, Double value, int length) {
       Double[] array = aMap.get(id);
       if (array == null) {
           array = new Double[length];
           aMap.put(id, array);
       }
       array[index] = value;
   }

   public static Map<Pair, Double[]> getReverseArrSparseFillHoles(MyConfig conf, Map<Pair, Double[]> listMap) {
       Map<Pair, Double[]> retMap = /*getReverse*/(listMap);
       //System.out.println("carn " + Arrays.asList(listMap.get("F00000NMNP")));
       for (Pair id : listMap.keySet()) {
           /*
           if (id.equals("F00000NMNP")) {
               int j = 1;
           }
           */
           retMap.put(id, ArraysUtil.fixMapHoles(listMap.get(id), null, maxHoleNumber()));
       }      
       //System.out.println("carn " + Arrays.asList(retMap.get("F00000NMNP")));
       return retMap;
   }
   
   public static int maxHoleNumber() {
       return 35;
   }
   
}

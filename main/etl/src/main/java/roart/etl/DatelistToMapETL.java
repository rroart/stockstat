package roart.etl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.MyMyConfig;
import roart.common.model.StockItem;
import roart.common.util.MapUtil;
import roart.model.data.MarketData;
import roart.stockutil.StockDao;

public class DatelistToMapETL {

    public static Map<String, Double[][]> getArrSparse(MyMyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        Map<String, Double[][]> retMap = new HashMap<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        int index = 0;
        if (!currentyear || !conf.wantMergecy()) {
            if (index >= 0) {
                for (int i = datedstocklists.length - 1; i >= 0; i--) {
                    List<StockItem> stocklist = datedstocklists[i];
                    for (StockItem stock : stocklist) {
                        String stockid = stock.getId();
                        Double[] value = StockDao.getValue(stock, periodInt);
                        MapUtil.mapAdd(retMap, stockid, datedstocklists.length - 1 - i, value, datedstocklists.length);
                    }
                }
            }
        } else {
            Map<String, Double> basenumberMap = new HashMap<>();
            Map<String, Double> lastnumberMap = new HashMap<>();
            Map<String, Integer> yearMap = new HashMap<>();
            for (int i = datedstocklists.length - 1; i >= 0; i--) {
                List<StockItem> stocklist = datedstocklists[i];
                for (StockItem stock : stocklist) {
                    String stockid = stock.getId();
                    int curYear = stock.getDate().getYear();
                    Integer thisYear = yearMap.get(stockid);
                    if (thisYear == null) {
                        thisYear = curYear;
                        yearMap.put(stockid, thisYear);
                    }
                    if (curYear != thisYear) {
                        Double basenumber = basenumberMap.get(stockid);
                        if (basenumber == null) {
                            basenumber = 1.0;
                        }
                        Double lastnumber = lastnumberMap.get(stockid);
                        if (lastnumber != null) {
                            basenumberMap.put(stockid, lastnumber);
                        } else {
                            basenumberMap.put(stockid, basenumber);                            
                        }
                        yearMap.put(stockid, curYear);
                    }
                    Double[] value = StockDao.getValue(stock, periodInt);
                    for (int ii = 0; ii < value.length; ii++ ) {
                        if (value[ii] != null) {
                            value[ii] = 0.01 * value[ii] + 1;
                            Double basenumber = basenumberMap.get(stockid);
                            if (basenumber == null) {
                                basenumber = 1.0;
                            }
                            value[ii] = value[ii] * basenumber;
                        }
                        if (value != null && ii == 0) {
                            lastnumberMap.put(stockid, value[ii]);
                            if (value[ii] == null) {
                                int jj = 0;
                            }
                        }
                        if (value[ii] != null && ii == 0) {
                            //lastnumberMap.put(stockid, value[ii]);
                        }
                    }
                    MapUtil.mapAdd(retMap, stockid, datedstocklists.length - 1 - i, value, datedstocklists.length);
                }
            }
        }
        return Collections.unmodifiableMap(retMap);
    }

    public static Map<String, Object[][]> getVolumes(MyMyConfig conf, String market, String date, Integer periodInt, int count, int mytableintervaldays,
            Map<String, MarketData> marketdataMap, boolean currentyear) throws Exception {
        Map<String, Object[][]> retMap = new HashMap<>();
        List<StockItem> datedstocklists[] = marketdataMap.get(market).datedstocklists;
        for (int i = datedstocklists.length - 1; i >= 0; i--) {
            List<StockItem> stocklist = datedstocklists[i];
            for (StockItem stock : stocklist) {
                String stockid = stock.getId();
                Pair<Long, String> value = StockDao.getVolume(stock);
                Object[] value2 = new Object[] { value.getLeft(), value.getRight() };
                MapUtil.mapAdd(retMap, stockid, datedstocklists.length - 1 - i, value2, datedstocklists.length);
            }
        }
        return retMap;
    }

}

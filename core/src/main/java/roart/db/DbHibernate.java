package roart.db;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.model.Meta;
import roart.model.MetaItem;
import roart.model.Stock;
import roart.model.StockItem;
import roart.util.ArraysUtil;
import roart.util.TaUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbHibernate {

    private static Logger log = LoggerFactory.getLogger(DbHibernate.class);
    
    public DbHibernate() {
    }

    public static List<StockItem> getAll(String market) throws Exception {
    	long time0 = System.currentTimeMillis();
    	List<Stock> stocks = Stock.getAll(market);
		List<StockItem> stockitems = new ArrayList<>();
		for (Stock stock : stocks) {
			StockItem stockItem = new StockItem(stock.getDbid(), stock.getMarketid(), stock.getId(), stock.getName(), stock.getDate(), stock.getIndexvalue(), stock.getIndexvaluelow(), stock.getIndexvaluehigh(), stock.getPrice(), stock.getPricelow(), stock.getPricehigh(), stock.getVolume(), stock.getCurrency(), stock.getPeriod1(), stock.getPeriod2(), stock.getPeriod3(), stock.getPeriod4(), stock.getPeriod5(), stock.getPeriod6(), stock.getPeriod7(), stock.getPeriod8(), stock.getPeriod9());
			stockitems.add(stockItem);
		}
		log.info("time0 " + (System.currentTimeMillis() - time0));
		return stockitems;
    }

    public static MetaItem getMarket(String market) throws Exception {
		Meta meta = Meta.getById(market);
		return new MetaItem(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9());
    }
    
    @Deprecated
    public static Map<String, Object[]> doCalculationsArr(MyConfig conf, Map<String, Double[]> listMap, String key, Indicator indicator, boolean wantPercentizedPriceIndex) {
        Map<String, Object[]> objectMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            //Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            Double [] list = listMap.get(id);
            if (wantPercentizedPriceIndex) {
                list = ArraysUtil.getPercentizedPriceIndex(list, key);
            }
            log.info("beg end " + id + " "+ key);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.info("list " + list.length + " " + Arrays.asList(list));
            //double momentum = tu.getMom(list, conf.getDays());
            //Object[] objs = (Object[]) indicator.calculate(ArraysUtil.getNonNull(list));
            //objectMap.put(id, objs);
        }
        return objectMap;
    }
    public static Map<String, Object[]> doCalculationsArrNonNull(MyConfig conf, Map<String, double[][]> listMap, String key, Indicator indicator, boolean wantPercentizedPriceIndex) {
        Map<String, Object[]> objectMap = new HashMap<>();
        for (String id : listMap.keySet()) {
            //Double[] list = ArraysUtil.getArrayNonNullReverse(listMap.get(id));
            double [][] list = listMap.get(id);
            if ("F00000HGSN".equals(id)) {              
                log.info("braz " + Arrays.toString(list));                
            }
           if (wantPercentizedPriceIndex && list.length > 0 && list[0].length > 0) {
               double first = list[0][0];
               for(int i = 0; i < list.length; i ++)
                list[i] = ArraysUtil.getPercentizedPriceIndex(list[i], key, indicator.getCategory(), first);
            }
           if ("F00000HGSN".equals(id)) {              
               log.info("braz " + Arrays.toString(list));                
           }
            log.info("beg end " + id + " "+ key);
            //System.out.println("beg end " + begOfArray.value + " " + endOfArray.value);
            log.info("list " + list.length + " " + Arrays.asList(list));
            //double momentum = tu.getMom(list, conf.getDays());
            if (list.length == 180) {
                log.info("180");
            } else {
                log.info("not");
            }
            Object[] objs = (Object[]) indicator.calculate(list);
            if ("F00000HGSN".equals(id)) {
                log.info("braz " + Arrays.asList(list));
            }
            objectMap.put(id, objs);
        }
        return objectMap;
    }
}


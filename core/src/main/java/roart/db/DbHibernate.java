package roart.db;

import roart.model.Meta;
import roart.model.MetaItem;
import roart.model.Stock;
import roart.model.StockItem;

import java.util.ArrayList;
import java.util.List;

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
			StockItem stockItem = new StockItem(stock.getDbid(), stock.getMarketid(), stock.getId(), stock.getName(), stock.getDate(), stock.getIndexvalue(), stock.getPrice(), stock.getCurrency(), stock.getPeriod1(), stock.getPeriod2(), stock.getPeriod3(), stock.getPeriod4(), stock.getPeriod5(), stock.getPeriod6());
			stockitems.add(stockItem);
		}
		log.info("time0 " + (System.currentTimeMillis() - time0));
		return stockitems;
    }

    public static MetaItem getMarket(String market) throws Exception {
		Meta meta = Meta.getById(market);
		return new MetaItem(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6());
    }
}


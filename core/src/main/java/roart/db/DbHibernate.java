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
		List<Stock> stocks = Stock.getAll(market);
		List<StockItem> stockitems = new ArrayList<>();
		for (Stock stock : stocks) {
			StockItem stockItem = new StockItem(stock);
			stockitems.add(stockItem);
		}
		return stockitems;
    }

    public static MetaItem getMarket(String market) throws Exception {
		Meta meta = Meta.getById(market);
		return new MetaItem(meta);
    }
}


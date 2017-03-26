package roart.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.MetaItem;
import roart.model.StockItem;

public class DbSparkAccess extends DbAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public List<StockItem> getAll(String market) {
		return DbSpark.getAll(market);
	}

	@Override
	public MetaItem getMarket(String market) {
		return DbSpark.getMarket(market);
	}


}


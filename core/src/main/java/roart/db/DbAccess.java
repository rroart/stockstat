package roart.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.model.MetaItem;
import roart.model.StockItem;

public abstract class DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract List<StockItem> getAll(String market) throws Exception;

	public abstract MetaItem getMarket(String market) throws Exception;

}


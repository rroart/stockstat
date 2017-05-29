package roart.db;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.model.MetaItem;
import roart.model.StockItem;

public class DbSparkAccess extends DbAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static DbAccess instance;
	
	@Override
	public List<StockItem> getAll(String market) throws Exception {
		return DbSpark.getAll(market);
	}

	@Override
	public MetaItem getMarket(String market) {
		return DbSpark.getMarket(market);
	}

    @Override
    public Map<String, Object[]> doCalculationsArr(MyConfig conf, Map<String, Double[]> listMap, String key,
            Indicator indicator, boolean wantPercentizedPriceIndex) {
        return DbSpark.doCalculationsArr(listMap, key, indicator, wantPercentizedPriceIndex);
    }

    public static DbAccess instance(MyConfig conf) {
        if (instance == null) {
            instance = new DbHibernateAccess();
            new DbSpark(conf);
        }
        return instance;
    }

}


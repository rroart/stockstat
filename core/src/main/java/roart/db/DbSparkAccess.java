package roart.db;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
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
    public Map<String, Object[]> doCalculationsArr(MyMyConfig conf, Map<String, double[][]> listMap, String key,
            Indicator indicator, boolean wantPercentizedPriceIndex) {
        return DbSpark.doCalculationsArrNonNull(listMap, key, indicator, wantPercentizedPriceIndex);
    }

    public static DbAccess instance(MyMyConfig conf) {
        if (instance == null) {
            instance = new DbSparkAccess();
            new DbSpark(conf);
        }
        return instance;
    }

}


package roart.db;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyConfig;
import roart.indicator.Indicator;
import roart.model.MetaItem;
import roart.model.StockItem;

public class DbHibernateAccess extends DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
    public List<StockItem> getAll(String market) throws Exception {
    	return DbHibernate.getAll(market);
    }

	@Override
	public MetaItem getMarket(String market) throws Exception {
		return DbHibernate.getMarket(market);
	}

    @Override
    public Map<String, Object[]> doCalculationsArr(MyConfig conf, Map<String, Double[]> listMap, String key,
            Indicator indicator, boolean wantPercentizedPriceIndex) {
        return DbHibernate.doCalculationsArr(conf, listMap, key, indicator, wantPercentizedPriceIndex);
    }


}


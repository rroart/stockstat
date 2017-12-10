package roart.db;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.config.MyMyConfig;
import roart.indicator.Indicator;
import roart.model.MetaItem;
import roart.model.StockItem;

public class DbHibernateAccess extends DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static DbAccess instance;
    
    @Override
    public List<StockItem> getAll(String market) throws Exception {
    	return DbHibernate.getAll(market);
    }

	@Override
	public MetaItem getMarket(String market) throws Exception {
		return DbHibernate.getMarket(market);
	}

    @Override
    public Map<String, Object[]> doCalculationsArr(MyMyConfig conf, Map<String, double[][]> listMap, String key,
            Indicator indicator, boolean wantPercentizedPriceIndex) {
        return DbHibernate.doCalculationsArrNonNull(conf, listMap, key, indicator, wantPercentizedPriceIndex);
    }

    public static DbAccess instance() {
        if (instance == null) {
            instance = new DbHibernateAccess();
        }
        return instance;
    }


}


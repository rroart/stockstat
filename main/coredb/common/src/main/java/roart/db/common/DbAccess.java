package roart.db.common;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.model.MetaItem;
import roart.model.StockItem;
import roart.pipeline.common.Calculatable;

public abstract class DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract List<StockItem> getAll(String market) throws Exception;

	public abstract MetaItem getMarket(String market) throws Exception;

    public abstract Map<String, Object[]> doCalculationsArr(MyMyConfig conf, Map<String, double[][]> listMap, String key,
            Calculatable indicator, boolean wantPercentizedPriceIndex);

}


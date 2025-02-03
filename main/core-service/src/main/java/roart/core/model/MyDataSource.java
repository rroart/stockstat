package roart.core.model;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.model.MetaItem;
import roart.model.data.StockData;
import roart.pipeline.impl.ExtraReader;

public abstract class MyDataSource {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public abstract StockData getStockData();
    
    public abstract StockData getStockData(String market);

    public abstract Map<String, StockData> getExtraStockData(ExtraReader extraReader);

    public abstract List<MetaItem> getMetas();

}

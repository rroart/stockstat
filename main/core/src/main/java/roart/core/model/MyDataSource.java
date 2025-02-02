package roart.core.model;

import java.util.Map;
import roart.model.data.StockData;
import roart.pipeline.impl.ExtraReader;

public abstract class MyDataSource {
    public abstract StockData getStockData();
    
    public abstract Map<String, StockData> getExtraStockData(ExtraReader extraReader);
}

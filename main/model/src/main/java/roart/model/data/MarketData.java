package roart.model.data;

import java.util.List;
import java.util.Map;

import roart.common.model.MetaItem;
import roart.model.StockItem;

public class MarketData {
    public List<StockItem> stocks;
    public String[] periodtext;
    public List<StockItem>[] datedstocklists;
    public MetaItem meta;
}

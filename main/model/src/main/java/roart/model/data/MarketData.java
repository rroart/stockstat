package roart.model.data;

import java.util.List;
import java.util.Map;

import roart.common.model.MetaDTO;
import roart.common.model.StockDTO;

public class MarketData {
    public List<StockDTO> stocks;
    public String[] periodtext;
    public List<StockDTO>[] datedstocklists;
    public MetaDTO meta;
}

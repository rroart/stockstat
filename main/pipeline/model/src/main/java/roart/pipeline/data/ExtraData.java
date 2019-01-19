package roart.pipeline.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

import roart.category.AbstractCategory;
import roart.model.StockItem;
import roart.pipeline.Pipeline;

public class ExtraData {
    public List<Date> dateList;
    public Map<Pair<String, String>, List<StockItem>> pairStockMap;
    public Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap;
    public int category;
    public Map<Pair<String, String>, String> pairCatMap;
    public AbstractCategory[] categories;
    public Pipeline[] datareaders;

    public ExtraData(List<Date> dateList, Map<Pair<String, String>, List<StockItem>> pairStockMap,
            Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap, int period,
            Map<Pair<String, String>, String> pairCatMap, AbstractCategory[] categories, Pipeline[] datareaders) {
        this.dateList = dateList;
        this.pairStockMap = pairStockMap;
        this.pairDateMap = pairDateMap;
        this.category = period;
        this.pairCatMap = pairCatMap;
        this.categories = categories;
        this.datareaders = datareaders;
    }
}

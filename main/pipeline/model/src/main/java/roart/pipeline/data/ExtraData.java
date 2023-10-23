package roart.pipeline.data;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import roart.category.AbstractCategory;
import roart.common.model.StockItem;
import roart.common.pipeline.data.PipelineData;
import roart.pipeline.Pipeline;

public class ExtraData {
    public List<String> dateList;
    //public Map<Pair<String, String>, List<StockItem>> pairStockMap;
    //public Map<Pair<String, String>, Map<Date, StockItem>> pairDateMap;
    public int category;
    //public Map<Pair<String, String>, String> pairCatMap;
    public AbstractCategory[] categories;
    public PipelineData[] datareaders;
    public PipelineData extrareader;
    
    public ExtraData(List<String> dateList,
            int period,
            AbstractCategory[] categories, PipelineData[] datareaders, PipelineData extrareader) {
        this.dateList = dateList;
        this.category = period;
        this.categories = categories;
        this.datareaders = datareaders;
        this.extrareader = extrareader;
    }
}

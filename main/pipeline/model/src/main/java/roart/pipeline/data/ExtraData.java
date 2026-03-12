package roart.pipeline.data;

import java.util.List;

import roart.common.pipeline.data.SerialPipeline;

public class ExtraData {
    public List<String> dateList;
    //public Map<Pair<String, String>, List<StockDTO>> pairStockMap;
    //public Map<Pair<String, String>, Map<Date, StockDTO>> pairDateMap;
    //public Map<Pair<String, String>, String> pairCatMap;
    public SerialPipeline datareaders;
    public SerialPipeline extrareader;
    
    public ExtraData(List<String> dateList,
                     SerialPipeline datareaders,
                     SerialPipeline extrareader) {
        this.dateList = dateList;
        this.datareaders = datareaders;
        this.extrareader = extrareader;
    }
}

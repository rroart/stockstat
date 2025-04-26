package roart.model.data;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import roart.common.model.StockDTO;

/*
@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
        */
public class StockData {

    public String[] periodText;
    public Map<String, MarketData> marketdatamap;
    public List<StockDTO>[] datedstocklists;
    public List<String> stockdates;
    public Map<String, List<StockDTO>> stockdatemap;
    public Map<String, String> idNameMap;
    public Integer cat;
    public String catName;
    public List<StockDTO> datedstocks;
    public Integer days;
    public Map<String, List<StockDTO>> stockidmap;

    public StockData() {
        super();
    }
}

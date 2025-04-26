package roart.common.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
//import roart.model.data.StockData;
//import roart.pipeline.impl.ExtraReader;

public abstract class MyDataSource {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    /*
    public abstract StockData getStockData();
    
    public abstract StockData getStockData(String market);

    public abstract Map<String, StockData> getExtraStockData(ExtraReader extraReader);
    */
    
    public abstract List<MetaDTO> getMetas() throws Exception;

    public abstract List<StockDTO> getAll(String market, IclijConfig conf, boolean disableCache) throws Exception;

    public MetaDTO getById(String market, IclijConfig conf) throws Exception {
        List<MetaDTO> metas = getMetas();
        for (MetaDTO item : metas) {
            if (market.equals(item.getMarketid())) {
                return item;
            }
        }
        return null;
    }

    public List<StockDTO> getAll(String type, String language) throws Exception {
        //  TODO Auto-generated method stub
        return null;
    }

    public List<String> getDates(String market, IclijConfig conf) throws Exception {
        //  TODO Auto-generated method stub
        return null;
    }

    public List<String> getMarkets() throws Exception {
        //  TODO Auto-generated method stub
        return null;
    }
}

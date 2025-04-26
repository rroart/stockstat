package roart.db.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import roart.common.model.MetaItem;
import roart.common.model.MyDataSource;
import roart.common.model.StockItem;
import roart.db.spring.DbSpringDS;
import roart.iclij.config.IclijConfig;

public class DbDao {

    private MyDataSource dataSource;
    
    private IclijConfig iclijConfig;
    
    @Autowired
    public DbDao(IclijConfig iclijConfig, MyDataSource dataSource) {
        this.iclijConfig = iclijConfig;
        this.dataSource = dataSource;
    }
    
    public List<StockItem> getAll(String type, String language) throws Exception {
        return dataSource.getAll(language, iclijConfig, false); // TODO bool
    }

    public List<StockItem> getAll(String market, IclijConfig conf, boolean disableCache) throws Exception {
        return dataSource.getAll(market, iclijConfig, disableCache);      
    }

    public List<String> getDates(String market, IclijConfig conf) throws Exception {
        return dataSource.getDates(market, conf);
    }
    
    public List<String> getMarkets() throws Exception {
        return dataSource.getMarkets();
    }
    
    public List<MetaItem> getMetas() throws Exception {
        return dataSource.getMetas();
    }
    
    public MetaItem getById(String market, IclijConfig conf) throws Exception {
        return dataSource.getById(market, conf);
    }
}

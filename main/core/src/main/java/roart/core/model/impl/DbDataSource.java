package roart.core.model.impl;

import java.util.Map;

import roart.core.model.MyDataSource;
import roart.db.dao.DbDao;
import roart.etl.db.Extract;
import roart.iclij.config.IclijConfig;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.pipeline.impl.ExtraReader;

public class DbDataSource extends MyDataSource {

    private DbDao dbDao;
    private IclijConfig conf;
    
    public DbDataSource(DbDao dbDao, IclijConfig conf) {
        this.dbDao = dbDao;
        this.conf = conf;
    }
    
    @Override
    public StockData getStockData() {
        return new Extract(dbDao).getStockData(conf);
    }

    @Override
    public Map<String, StockData> getExtraStockData(ExtraReader extraReader) {
        return new IndicatorUtils().getExtraStockDataMap(conf, dbDao, extraReader);
    }

}

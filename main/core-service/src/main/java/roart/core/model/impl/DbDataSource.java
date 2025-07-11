package roart.core.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.common.model.MetaDTO;
import roart.common.model.MyDataSource;
import roart.common.model.StockDTO;
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
    
    /*
    @Override
    public StockData getStockData() {
        return new Extract(dbDao).getStockData(conf);
    }

    @Override
    public StockData getStockData(String market) {
        return new Extract(dbDao).getStockData(conf, market);
    }

    @Override
    public Map<String, StockData> getExtraStockData(ExtraReader extraReader) {
        return new IndicatorUtils().getExtraStockDataMap(conf, dbDao, extraReader);
    }
    */

    @Override
    public List<MetaDTO> getMetas() {
        try {
            return dbDao.getMetas();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);        
        }
        return null;
    }

    @Override
    public List<StockDTO> getAll(String market, IclijConfig conf, boolean disableCache) {
        try {
            return dbDao.getAll(market, conf, disableCache);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return new ArrayList<>();
        }        
    }
}

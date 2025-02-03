package roart.controller;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.model.MetaItem;
import roart.core.model.MyDataSource;
import roart.db.dao.DbDao;
import roart.etl.db.Extract;
import roart.iclij.config.IclijConfig;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.StockData;
import roart.pipeline.impl.ExtraReader;
import roart.testdata.TestConstants;
import roart.testdata.TestData;
import roart.common.util.TimeUtil;
import roart.common.constants.Constants;

public class TestDataSource extends MyDataSource {

    private IclijConfig conf;
    private Date startDate;
    private Date endDate;
    private  String marketName;
    private int size;
    boolean weekdays;
    private int column;
    private boolean ohlc;
    
    public TestDataSource(IclijConfig conf, Date startDate, Date endDate, String marketName, int size, boolean weekdays, int column,
            boolean ohlc) {
        super();
        this.conf = conf;
        this.startDate = startDate;
        this.endDate = endDate;
        this.marketName = marketName;
        this.size = size;
        this.weekdays = weekdays;
        this.column = column;
        this.ohlc = ohlc;
    }

    @Override
    public StockData getStockData() {
        try {
            return new TestData().getStockdata(conf, startDate, endDate, marketName, size, weekdays, column, ohlc);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public StockData getStockData(String market) {
        try {
            return new TestData().getStockdata(conf, startDate, endDate, market, size, weekdays, column, ohlc);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    @Override
    public Map<String, StockData> getExtraStockData(ExtraReader extraReader) {
        try {
            return new TestData().getExtraStockdataMap(conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }
    
    @Override
    public List<MetaItem> getMetas() {
        return new TestData().getMetas();
    }
    
}

package roart.etl.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.model.MyDataSource;
import roart.common.model.StockItem;
import roart.common.util.TimeUtil;
import roart.common.util.ValidateUtil;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.etl.MarketDataETL;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.stockutil.StockUtil;

public class Extract {

    private static Logger log = LoggerFactory.getLogger(Extract.class);

    public StockData getStockData(IclijConfig conf, boolean disableCache) {
        String market = conf.getConfigData().getMarket();
        return getStockData(conf, market, disableCache);
    }

    private DbDao dbDao;
    
    private MyDataSource dataSource;
    
    public Extract(DbDao dbDao) {
        super();
        this.dbDao = dbDao;
    }

    public Extract(MyDataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }
    
    public StockData getStockData(IclijConfig conf, String market, boolean disableCache) {
        List<StockItem> stocks = null;
        try {
            if (dbDao != null) {
                stocks = dbDao.getAll(market, conf, disableCache);
            } else {
                stocks = dataSource.getAll(market, conf, disableCache);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return null;
        }
        log.info("stocks {}", stocks.size());
        String[] periodText;
        if (dbDao != null) {
            periodText = DbDaoUtil.getPeriodText(market, conf, dbDao);
        } else {
            periodText = this.getPeriodText(market, conf, dataSource);
        }
        MetaItem meta = null;
        try {
            if (dbDao != null) {
                meta = dbDao.getById(market, conf);
            } else {
                meta = dataSource.getById(market, conf);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return getStockData(conf, market, stocks, meta, periodText);
    }

    public StockData getStockData(IclijConfig conf, String market, List<StockItem> stocks, MetaItem meta, String[] periodText) {    
        Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
        Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
        stockdatemap = StockUtil.filterFew(stockdatemap, conf.getFilterDate());
        if (conf.getConfigData().getDate() == null) {
            try {
                getCurrentDate(conf, stockdatemap);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    
        Integer days = conf.getDays();
    
        if (days == 0) {
            days = stockdatemap.keySet().size();
        }
        List<String> stockdates = new ArrayList<>(stockdatemap.keySet());
        Collections.sort(stockdates);
    
        Map<String, MarketData> marketdatamap = null;
        try {
            marketdatamap = new MarketDataETL().getMarketdatamap(days, market, conf, stocks, periodText, meta);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    
        Integer cat = null;
        try {
            cat = StockUtil.getWantedCategory(stocks, marketdatamap.get(market).meta);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        
        if (cat == null) {
            return null;
        }
        
        String catName = StockUtil.getCatName(cat, periodText);
        log.info("Category title {}", catName);
    
        ValidateUtil.validateSizes(stocks, marketdatamap.get(market).stocks);
        if (stocks.size() != marketdatamap.get(market).stocks.size()) {
            log.error("Sizes {} {}", stocks.size(), marketdatamap.get(market).stocks.size());
        }
        // temp hack
        Map<String, String> idNameMap = getIdNameMap(stockidmap);
    
        // the main list, based on freshest or specific date.
    
        /*
         * For all days with intervals
         * Make stock lists based on the intervals
         */
    
        List<StockItem>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getConfigData().getDate(), 2, conf.getTableMoveIntervalDays());
    
        List<StockItem> datedstocks = datedstocklists[0];
        if (datedstocks == null) {
            return null;
        }
        log.info("Datestocksize {}", datedstocks.size());
    
        StockData stockData = new StockData();
        stockData.marketdatamap = marketdatamap;
        stockData.periodText = periodText;
        stockData.datedstocklists = datedstocklists;
        stockData.stockdates = stockdates;
        stockData.stockidmap = stockidmap;
        stockData.stockdatemap = stockdatemap;
        stockData.idNameMap = idNameMap;
        stockData.cat = cat;
        stockData.catName = catName;
        stockData.datedstocks = datedstocks;
        stockData.days = days;
        
        return stockData;
    }

    private Map<String, String> getIdNameMap(Map<String, List<StockItem>> stockidmap) {
        Map<String, String> idNameMap = new HashMap<>();
        // sort based on date
        for (String key : stockidmap.keySet()) {
            List<StockItem> stocklist = stockidmap.get(key);
            stocklist.sort(StockUtil.StockDateComparator);
            idNameMap.put(key, stocklist.get(0).getName());
        }
        return idNameMap;
    }

    private void getCurrentDate(IclijConfig conf, Map<String, List<StockItem>> stockdatemap) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String date = null;
        TreeSet<String> set = new TreeSet<>(stockdatemap.keySet());
        List<String> list = new ArrayList<>(set);
        int size = list.size();
        if (size == 0) {
            int jj = 0;
        }
        date = list.get(size - 1);
        conf.getConfigData().setDate(TimeUtil.convertDate(dt.parse(date)));
        log.info("mydate2 {}", conf.getConfigData().getDate());
    }
    
    private static String[] getPeriodText(String market, IclijConfig conf, MyDataSource dataSource) {
        String[] periodText = { "Period1", "Period2", "Period3", "Period4", "Period5", "Period6", "Period7", "Period8", "Period9" };
        MetaItem meta = null;
        try {
            meta = dataSource.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        try {
            if (meta != null) {
                for (int i = 0; i < Constants.PERIODS; i++) {
                    if (meta.getPeriod(i) != null) {
                        periodText[i] = meta.getPeriod(i);
                    }
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return periodText;
    }


}
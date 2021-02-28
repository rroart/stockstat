package roart.etl.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.util.ValidateUtil;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.etl.MarketDataETL;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.stockutil.StockUtil;

public class Extract {

    private static Logger log = LoggerFactory.getLogger(Extract.class);

    public StockData getStockData(MyMyConfig conf) {
        String market = conf.getMarket();
        return getStockData(conf, market);
    }

    public StockData getStockData(MyMyConfig conf, String market) {
        List<StockItem> stocks = null;
        try {
            stocks = DbDao.getAll(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return null;
        }
        log.info("stocks {}", stocks.size());
        String[] periodText = DbDaoUtil.getPeriodText(market, conf);
        MetaItem meta = null;
        try {
            meta = DbDao.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    
        Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
        Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
        if (conf.getdate() == null) {
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
    
        List<StockItem>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());
    
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

    private void getCurrentDate(MyMyConfig conf, Map<String, List<StockItem>> stockdatemap) throws ParseException {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String date = null;
        TreeSet<String> set = new TreeSet<>(stockdatemap.keySet());
        List<String> list = new ArrayList<>(set);
        int size = list.size();
        if (size == 0) {
            int jj = 0;
        }
        date = list.get(size - 1);
        conf.setdate(dt.parse(date));
        log.info("mydate2 {}", conf.getdate());
    }

}
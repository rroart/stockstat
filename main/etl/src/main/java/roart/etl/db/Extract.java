package roart.etl.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.model.MetaDTO;
import roart.common.model.MyDataSource;
import roart.common.model.StockDTO;
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
    
    public Extract(DbDao dbDao) {
        super();
        this.dbDao = dbDao;
    }

    /*
    public Extract(MyDataSource dataSource) {
        super();
        this.dataSource = dataSource;
    }
    */

    public StockData getStockData(IclijConfig conf, String market, boolean disableCache) {
        String[] periodText;
        periodText = DbDaoUtil.getPeriodText(market, conf, dbDao);
        MetaDTO meta = null;
        try {
            meta = dbDao.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        // TODO not possible
        int batchSize = conf.getDbBatchsize();
        if (false && dbDao.hasStockBatch() && batchSize > 0) {
            return getStockDataBatch(conf, market, disableCache);
        }
        List<StockDTO> stocks = null;
        try {
            stocks = dbDao.getAll(market, conf, disableCache);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return null;
        }
        log.info("stocks {}", stocks.size());
        return getStockData(conf, market, stocks, meta, periodText);
    }

    public StockData getStockDataBatch(IclijConfig conf, String market, boolean disableCache) {
        String[] periodText;
        periodText = DbDaoUtil.getPeriodText(market, conf, dbDao);
        MetaDTO meta = null;
        try {
            meta = dbDao.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        int batchSize = conf.getDbBatchsize();
        List<StockDTO> stocks = null;
        StockData result = null;
        try {
                int batch = 0;
                while (true) {
                    log.info("batch {} {}", batch, batchSize);
                    List<StockDTO> batchStocks = dbDao.getAll(market, conf, disableCache, batch, batchSize);
                    if (batchStocks.isEmpty()) {
                        break;
                    }
                    StockData partial = getStockData(conf, market, batchStocks, meta, periodText);
                    if (partial != null) {
                        if (result == null) {
                            result = partial;
                        } else {
                            merge(result, partial, conf);
                        }
                    }
                    batch++;
                    log.info("batch {}", batch);
                }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (result == null) {
            return null;
        }
        log.info("stocks total {}", result.datedstocks.size());
        return result;
    }

    public StockData getStockData(IclijConfig conf, String market, List<StockDTO> stocks, MetaDTO meta, String[] periodText) {
        Map<String, List<StockDTO>> stockidmap = StockUtil.splitId(stocks);
        Map<String, List<StockDTO>> stockdatemap = StockUtil.splitDate(stocks);
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
            marketdatamap = new MarketDataETL().getMarketdatamap(days, market, conf, stocks, periodText, meta, new ArrayList<>(stockidmap.keySet()));
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
    
        List<StockDTO>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getConfigData().getDate(), 2, conf.getTableMoveIntervalDays());
    
        List<StockDTO> datedstocks = datedstocklists[0];
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
        log.info("Stockdates {} {} {} {}", conf.getConfigData().getMarket(), conf.getConfigData().getDate(), stockData.datedstocklists.length, stockData.stockdates.size());
        log.info("mystockdates {} {} {}", stockdates.size(), stockdatemap.keySet().size(), datedstocklists.length);        

        return stockData;
    }

    private Map<String, String> getIdNameMap(Map<String, List<StockDTO>> stockidmap) {
        Map<String, String> idNameMap = new HashMap<>();
        // sort based on date
        for (String key : stockidmap.keySet()) {
            List<StockDTO> stocklist = stockidmap.get(key);
            stocklist.sort(StockUtil.StockDateComparator);
            idNameMap.put(key, stocklist.get(0).getName());
        }
        return idNameMap;
    }

    private void getCurrentDate(IclijConfig conf, Map<String, List<StockDTO>> stockdatemap) throws ParseException {
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
        MetaDTO meta = null;
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

    // Github Copilot

    public void merge(StockData main, StockData partial, IclijConfig conf) {
        // merge stockidmap
        if (partial.stockidmap != null) {
            for (Entry<String, List<StockDTO>> entry : partial.stockidmap.entrySet()) {
                main.stockidmap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
            }
        }
        // merge stockdatemap
        if (partial.stockdatemap != null) {
            for (Entry<String, List<StockDTO>> entry : partial.stockdatemap.entrySet()) {
                main.stockdatemap.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).addAll(entry.getValue());
            }
        }
        // merge marketdatamap
        if (partial.marketdatamap != null && main.marketdatamap != null) {
            for (Entry<String, MarketData> entry : partial.marketdatamap.entrySet()) {
                String key = entry.getKey();
                MarketData partialMd = entry.getValue();
                MarketData mainMd = main.marketdatamap.get(key);
                if (mainMd == null) {
                    main.marketdatamap.put(key, partialMd);
                } else {
                    mainMd.stocks.addAll(partialMd.stocks);
                    for (int i = 0; i < partialMd.datedstocklists.length; i++) {
                        if (mainMd.datedstocklists[i] == null) {
                            mainMd.datedstocklists[i] = partialMd.datedstocklists[i];
                        } else if (partialMd.datedstocklists[i] != null) {
                            mainMd.datedstocklists[i].addAll(partialMd.datedstocklists[i]);
                        }
                    }
                }
            }
        }
        // merge datedstocklists
        if (partial.datedstocklists != null && main.datedstocklists != null) {
            for (int i = 0; false && i < Math.min(partial.datedstocklists.length, main.datedstocklists.length); i++) {
                if (main.datedstocklists[i] == null) {
                    main.datedstocklists[i] = partial.datedstocklists[i];
                } else if (partial.datedstocklists[i] != null) {
                    main.datedstocklists[i].addAll(partial.datedstocklists[i]);
                }
            }
            main.datedstocklists = StockUtil.getDatedstocklists(main.stockdatemap, conf.getConfigData().getDate(), 2, conf.getTableMoveIntervalDays());
            main.datedstocks = main.datedstocklists[0];
        }
        // merge stockdates
        if (partial.stockdates != null) {
            Set<String> dates = new HashSet<>(main.stockdates != null ? main.stockdates : new ArrayList<>());
            dates.addAll(partial.stockdates);
            main.stockdates = new ArrayList<>(dates);
            Collections.sort(main.stockdates);
        }
        // merge idNameMap
        if (partial.idNameMap != null) {
            if (main.idNameMap != null) {
                main.idNameMap.putAll(partial.idNameMap);
            }
        }
        // merge datedstocks
        if (partial.datedstocks != null && main.datedstocks != null) {
            log.info("len" + main.datedstocks.size() + " " + partial.datedstocks.size());
            //main.datedstocks.addAll(partial.datedstocks);
        }
    }

}

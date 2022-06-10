package roart.etl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.common.model.MetaItem;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.stockutil.StockUtil;

public class MarketDataETL {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * For a given set of markets
     * Create a map to the marketdata
     * the marketdata being the periodtexts, all stocks and 
     * datedstocklist
     * 
     * @param days
     * @param markets to iterate
     * @param conf
     * @param stocks TODO
     * @param periodText TODO
     * @param meta TODO
     * @return
     * @throws Exception
     */
    
    public Map<String, MarketData> getMarketdatamap(int days,
            String market, MyMyConfig conf, List<StockItem> stocks, String[] periodText, MetaItem meta) throws Exception {
        Map<String, MarketData> marketdatamap = new HashMap();
        log.info("prestocks");
        log.info("stocks {}", stocks.size());
        MarketData marketdata = new MarketData();
        marketdata.stocks = stocks;
        marketdata.periodtext = periodText;
        marketdata.meta = meta;
        Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
        stockdatemap = StockUtil.filterFew(stockdatemap, conf.getFilterDate());
        System.out.println("grr " + stockdatemap.keySet());
        // the main list, based on freshest or specific date.

        /*
         * For all days with intervals
         * Make stock lists based on the intervals
         */

        List<StockItem> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), days, conf.getTableIntervalDays());
        marketdata.datedstocklists = datedstocklists;
        marketdatamap.put(market,  marketdata);
        return marketdatamap;
    }

}

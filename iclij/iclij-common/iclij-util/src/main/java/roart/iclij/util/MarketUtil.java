package roart.iclij.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.config.ActionComponentConfig;

public class MarketUtil {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Market findMarket(String market) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Market> markets = getMarkets(instance);
        Market foundMarket = null;
        for (Market aMarket : markets) {
            if (market.equals(aMarket.getConfig().getMarket())) {
                foundMarket = aMarket;
                break;
            }
        }
        return foundMarket;
    }
    
    private List<Market> getMarkets(IclijConfig instance) {
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    public List<Market> getMarkets(boolean isDataset) {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        List<Market> markets = new ArrayList<>();
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        markets = new MarketUtil().filterMarkets(markets, isDataset);
        return markets;
    }

    public List<Market> filterMarkets(List<Market> markets, boolean isDataset) {
        List<Market> filtered = new ArrayList<>();
        for (Market market : markets) {
            Boolean dataset = market.getConfig().getDataset();
            boolean adataset = dataset != null && dataset;
            if (adataset == isDataset) {
                filtered.add(market);
            }
        }
        return filtered;
    }

    public List<MemoryItem> getMarketMemory(Market market) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAll(market.getConfig().getMarket());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

    public List<MemoryItem> getMarketMemory(Market market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) {
        List<MemoryItem> marketMemory = null;
        try {
            marketMemory = IclijDbDao.getAllMemories(market.getConfig().getMarket(), action, component, subcomponent, parameters, startDate, endDate);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return marketMemory;
    }

}

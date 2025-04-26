package roart.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.constants.Constants;
import roart.common.model.MetaItem;
import roart.common.model.MyDataSource;
import roart.common.model.StockItem;
import roart.iclij.config.IclijConfig;

public class TestDataSources extends MyDataSource {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private List<TestDataSource> testDataSources;
    
    private List<MetaItem> metas = new ArrayList<>();
    
    private Map<String, List<StockItem>> stockMap = new HashMap<>();

    public TestDataSources(List<TestDataSource> testDataSources) {
        super();
        this.testDataSources = testDataSources;
        for (TestDataSource testDataSource : testDataSources) {
            metas.addAll(testDataSource.getMetas());
            stockMap.put(testDataSource.marketName, testDataSource.getAll(testDataSource.marketName, null, true));
        }
    }

    @Override
    public List<MetaItem> getMetas() {
        return metas;
    }

    @Override
    public List<StockItem> getAll(String market, IclijConfig conf, boolean disableCache) {
        if (false) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        String key = CacheConstants.STOCKS + market + conf.getConfigData().getDate();
        log.info("StockItem getall {}", key);
        List<StockItem> list = (List<StockItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        list = stockMap.get(market);
        if (!disableCache) {
            MyCache.getInstance().put(key, list);
        } else {
            log.info("Cache disabled for {}", key.hashCode());
        }
        return list;
    }

    @Override
    public List<StockItem> getAll(String type, String language) throws Exception {
        log.error("Should not be here");
        return null;
    }

    @Override
    public List<String> getDates(String market, IclijConfig conf) throws Exception {
        log.error("Should not be here");
        return null;
    }

    @Override
    public List<String> getMarkets() throws Exception {
        log.error("Should not be here");
        return null;
    }

    @Override
    public MetaItem getById(String market, IclijConfig conf) throws Exception {
        return metas.stream().filter(d -> d.getMarketid().equals(market)).findFirst().orElse(null);
    }

}

package roart.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.config.MyXMLConfig;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.common.cache.MyCache;
import roart.db.common.DbAccess;
import roart.db.dao.util.StockETL;
import roart.db.hibernate.DbHibernateAccess;
import roart.db.spring.DbSpringAccess;
import roart.db.spark.DbSpark;
import roart.db.spark.DbSparkAccess;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.config.MyXMLConfig;

@Component
public class DbDao {
    private static Logger log = LoggerFactory.getLogger(DbDao.class);

    @Autowired
    MyXMLConfig conf;

    private DbAccess access = null;

    DbSpringAccess dbSpringAccess;

    @Autowired
    public DbDao(DbSpringAccess dbSpringAccess) {
        MyMyConfig instance = new MyMyConfig(MyXMLConfig.getConfigInstance());
        boolean hibernate = instance.wantDbHibernate();

        this.dbSpringAccess = dbSpringAccess;
        if (hibernate) {
            access = new DbHibernateAccess();
        } else {
            access = dbSpringAccess;
        }
        log.info("Hibernate enabled: {}", hibernate);
   }
    
    public List<StockItem> getAll(String type, String language) throws Exception {
        if (access == null) {
            return null;
        }
        long time0 = System.currentTimeMillis();
        List<StockItem> list = access.getStocksByMarket(type);
        log.info("StockItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    /*
    @Deprecated
    public Map<String, Object[]> doCalculationsArr(MyMyConfig conf, Map<String, double[][]> listMap, String key, AbstractIndicator indicator, boolean wantPercentizedPriceIndex) {
        if (access == null) {
            return null;
        }
        return access.doCalculationsArr(conf, listMap, key, indicator, wantPercentizedPriceIndex);
    }
    */

    public List<StockItem> getAll(String market, MyMyConfig conf) throws Exception {
        String key = CacheConstants.STOCKS + market + conf.getdate();
        log.info("StockItem getall {}", key);
        List<StockItem> list = (List<StockItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        list = new ArrayList<>();
        long time0 = System.currentTimeMillis();
        list = access.getStocksByMarket(market);
        list = StockETL.filterWeekend(conf, list);
        log.info("StockItem getall {} {}", market, (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<String> getDates(String market, MyMyConfig conf) throws Exception {
        String key = CacheConstants.DATES + conf.getMarket() + conf.getdate();
        List<String> list =  (List<String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        List<Date> dates = access.getDates(market);
        if (conf.getdate() != null) {
        dates = dates.stream().filter(e -> !e.after(conf.getdate())).collect(Collectors.toList());
        }
        List<String> retlist = StockETL.filterWeekendConvert(conf, dates);
        log.info("StockItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, retlist);
        return retlist;
    }

    public List<String> getMarkets() throws Exception {
        String key = CacheConstants.MARKETS;
        List<String> list =  (List<String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        list = access.getMarkets();
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MetaItem> getMetas() throws Exception {
        String key = CacheConstants.METAS;
        List<MetaItem> list =  (List<MetaItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        List<MetaItem> metas = access.getMetas();
        List<MetaItem> metaitems = new ArrayList<>();
        /*
        for (Meta meta : metas) {
            MetaItem metaItem = new MetaItem(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9(), meta.getPriority(), meta.getReset(), meta.isLhc());
            metaitems.add(metaItem);
        }
        */
        log.info("time0 " + (System.currentTimeMillis() - time0));
        MyCache.getInstance().put(key, metas);
        return metas;
    }

    public MetaItem getById(String market, MyMyConfig conf) throws Exception {
        System.out.println("mymarket " + market);
        return access.getMetaByMarket(market);
    }
}

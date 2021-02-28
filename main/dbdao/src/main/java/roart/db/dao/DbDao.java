package roart.db.dao;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.model.MetaItem;
import roart.common.cache.MyCache;
import roart.db.common.DbAccess;
import roart.db.hibernate.DbHibernateAccess;
import roart.db.model.Meta;
import roart.db.model.Stock;
import roart.db.spark.DbSpark;
import roart.db.spark.DbSparkAccess;
import roart.model.StockItem;
import roart.etl.StockETL;

public class DbDao {
    private static Logger log = LoggerFactory.getLogger(DbDao.class);

    private static DbAccess access = null;

    public static void instance(String type, MyMyConfig conf) {
        System.out.println("instance " + type);
        log.info("instance " + type);
        if (type == null) {
            return;
        }
        if (true || access == null) {
            if (type.equals(MLConstants.SPARK)) {
                access = new DbSparkAccess();
                new DbSpark(conf);
            }
            if (type.equals(ConfigConstants.HIBERNATE)) {
                access = new DbHibernateAccess();
            }
        }
    }

    public static DbAccess instance(MyMyConfig conf) {
        if (false && conf.wantDbSpark()) {
            return DbSparkAccess.instance(conf);
        }
        if (true || conf.wantDbHibernate()) {
            return DbHibernateAccess.instance();
        }
        System.out.println("ret null");
        return null;
    }

    public static List<StockItem> getAll(String type, String language) throws Exception {
        if (access == null) {
            return null;
        }
        long time0 = System.currentTimeMillis();
        List<StockItem> list = access.getAll(type);
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

    public static List<StockItem> getAll(String market, MyMyConfig conf) throws Exception {
        String key = CacheConstants.STOCKS + market + conf.getdate();
        List<StockItem> list =  (List<StockItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = DbDao.instance(conf).getAll(market);
        list = StockETL.filterWeekend(conf, list);
        log.info("StockItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<String> getDates(String market, MyMyConfig conf) throws Exception {
        String key = CacheConstants.DATES + conf.getMarket() + conf.getdate();
        List<String> list =  (List<String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        List<Date> dates = Stock.getDates(market);
        List<String> retlist = StockETL.filterWeekendConvert(conf, dates);
        log.info("StockItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, retlist);
        return retlist;
    }

    public static List<String> getMarkets() throws Exception {
        String key = CacheConstants.MARKETS;
        List<String> list =  (List<String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        list = Stock.getMarkets();
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<MetaItem> getMetas() throws Exception {
        String key = CacheConstants.METAS;
        List<MetaItem> list =  (List<MetaItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        List<Meta> metas = Meta.getAll();
        List<MetaItem> metaitems = new ArrayList<>();
        for (Meta meta : metas) {
            MetaItem metaItem = new MetaItem(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9(), meta.getPriority(), meta.getReset(), meta.isLhc());
            metaitems.add(metaItem);
        }
        log.info("time0 " + (System.currentTimeMillis() - time0));
        MyCache.getInstance().put(key, metaitems);
        return metaitems;
    }

    public static MetaItem getById(String market, MyMyConfig conf) throws Exception {
        System.out.println("mymarket " + market);
        return DbDao.instance(conf).getMarket(market);
    }
}

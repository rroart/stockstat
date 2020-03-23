package roart.db.dao;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.model.MetaItem;
import roart.db.common.DbAccess;
import roart.db.hibernate.DbHibernateAccess;
import roart.db.model.Meta;
import roart.db.model.Stock;
import roart.db.spark.DbSpark;
import roart.db.spark.DbSparkAccess;
import roart.model.StockItem;
import roart.stockutil.StockUtil;

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
        long time0 = System.currentTimeMillis();
        List<StockItem> list = StockUtil.filterWeekend(conf, DbDao.instance(conf).getAll(market));
        log.info("StockItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<String> getMarkets() throws Exception {
        return Stock.getMarkets();
    }

    public static List<MetaItem> getMetas() throws Exception {
        long time0 = System.currentTimeMillis();
        List<Meta> metas = Meta.getAll();
                List<MetaItem> metaitems = new ArrayList<>();
                for (Meta meta : metas) {
                        MetaItem metaItem = new MetaItem(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9(), meta.getPriority(), meta.getReset(), meta.isLhc());
                        metaitems.add(metaItem);
                }
                log.info("time0 " + (System.currentTimeMillis() - time0));
                return metaitems;
    }

    public static MetaItem getById(String market, MyMyConfig conf) throws Exception {
        System.out.println("mymarket " + market);
        return DbDao.instance(conf).getMarket(market);
    }
}

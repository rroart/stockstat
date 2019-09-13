package roart.db.dao;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.db.common.DbAccess;
import roart.db.hibernate.DbHibernateAccess;
import roart.db.model.Stock;
import roart.db.spark.DbSpark;
import roart.db.spark.DbSparkAccess;
import roart.model.MetaItem;
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
        return access.getAll(type);
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
        return StockUtil.filterWeekend(conf, DbDao.instance(conf).getAll(market));
    }

    public static List<String> getMarkets() throws Exception {
        return Stock.getMarkets();
    }

    public static MetaItem getById(String market, MyMyConfig conf) throws Exception {
        System.out.println("mymarket " + market);
        return DbDao.instance(conf).getMarket(market);
    }
}

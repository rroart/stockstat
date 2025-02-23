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
import roart.common.config.ConfigConstantMaps;
import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.config.MyXMLConfig;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.common.util.TimeUtil;
import roart.common.cache.MyCache;
import roart.db.common.DbAccess;
import roart.db.dao.util.StockETL;
import roart.db.hibernate.DbHibernateAccess;
import roart.db.spring.DbSpringAccess;
import roart.iclij.config.IclijConfig;

@Component
public class DbDao {
    private static Logger log = LoggerFactory.getLogger(DbDao.class);
    
    private DbAccess access = null;

    DbSpringAccess dbSpringAccess;

    @Autowired
    public DbDao(IclijConfig iclijConfig, DbSpringAccess dbSpringAccess) {
        boolean hibernate = iclijConfig.wantDbHibernate();

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

    public List<StockItem> getAll(String market, IclijConfig conf) throws Exception {
        String key = CacheConstants.STOCKS + market + conf.getConfigData().getDate();
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

    public List<String> getDates(String market, IclijConfig conf) throws Exception {
        String key = CacheConstants.DATES + conf.getConfigData().getMarket() + conf.getConfigData().getDate();
        List<String> list =  (List<String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        List<Date> dates = access.getDates(market);
        if (conf.getConfigData().getDate() != null) {
            Date confDate = TimeUtil.convertDate(conf.getConfigData().getDate());
        dates = dates.stream().filter(e -> !e.after(confDate)).collect(Collectors.toList());
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
        List<MetaItem> metas = access.getAllMetas();
        List<MetaItem> metaitems = new ArrayList<>();
        /*
        for (Meta meta : metas) {
            MetaItem metaItem = new MetaItem(meta.getMarketid(), meta.getPeriod1(), meta.getPeriod2(), meta.getPeriod3(), meta.getPeriod4(), meta.getPeriod5(), meta.getPeriod6(), meta.getPeriod7(), meta.getPeriod8(), meta.getPeriod9(), meta.getPriority(), meta.getReset(), meta.isLhc());
            metaitems.add(metaItem);
        }
        */
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        MyCache.getInstance().put(key, metas);
        return metas;
    }

    public MetaItem getById(String market, IclijConfig conf) throws Exception {
        return access.getMetaByMarket(market);
    }
}

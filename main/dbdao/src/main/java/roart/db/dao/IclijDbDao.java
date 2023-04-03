package roart.db.dao;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import roart.common.util.TimeUtil;
import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.model.AboveBelowItem;
import roart.common.model.ActionComponentItem;
import roart.common.model.ConfigItem;
import roart.common.model.ContItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.RelationItem;
import roart.common.model.SimDataItem;
import roart.common.model.TimingBLItem;
import roart.common.model.TimingItem;
import roart.db.common.DbAccess;
import roart.db.hibernate.DbHibernateAccess;
import roart.db.spring.DbSpringAccess;

import org.springframework.stereotype.Component;

@Component
public class IclijDbDao {
    private static Logger log = LoggerFactory.getLogger(IclijDbDao.class);

    public static DbAccess badAccess;
    
    private DbAccess access;

    DbSpringAccess dbSpringAccess;

    @Autowired
    public IclijDbDao(DbSpringAccess dbSpringAccess) {
        this.dbSpringAccess = dbSpringAccess;
        //access = dbSpringAccess;
        access = new DbHibernateAccess();
        badAccess = access;
    }
    
    @Deprecated
    public List<MemoryItem> getAll() throws Exception {
        String key = CacheConstants.MEMORIES;
        List<MemoryItem> list =  (List<MemoryItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getMemories();
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MemoryItem> getAll(String type) throws Exception {
        String key = CacheConstants.MEMORIES + type;
        List<MemoryItem> list =  (List<MemoryItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list =  access.getMemoriesByMarket(type);
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MemoryItem> getAllMemories(String market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.MEMORIES + market + action + component + subcomponent + parameters + startDate + endDate;
        List<MemoryItem> list =  (List<MemoryItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getMemories(market, action, component, subcomponent, parameters, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<TimingItem> getAllTiming() throws Exception {
        String key = CacheConstants.TIMINGS;
        List<TimingItem> list =  (List<TimingItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getTimings();        
        log.info("TimingItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<TimingItem> getAllTiming(String market, String action, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.TIMINGS + market + action + startDate + endDate;
        List<TimingItem> list =  (List<TimingItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getTimings(market, action, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("TimingItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<RelationItem> getAllRelations() throws Exception {
        String key = CacheConstants.RELATIONS;
        List<RelationItem> list =  (List<RelationItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getRelations();        
        log.info("RelationItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<IncDecItem> getAllIncDecs() throws Exception {
        String key = CacheConstants.INCDECS;
        List<IncDecItem> list =  (List<IncDecItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getIncDecs();        
        log.info("IncDecItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<IncDecItem> getAllIncDecs(String market, LocalDate startDate, LocalDate endDate, String parameters) throws Exception {
        String key = CacheConstants.INCDECS + market + startDate + endDate + parameters;
        List<IncDecItem> list = (List<IncDecItem>) MyCache.getInstance().get(key);
        if (list == null) {
            long time0 = System.currentTimeMillis();
            list =  access.getIncDecs(market, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate), parameters);
            MyCache.getInstance().put(key, list);
            log.info("IncDecItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        }
        return list;
    }

    public List<ConfigItem> getAllConfigs(String market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.CONFIGS + market + action + component + subcomponent + parameters + startDate + endDate;
        List<ConfigItem> list =  (List<ConfigItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getConfigs(market, action, component, subcomponent, parameters, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("ConfigItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<ConfigItem> getAllConfigs(String market) throws Exception {
        String key = CacheConstants.CONFIGS + market;
        List<ConfigItem> list =  (List<ConfigItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getConfigsByMarket(market);        
        log.info("ConfigItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MLMetricsItem> getAllMLMetrics() throws Exception {
        String key = CacheConstants.MLMETRICS ;
        List<MLMetricsItem> list =  (List<MLMetricsItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getMLMetrics();        
        log.info("MLMetricsItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MLMetricsItem> getAllMLMetrics(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.MLMETRICS + market + startDate + endDate;
        List<MLMetricsItem> list =  (List<MLMetricsItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getMLMetrics(market, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("MLMetricsItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }
    
    public void save(Object object) {
        access.save(object);
    }

    public List<SimDataItem> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        return access.getAllSimData(market, startDate, endDate);
    }

    public List<AboveBelowItem> getAllAboveBelow(String market, Date startDate, Date endDate) {
        return access.getAllAboveBelow(market, startDate, endDate);
    }

    public void deleteById(Object object, String id) {
        access.deleteById(object, id);
    }

    public void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate) {
        access.delete(object, market, action, component, subcomponent, startDate, endDate);
    }

    public List<ActionComponentItem> getAllActionComponent() {
        return access.getAllActionComponent();
    }

    public List<TimingBLItem> getAllTimingBLItem() {
        return access.getAllTimingBLItem();
    }

    public List<ContItem> getAllCont() {
        return access.getAllContItem();
    }
}


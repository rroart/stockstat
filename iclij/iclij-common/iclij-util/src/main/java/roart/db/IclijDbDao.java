package roart.db;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.util.TimeUtil;
import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.RelationItem;
import roart.iclij.model.TimingItem;
import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;

public class IclijDbDao {
    private static Logger log = LoggerFactory.getLogger(IclijDbDao.class);

    public static List<MemoryItem> getAll() throws Exception {
        String key = CacheConstants.MEMORIES;
        List<MemoryItem> list =  (List<MemoryItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = MemoryItem.getAll();
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<MemoryItem> getAll(String type) throws Exception {
        String key = CacheConstants.MEMORIES + type;
        List<MemoryItem> list =  (List<MemoryItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list =  MemoryItem.getAll(type);
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<MemoryItem> getAllMemories(String market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.MEMORIES + market + action + component + subcomponent + parameters + startDate + endDate;
        List<MemoryItem> list =  (List<MemoryItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list =  MemoryItem.getAll(market, action, component, subcomponent, parameters, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<TimingItem> getAllTiming() throws Exception {
        String key = CacheConstants.TIMINGS;
        List<TimingItem> list =  (List<TimingItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = TimingItem.getAll();        
        log.info("TimingItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<TimingItem> getAllTiming(String market, String action, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.TIMINGS + market + action + startDate + endDate;
        List<TimingItem> list =  (List<TimingItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = TimingItem.getAll(market, action, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("TimingItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<RelationItem> getAllRelations() throws Exception {
        String key = CacheConstants.RELATIONS;
        List<RelationItem> list =  (List<RelationItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = RelationItem.getAll();        
        log.info("RelationItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<IncDecItem> getAllIncDecs() throws Exception {
        String key = CacheConstants.INCDECS;
        List<IncDecItem> list =  (List<IncDecItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = IncDecItem.getAll();        
        log.info("IncDecItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<IncDecItem> getAllIncDecs(String market, LocalDate startDate, LocalDate endDate, String parameters) throws Exception {
        String key = CacheConstants.INCDECS + market + startDate + endDate + parameters;
        List<IncDecItem> list = (List<IncDecItem>) MyCache.getInstance().get(key);
        if (list == null) {
            long time0 = System.currentTimeMillis();
            list =  IncDecItem.getAll(market, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate), parameters);
            MyCache.getInstance().put(key, list);
            log.info("IncDecItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        }
        return list;
    }

    public static List<ConfigItem> getAllConfigs(String market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.CONFIGS + market + action + component + subcomponent + parameters + startDate + endDate;
        List<ConfigItem> list =  (List<ConfigItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = ConfigItem.getAll(market, action, component, subcomponent, parameters, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("ConfigItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<ConfigItem> getAllConfigs(String market) throws Exception {
        String key = CacheConstants.CONFIGS + market;
        List<ConfigItem> list =  (List<ConfigItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = ConfigItem.getAll(market);        
        log.info("ConfigItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<MLMetricsItem> getAllMLMetrics() throws Exception {
        String key = CacheConstants.MLMETRICS ;
        List<MLMetricsItem> list =  (List<MLMetricsItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = MLMetricsItem.getAll();        
        log.info("MLMetricsItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public static List<MLMetricsItem> getAllMLMetrics(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.MLMETRICS + market + startDate + endDate;
        List<MLMetricsItem> list =  (List<MLMetricsItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = MLMetricsItem.getAll(market, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("MLMetricsItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }
}


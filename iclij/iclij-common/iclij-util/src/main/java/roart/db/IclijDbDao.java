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
        long time0 = System.currentTimeMillis();
        List<MemoryItem> list = MemoryItem.getAll();
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<MemoryItem> getAll(String type) throws Exception {
        long time0 = System.currentTimeMillis();
        List<MemoryItem> list =  MemoryItem.getAll(type);
        log.info("MemoryItem getall {}", (System.currentTimeMillis() - time0) / 1000);
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
        long time0 = System.currentTimeMillis();
        List<TimingItem> list = TimingItem.getAll();        
        log.info("TimingItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<TimingItem> getAllTiming(String market, String action, LocalDate startDate, LocalDate endDate) throws Exception {
        long time0 = System.currentTimeMillis();
        List<TimingItem> list = TimingItem.getAll(market, action, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("TimingItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<RelationItem> getAllRelations() throws Exception {
        long time0 = System.currentTimeMillis();
        List<RelationItem> list = RelationItem.getAll();        
        log.info("RelationItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<IncDecItem> getAllIncDecs() throws Exception {
        long time0 = System.currentTimeMillis();
        List<IncDecItem> list = IncDecItem.getAll();        
        log.info("IncDecItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<IncDecItem> getAllIncDecs(String market, LocalDate startDate, LocalDate endDate, String parameters) throws Exception {
        String key = CacheConstants.INCDEC + market + startDate + endDate + parameters;
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
        long time0 = System.currentTimeMillis();
        List<ConfigItem> list = ConfigItem.getAll(market, action, component, subcomponent, parameters, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("ConfigItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<ConfigItem> getAllConfigs(String market) throws Exception {
        long time0 = System.currentTimeMillis();
        List<ConfigItem> list = ConfigItem.getAll(market);        
        log.info("ConfigItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<MLMetricsItem> getAllMLMetrics() throws Exception {
        long time0 = System.currentTimeMillis();
        List<MLMetricsItem> list = MLMetricsItem.getAll();        
        log.info("MLMetricsItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }

    public static List<MLMetricsItem> getAllMLMetrics(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        long time0 = System.currentTimeMillis();
        List<MLMetricsItem> list = MLMetricsItem.getAll(market, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("MLMetricsItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }
}


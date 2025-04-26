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
import roart.common.config.ConfigConstantMaps;
import roart.common.model.AboveBelowDTO;
import roart.common.model.ActionComponentDTO;
import roart.common.model.ConfigDTO;
import roart.common.model.ContDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.RelationDTO;
import roart.common.model.SimDataDTO;
import roart.common.model.TimingBLDTO;
import roart.common.model.TimingDTO;
import roart.db.common.DbDS;
import roart.db.hibernate.DbHibernateDS;
import roart.db.spring.DbSpringDS;

import org.springframework.stereotype.Component;
import roart.iclij.config.IclijConfig;

@Component
public class IclijDbDao {
    private static Logger log = LoggerFactory.getLogger(IclijDbDao.class);

    // for gem
    public static DbDS badDS;
    
    private DbDS access;

    DbDS dbSpringDS;

    @Autowired
    public IclijDbDao(IclijConfig iclijConfig, DbDS dbSpringDS) {
        boolean hibernate = iclijConfig.wantDbHibernate();

        this.dbSpringDS = dbSpringDS;
        if (hibernate) {
            access = new DbHibernateDS();
        } else {
            access = dbSpringDS;
        }
        log.info("Hibernate enabled: {}", hibernate);
        badDS = access;
    }
    
    @Deprecated
    public List<MemoryDTO> getAll() throws Exception {
        String key = CacheConstants.MEMORIES;
        List<MemoryDTO> list =  (List<MemoryDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getAllMemories();
        log.info("MemoryDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MemoryDTO> getAll(String type) throws Exception {
        String key = CacheConstants.MEMORIES + type;
        List<MemoryDTO> list =  (List<MemoryDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list =  access.getMemoriesByMarket(type);
        log.info("MemoryDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MemoryDTO> getAllMemories(String market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.MEMORIES + market + action + component + subcomponent + parameters + startDate + endDate;
        List<MemoryDTO> list =  (List<MemoryDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getMemories(market, action, component, subcomponent, parameters, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));
        log.info("MemoryDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<TimingDTO> getAllTiming() throws Exception {
        String key = CacheConstants.TIMINGS;
        List<TimingDTO> list =  (List<TimingDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getAllTimings();        
        log.info("TimingDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<TimingDTO> getAllTiming(String market, String action, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.TIMINGS + market + action + startDate + endDate;
        List<TimingDTO> list =  (List<TimingDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getTimings(market, action, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("TimingDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<RelationDTO> getAllRelations() throws Exception {
        String key = CacheConstants.RELATIONS;
        List<RelationDTO> list =  (List<RelationDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getAllRelations();        
        log.info("RelationDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<IncDecDTO> getAllIncDecs() throws Exception {
        String key = CacheConstants.INCDECS;
        List<IncDecDTO> list =  (List<IncDecDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getAllIncDecs();        
        log.info("IncDecDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<IncDecDTO> getAllIncDecs(String market, LocalDate startDate, LocalDate endDate, String parameters) throws Exception {
        String key = CacheConstants.INCDECS + market + startDate + endDate + parameters;
        List<IncDecDTO> list = (List<IncDecDTO>) MyCache.getInstance().get(key);
        if (list == null) {
            long time0 = System.currentTimeMillis();
            list =  access.getIncDecs(market, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate), parameters);
            MyCache.getInstance().put(key, list);
            log.info("IncDecDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        }
        return list;
    }

    public List<ConfigDTO> getAllConfigs(String market, String action, String component, String subcomponent, String parameters, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.CONFIGS + market + action + component + subcomponent + parameters + startDate + endDate;
        List<ConfigDTO> list =  (List<ConfigDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getConfigs(market, action, component, subcomponent, parameters, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("ConfigDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<ConfigDTO> getAllConfigs(String market) throws Exception {
        String key = CacheConstants.CONFIGS + market;
        List<ConfigDTO> list =  (List<ConfigDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getConfigsByMarket(market);        
        log.info("ConfigDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MLMetricsDTO> getAllMLMetrics() throws Exception {
        String key = CacheConstants.MLMETRICS ;
        List<MLMetricsDTO> list =  (List<MLMetricsDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getAllMLMetrics();        
        log.info("MLMetricsDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }

    public List<MLMetricsDTO> getAllMLMetrics(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        String key = CacheConstants.MLMETRICS + market + startDate + endDate;
        List<MLMetricsDTO> list =  (List<MLMetricsDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        long time0 = System.currentTimeMillis();
        list = access.getMLMetrics(market, TimeUtil.convertDate(startDate), TimeUtil.convertDate(endDate));        
        log.info("MLMetricsDTO getall {}", (System.currentTimeMillis() - time0) / 1000);
        MyCache.getInstance().put(key, list);
        return list;
    }
    
    public void save(Object object) {
        access.save(object);
    }

    public List<SimDataDTO> getAllSimData(String market, LocalDate startDate, LocalDate endDate) {
        return access.getAllSimData(market, startDate, endDate);
    }

    public List<AboveBelowDTO> getAllAboveBelow(String market, Date startDate, Date endDate) {
        return access.getAllAboveBelow(market, startDate, endDate);
    }

    public void deleteById(Object object, String id) {
        access.deleteById(object, id);
    }

    public void delete(Object object, String market, String action, String component, String subcomponent, Date startDate, Date endDate) {
        access.delete(object, market, action, component, subcomponent, startDate, endDate);
    }

    public List<ActionComponentDTO> getAllActionComponent() {
        return access.getAllActionComponent();
    }

    public List<TimingBLDTO> getAllTimingBLDTO() {
        return access.getAllTimingBL();
    }

    public List<ContDTO> getAllCont() {
        return access.getAllConts();
    }
}


package roart.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.ConfigItem;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.RelationItem;
import roart.iclij.model.TimingItem;

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

    public static List<TimingItem> getAllTiming() throws Exception {
        long time0 = System.currentTimeMillis();
        List<TimingItem> list = TimingItem.getAll();        
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

    public static List<ConfigItem> getAllConfigs(String market) throws Exception {
        long time0 = System.currentTimeMillis();
        List<ConfigItem> list = ConfigItem.getAll(market);        
        log.info("ConfigItem getall {}", (System.currentTimeMillis() - time0) / 1000);
        return list;
    }
}


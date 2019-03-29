package roart.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;

public class IclijDbDao {
    private static Logger log = LoggerFactory.getLogger(IclijDbDao.class);

    public static List<MemoryItem> getAll() throws Exception {
        return MemoryItem.getAll();
    }

    public static List<MemoryItem> getAll(String type) throws Exception {
        return MemoryItem.getAll(type);
    }

    public static List<TimingItem> getAllTiming() throws Exception {
        return TimingItem.getAll();        
    }

}


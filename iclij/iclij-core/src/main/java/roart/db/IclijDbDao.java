package roart.db;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.model.MemoryItem;

public class IclijDbDao {
	private static Logger log = LoggerFactory.getLogger(IclijDbDao.class);

    public static List<MemoryItem> getAll() throws Exception {
        return MemoryItem.getAll();
    }

	public static List<MemoryItem> getAll(String type) throws Exception {
		return MemoryItem.getAll(type);
	}
}


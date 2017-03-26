package roart.db;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.config.ConfigConstants;
import roart.model.StockItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbDao {
	private static Logger log = LoggerFactory.getLogger(DbDao.class);

	private static DbAccess access = null;

	public static void instance(String type) {
		System.out.println("instance " + type);
		log.info("instance " + type);
		if (type == null) {
			return;
		}
		if (access == null) {
			if (type.equals(ConfigConstants.SPARK)) {
				access = new DbSparkAccess();
				new DbSpark();
			}
			if (type.equals(ConfigConstants.HIBERNATE)) {
				access = new DbHibernateAccess();
			}
		}
	}

	public static DbAccess instance() {
		return access;
	}

	public static List<StockItem> getAll(String type, String language) throws Exception {
		if (access == null) {
			return null;
		}
		return access.getAll(type);
	}

}

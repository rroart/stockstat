package roart.db;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

import roart.config.ConfigConstants;
import roart.model.ResultItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbDao {
    private static Logger log = LoggerFactory.getLogger(DbDao.class);

    private static DbAccess classify = null;

    public static void instance(String type) {
	System.out.println("instance " + type);
	log.info("instance " + type);
	if (type == null) {
	  return;
	}
	if (classify == null) {
	    if (type.equals(ConfigConstants.SPARK)) {
		classify = new DbSparkAccess();
	    }
        if (type.equals(ConfigConstants.SPARK)) {
        classify = new DbSparkAccess();
        }
	}
    }

    public static String classify(String type, String language) {
	if (classify == null) {
	    return null;
	}
	return classify.classify(type, language);
    }

}

package roart.db;

import roart.model.ResultItem;

import java.util.List;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSparkAccess extends DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public String classify(String type, String language) {
	return DbDao.classify(type, language);
    }


}


package roart.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DbSparkAccess extends DbAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public String classify(String type, String language) {
	return DbDao.classify(type, language);
    }


}


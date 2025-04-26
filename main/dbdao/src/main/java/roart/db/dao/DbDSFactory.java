package roart.db.dao;

import org.springframework.stereotype.Component;

import roart.db.hibernate.DbHibernateDS;
import roart.db.spring.DbSpringDS;
import roart.common.config.ConfigConstants;
import roart.db.common.DbDS;

@Component
public class DbDSFactory {
    public static DbDS get(String db, DbSpringDS dbSpringDS) {
        if (db.equals(ConfigConstants.DATABASEHIBERNATE)) {
            return new DbHibernateDS();
        }
        if (db.equals(ConfigConstants.DATABASESPRING)) {
            return dbSpringDS;
        }
        return null;
    }
}

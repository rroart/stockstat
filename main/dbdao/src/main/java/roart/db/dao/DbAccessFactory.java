package roart.db.dao;

import org.springframework.stereotype.Component;

import roart.db.hibernate.DbHibernateAccess;
import roart.db.spring.DbSpringAccess;
import roart.common.config.ConfigConstants;
import roart.db.common.DbAccess;

@Component
public class DbAccessFactory {
    public static DbAccess get(String db, DbSpringAccess dbSpringAccess) {
        if (db.equals(ConfigConstants.DATABASEHIBERNATE)) {
            return new DbHibernateAccess();
        }
        if (db.equals(ConfigConstants.DATABASESPRING)) {
            return dbSpringAccess;
        }
        return null;
    }
}

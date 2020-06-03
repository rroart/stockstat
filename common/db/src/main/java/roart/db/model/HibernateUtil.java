package roart.db.model;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.util.MathUtil;

public class HibernateUtil {
    private static Logger log = LoggerFactory.getLogger(HibernateUtil.class);

    private static SessionFactory factory = buildSessionFactory();

    private Session sessionRead = null;

    private Transaction transactionRead = null;

    private Session sessionWrite = null;

    private Transaction transactionWrite = null;

    public HibernateUtil(boolean write) {
        try {
            if (write) {
                openSessionWrite();
                beginTransactionWrite();
            } else {
                openSessionRead();
                beginTransactionRead();
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    /*
    private void getSessionTransaction() throws HibernateException, Exception {
        openSessionRead();
        beginTransactionRead();
    }
     */

    private void beginTransactionRead() {
        transactionRead = sessionRead.beginTransaction();
    }

    private void openSessionRead() {
        sessionRead = factory.getCurrentSession();
    }

    public void beginTransactionWrite() {
        if (transactionWrite == null) {
            transactionWrite = sessionWrite.beginTransaction();
        }
    }

    public void openSessionWrite() throws Exception {
        if (sessionWrite == null) {
            sessionWrite = factory.openSession();
        }
    }

    /*
    private static void beginTransactionRead() {
        if (transactionRead == null) {
            transactionRead = sessionRead.beginTransaction();
        }
    }
    */

    /*
    public void openSessionRead() throws Exception {
        if (sessionRead == null) {
            sessionRead = factory.openSession();
        }
    }
    */

    public Query createQuery(String query) {
        return sessionRead.createQuery(query);
    }

    private static SessionFactory buildSessionFactory() {
        SessionFactory aFactory = null;
        if (factory == null) {
            Configuration configuration = new Configuration().configure();
            String connectionUrl = System.getProperty("connection.url");
            if (connectionUrl != null) {
                configuration.setProperty("connection.url", connectionUrl);
                configuration.setProperty("hibernate.connection.url", connectionUrl);
            }
            aFactory = configuration.buildSessionFactory();
        }
        return aFactory;
    }

    public void commit() throws HibernateException, Exception {
        log.debug("Doing hibernate commit");
        if (transactionWrite != null) {
            transactionWrite.commit();
            transactionWrite = null;
        }
        if (sessionWrite != null && sessionWrite.isOpen()) {
            sessionWrite.close();
            sessionWrite = null;
        }
    }

    public <T> List<T> get(Query<T> query) throws Exception {
        synchronized (sessionRead) {
            long time = System.currentTimeMillis();
            List<T> list = query.list();
            transactionRead.commit();
            String queryString = query.getQueryString();
            log.info("Db time {}s size {} for {} ", MathUtil.round((double) (System.currentTimeMillis() - time) / 1000, 1), list.size(), queryString.substring(0, Math.min(queryString.length(), 32)));
            return list;
        }
    }

    public <T> List<T> get(String queryString) throws Exception {
        Query<T> query = sessionRead.createQuery(queryString);
        return get(query);
    }

    public void save(Object object) {
        sessionWrite.save(object);
    }

    public <T> T get(Class aClass, Serializable id) throws HibernateException, Exception {
        synchronized (sessionRead) {
            T result = (T) sessionRead.get(aClass, id);
            transactionRead.commit();
            return result;
        }
    }
}

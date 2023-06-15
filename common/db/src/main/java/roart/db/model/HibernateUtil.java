package roart.db.model;

import java.io.Serializable;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;
import org.hibernate.query.SelectionQuery;
import org.hibernate.resource.transaction.spi.TransactionStatus;
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

    public HibernateUtil(Boolean write) {
        try {
            if (write == null || !write) {
                openSessionRead();
                beginTransactionRead();
            }
            if (write == null || write) {
                if (write == null) {
                    sessionWrite = sessionRead;
                    transactionWrite = transactionRead;
                } else {
                    openSessionWrite();
                    beginTransactionWrite();
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            //e.printStackTrace();
        }
    }

    /*
    private void getSessionTransaction() throws HibernateException, Exception {
        openSessionRead();
        beginTransactionRead();
    }
     */

    private void beginTransactionRead() {
        //transactionRead = sessionRead.beginTransaction();
        transactionRead = sessionRead.getTransaction();
        if (transactionRead.getStatus() == TransactionStatus.NOT_ACTIVE) {
            transactionRead = sessionRead.beginTransaction();
        }
        //System.out.println("se" + sessionRead);
        //System.out.println("tr" + transactionRead);
    }

    private void openSessionRead() {
        sessionRead = factory.getCurrentSession();
    }

    public void beginTransactionWrite() {
        if (transactionWrite == null) {
            transactionWrite = sessionWrite.beginTransaction();
        }
        //System.out.println("Vars " + transactionWrite + " " + sessionWrite);
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

    public SelectionQuery createQuery(String query) {
        return sessionRead.createSelectionQuery(query);
    }

    public MutationQuery createWriteQuery(String query) {
        return sessionWrite.createMutationQuery(query);
    }

    private static SessionFactory buildSessionFactory() {
        SessionFactory aFactory = null;
        if (factory == null) {
            Configuration configuration = new Configuration().configure();
            String connectionUrl = System.getProperty("connection.url");
            String username = System.getProperty("connection.username");
            String password = System.getProperty("connection.password");
            String driver = System.getProperty("connection.driver_class");
            String dialect = System.getProperty("connection.dialect");
            if (connectionUrl != null) {
                configuration.setProperty("connection.url", connectionUrl);
            }
            if (connectionUrl != null) {
                configuration.setProperty("hibernate.connection.url", connectionUrl);
            }
            if (username != null) {
                configuration.setProperty("hibernate.connection.username", username);
            }
            if (password != null) {
                configuration.setProperty("hibernate.connection.password", password);
            }
            if (driver != null) {
                configuration.setProperty("connection.driver_class", driver);
            }
            if (dialect != null) {
                configuration.setProperty("dialect", dialect);
            }
            aFactory = configuration.buildSessionFactory();
        }
        return aFactory;
    }

    public void commit() throws HibernateException, Exception {
        log.debug("Doing hibernate commit");
        //System.out.println("Doing hibernate commit");
        //System.out.println("Vars " + transactionWrite + " " + sessionWrite);
        if (transactionWrite != null) {
            transactionWrite.commit();
            transactionWrite = null;
        }
        if (sessionWrite != null && sessionWrite.isOpen()) {
            sessionWrite.close();
            sessionWrite = null;
        }
    }

    public <T> List<T> get(SelectionQuery<T> query) throws Exception {
        synchronized (sessionRead) {
            long time = System.currentTimeMillis();
            List<T> list = query.list();
            transactionRead.commit();
            String queryString = query.toString();
            log.info("Db time {}s size {} for {} ", MathUtil.round((double) (System.currentTimeMillis() - time) / 1000, 1), list.size(), queryString.substring(0, Math.min(queryString.length(), 32)));
            return list;
        }
    }

    public <T> List<T> get(String queryString) throws Exception {
        SelectionQuery<T> query = (SelectionQuery<T>) sessionRead.createSelectionQuery(queryString);
        return get(query);
    }

    public void save(Object object) {
        sessionWrite.persist(object);
    }

    public void saveOrUpdate(Object object) {
        sessionWrite.saveOrUpdate(object);
    }

    // only for Main
    public <T> T get(Class<T> aClass, Serializable id) throws HibernateException, Exception {
        synchronized (sessionRead) {
            //System.out.println("se" + sessionRead);
            //System.out.println("tr" + transactionRead);
            T result = (T) sessionRead.get(aClass, id);
            //System.out.println("se" + sessionRead);
            //System.out.println("tr" + transactionRead);
            //transactionRead.commit();
            return result;
        }
    }

    public void delete(String string) {
        MutationQuery query = sessionWrite.createMutationQuery(string);
        query.executeUpdate();
    }

    public void delete(Query query) {
        query.executeUpdate();
    }
}

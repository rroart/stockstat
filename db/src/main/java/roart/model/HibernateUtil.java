package roart.model;

import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// dummy
//import net.sf.ehcache.hibernate.EhCacheRegionFactory;

public class HibernateUtil {
    private static Logger log = LoggerFactory.getLogger(HibernateUtil.class);

    private static SessionFactory factory = null;
    private static Session session = null;
    private static Transaction transaction = null;

    public static Session getCurrentSession() throws /*MappingException,*/ HibernateException, Exception {
	return getHibernateSession();
    }

    public static Session currentSession() throws /*MappingException,*/ HibernateException, Exception {
	return getHibernateSession();
    }

    public static Session getHibernateSession() throws /*MappingException,*/ HibernateException, Exception {
	if (factory == null) {
		/*
	    AnnotationConfiguration configuration = new AnnotationConfiguration();
	    factory = configuration.configure().buildSessionFactory();*/
		/*
		Configuration configuration = new Configuration().configure();
		StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder().
				applySettings(configuration.getProperties());
		factory = configuration.buildSessionFactory(builder.build());
		*/
		factory = new Configuration().configure().buildSessionFactory();
	    //Object o = new net.sf.ehcache.hibernate.EhCacheRegionFactory();
	}

	if (session == null) {
	    //Session sess = factory.openSession();
	    session = factory.getCurrentSession();
	}

	if (session != null) {
	    if (!session.isOpen()) {
		session = factory.openSession();
	    }
	}

	if (transaction == null) {
	    transaction = session.beginTransaction();
	}

	return session;
    }

    public static void commit() throws /*MappingException,*/ HibernateException, Exception {
	log.info("Doing hibernate commit");
	if (transaction != null) {
	transaction.commit();
	transaction = null;
	}
	if (session != null && session.isOpen()) {
	    session.close();
	session = null;
	}
    }

    public static <T> List<T> convert(List l, Class<T> type) {
        return (List<T>)l;
    }

}

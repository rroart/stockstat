package roart.db.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

@Entity
@Table(name = "MLMetrics")
@org.hibernate.annotations.Table(appliesTo = "MLMetrics")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MLMetrics implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private Date record;
    
    @Column
    private Date date;
    
    @Column
    private String market;

    @Column
    private String component;
    
    @Column
    private String subcomponent;
    
    @Column
    private String localcomponent;
    
    @Column
    private Double testAccuracy;
    
    @Column
    private Double loss;
    
    @Column
    private Double threshold;

    public Date getRecord() {
        return record;
    }

    public void setRecord(Date record) {
        this.record = record;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    public String getLocalcomponent() {
        return localcomponent;
    }

    public void setLocalcomponent(String localcomponent) {
        this.localcomponent = localcomponent;
    }

    public Double getTestAccuracy() {
        return testAccuracy;
    }

    public void setTestAccuracy(Double testAccuracy) {
        this.testAccuracy = testAccuracy;
    }

    public Double getLoss() {
        return loss;
    }

    public void setLoss(Double loss) {
        this.loss = loss;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    @Transient
    @Transactional
    public void save() throws Exception {
        Session session = HibernateUtil.getMyHibernateSession();
        synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        session.save(this);
        transaction.commit();
        }
    }

    @Transient
    @Transactional
    public static List<MLMetrics> getAll() throws Exception {
        List<MLMetrics> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
        synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from MLMetrics").list();
        transaction.commit();
        }
        return list;
    }

    @Transient
    @Transactional
    public static List<MLMetrics> getAll(String mymarket) throws Exception {
        List<MLMetrics> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
        synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from MLMetrics where market = :mymarket").setParameter("mymarket",  mymarket).list();
        transaction.commit();
        }
        return list;
    }

    @Transient
    @Transactional
    public static List<MLMetrics> getAll(String market, Date startDate, Date endDate) throws Exception {
        List<MLMetrics> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
        synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        //String queryString = "from Memory where market = :market and action = :action and component = :component";
        String queryString = "from MLMetrics where market = :market";
        if (startDate != null) {
            queryString += " and date >= :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        Query query = session.createQuery(queryString);
        query.setParameter("market", market);
        //query.setParameter("action", action);
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        list = query.list();
        transaction.commit();
        }
        return list;
    }

}
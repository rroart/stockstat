package roart.db.model;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.transaction.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "IncDec")
@org.hibernate.annotations.Table(appliesTo = "IncDec")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class IncDec implements Serializable /*,Comparable<Meta>*/ {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private String market;

    @Column
    private boolean increase;

    @Column
    private String id;

    @Column
    private String name;

    @Column
    private String description;

    @Column
    private Double score;

    @Column
    private Date record;

    @Column
    private Date date;

    @Column
    private String parameters;
    
    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public boolean isIncrease() {
        return increase;
    }

    public void setIncrease(boolean increase) {
        this.increase = increase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description != null && description.length() > 250) {
            description = description.substring(0, 250);
        }
        this.description = description;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    @Transient
    @Transactional
    public static List<IncDec> getAll() throws Exception {
	List<IncDec> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from IncDec").list();
        transaction.commit();
	}
        return list;
    }

    @Transient
    @Transactional
    public static List<IncDec> getAll(String mymarket) throws Exception {
	List<IncDec> list = null;
	Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from IncDec where market = :mymarket").setParameter("mymarket",  mymarket).list();
        transaction.commit();
	}
        return list;
    }

    @Transient
    @Transactional
    public static List<IncDec> getAll(String market, Date startDate, Date endDate, String parameters) throws Exception {
        List<IncDec> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
        synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        //String queryString = "from Memory where market = :market and action = :action and component = :component";
        String queryString = "from IncDec where market = :market";
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
        if (parameters != null) {
            query.setParameter("parameters", parameters);
        }
        list = query.list();
        transaction.commit();
        }
        return list;
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

}

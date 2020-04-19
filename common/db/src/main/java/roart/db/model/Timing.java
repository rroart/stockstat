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
@Table(name = "Timing")
@org.hibernate.annotations.Table(appliesTo = "Timing")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Timing implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private String market;

    @Column
    private String mlmarket;

    @Column
    private String action;

    @Column
    private boolean evolve;
    
    @Column
    private String component;
    
    @Column
    private String subcomponent;
    
    @Column
    private String parameters;
    
    @Column
    private Double time;

    @Column
    private Double score;

    @Column
    private Date record;

    @Column
    private Date date;

    @Column
    private Boolean buy;
    
    @Column
    private String description;
    
    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getMlmarket() {
        return mlmarket;
    }

    public void setMlmarket(String mlmarket) {
        this.mlmarket = mlmarket;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }


    public boolean isEvolve() {
        return evolve;
    }

    public void setEvolve(boolean evolve) {
        this.evolve = evolve;
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
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

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    @Transactional
    public static List<Timing> getAll() throws Exception {
	List<Timing> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from Timing").list();
        transaction.commit();
	}
        return list;
    }

    @Transient
    @Transactional
    public static List<Timing> getAll(String mymarket) throws Exception {
	List<Timing> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from Timing where market = :mymarket").setParameter("mymarket",  mymarket).list();
        transaction.commit();
	}
        return list;
    }

    @Transient
    @Transactional
    public static List<Timing> getAll(String market, String action, Date startDate, Date endDate) throws Exception {
        List<Timing> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
        synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        //String queryString = "from Memory where market = :market and action = :action and component = :component";
        String queryString = "from Timing where market = :market and action = :action";
        if (startDate != null) {
            queryString += " and date >= :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        Query query = session.createQuery(queryString);
        query.setParameter("market", market);
        query.setParameter("action", action);
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

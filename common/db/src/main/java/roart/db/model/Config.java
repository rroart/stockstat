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
import javax.persistence.Transient;
import javax.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "Config")
@org.hibernate.annotations.Table(appliesTo = "Config")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Config implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private String market;

    @Column
    private String action;

    @Column
    private String component;

    @Column
    private String subcomponent;

    @Column
    private String id;

    @Column(length = 511)
    private String value;

    @Column
    private Double score;

    @Column
    private Date record;

    @Column
    private Date date;

    @Column
    private Boolean buy;
    
    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    @Transient
    @Transactional
    public static List<Config> getAll() throws Exception {
	List<Config> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from Config").list();
        transaction.commit();
	}
        return list;
    }

    @Transient
    @Transactional
    public static List<Config> getAll(String mymarket) throws Exception {
	List<Config> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from Config where market = :mymarket").setParameter("mymarket",  mymarket).list();
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

package roart.db.model;

import java.io.Serializable;
import java.time.LocalDate;
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
@Table(name = "Relation")
@org.hibernate.annotations.Table(appliesTo = "Relation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Relation implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private LocalDate record;

    @Column
    private String market;

    @Column
    private String id;

    @Column
    private String altId;

    @Column
    private String type;

    @Column
    private String otherMarket;

    @Column
    private String otherId;

    @Column
    private String otherAltId;

    @Column
    private Double value;

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOtherMarket() {
        return otherMarket;
    }

    public void setOtherMarket(String otherMarket) {
        this.otherMarket = otherMarket;
    }

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    public String getOtherAltId() {
        return otherAltId;
    }

    public void setOtherAltId(String otherAltId) {
        this.otherAltId = otherAltId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Transient
    @Transactional
    public static Relation ensureExistence() throws Exception {
        Relation fi = new Relation();
        HibernateUtil.currentSession().save(fi);
        return fi;
    }

    @Transient
    @Transactional
    public static List<Relation> getAll() throws Exception {
	List<Relation> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from Relation").list();
        transaction.commit();
	}
        return list;
    }

    @Transient
    @Transactional
    public static List<Relation> getAll(String mymarket) throws Exception {
	List<Relation> list = null;
        Session session = HibernateUtil.getMyHibernateSession();
	synchronized (HibernateUtil.class) {
        Transaction transaction = session.beginTransaction();
        list = session.createQuery("from Relation where market = :mymarket").setParameter("mymarket",  mymarket).list();
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

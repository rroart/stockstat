package roart.db.model;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

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

    @Column(length = 511)
    private String description;

    @Column
    private Double score;

    @Column
    private Date record;

    @Column
    private Date date;

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

    @Transient
    public static List<IncDec> getAll() throws Exception {
        Session session = HibernateUtil.getMyHibernateSession();
        Transaction transaction = session.beginTransaction();
        List<IncDec> list = session.createQuery("from IncDec").list();
        transaction.commit();
        return list;
    }

    @Transient
    public static List<IncDec> getAll(String mymarket) throws Exception {
        Session session = HibernateUtil.getMyHibernateSession();
        Transaction transaction = session.beginTransaction();
        List<IncDec> list = session.createQuery("from IncDec where market = :mymarket").setParameter("mymarket",  mymarket).list();
        transaction.commit();
        return list;
    }

    @Transient
    public void save() throws Exception {
        Session session = HibernateUtil.getMyHibernateSession();
        Transaction transaction = session.beginTransaction();
        session.save(this);
        transaction.commit();
    }

}

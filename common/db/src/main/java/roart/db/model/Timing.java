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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

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
    private String action;

    @Column
    private boolean evolve;
    
    @Column
    private String component;
    
    @Column
    private Double time;

    @Column
    private Date record;

    @Column
    private Date date;

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

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
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
    public static List<Timing> getAll() throws Exception {
        Session session = HibernateUtil.getMyHibernateSession();
        Transaction transaction = session.beginTransaction();
        List<Timing> list = session.createQuery("from Timing").list();
        transaction.commit();
        return list;
    }

    @Transient
    public static List<Timing> getAll(String mymarket) throws Exception {
        Session session = HibernateUtil.getMyHibernateSession();
        Transaction transaction = session.beginTransaction();
        List<Timing> list = session.createQuery("from Timing where market = :mymarket").setParameter("mymarket",  mymarket).list();
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

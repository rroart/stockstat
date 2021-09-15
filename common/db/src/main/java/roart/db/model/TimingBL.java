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
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.transaction.Transactional;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import roart.db.thread.Queues;

@Entity
@Table(name = "TimingBL")
@org.hibernate.annotations.Table(appliesTo = "TimingBL")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class TimingBL implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private LocalDate record;
    
    @Column
    private String id;
    
    @Column
    private int count;

    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
    
    @Transient
    @Transactional
    public static List<TimingBL> getAll() throws Exception {
        return new HibernateUtil(false).get("from TimingBL");
    }

    @Transient
    @Transactional
    public static List<TimingBL> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        Query<TimingBL> query = hu.createQuery("from TimingBL where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public static List<TimingBL> getAll(String market, String action, Date startDate, Date endDate) throws Exception {
        String queryString = "from TimingBL";
        if (startDate != null) {
            queryString += " and record > :startdate";
        }
        if (endDate != null) {
            queryString += " and record <= :enddate";
        }
        HibernateUtil hu = new HibernateUtil(false);
        Query<TimingBL> query = hu.createQuery(queryString);
        query.setParameter("market", market);
        query.setParameter("action", action);
        //query.setParameter("action", action);
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        return hu.get(query);
    }

    @Transient
    @Transactional
    public void save() throws Exception {
       Queues.queue.add(this);
    }
    
    @Transient
    @Transactional
    public static void delete(Long id) throws Exception {
        Queues.queuedelete.add("delete TimingBL where dbid = " + id);
    }

    @Transient
    @Transactional
    public static void delete(String id) {
        Queues.queuedelete.add("delete TimingBL where id = \"" + id + "\"");        
    }

}

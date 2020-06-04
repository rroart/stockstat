package roart.db.model;

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
import org.hibernate.query.Query;

import roart.db.thread.Queues;

@Entity
@Table(name = "Cont")
@org.hibernate.annotations.Table(appliesTo = "Cont")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Cont {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    public String md5;
    
    @Column
    public String filename;
    
    @Column
    public LocalDate date;    

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Transient
    @Transactional
    public static List<Cont> getAll() throws Exception {
        return new HibernateUtil(false).get("from Cont");
    }

    @Transient
    @Transactional
    public static List<Cont> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        Query<Cont> query = hu.createQuery("from Cont where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public void save() throws Exception {
        Queues.queue.add(this);
    }

}

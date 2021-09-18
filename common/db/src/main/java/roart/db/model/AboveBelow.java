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
@Table(name = "AboveBelow")
@org.hibernate.annotations.Table(appliesTo = "AboveBelow")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AboveBelow implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private LocalDate record;
    
    @Column
    private LocalDate date;
    
    @Column
    private String market;

    @Column
    private String components;
    
    @Column
    private String subcomponents;

    @Column
    private Double score;

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getSubcomponents() {
        return subcomponents;
    }

    public void setSubcomponents(String subcomponents) {
        this.subcomponents = subcomponents;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
    
    @Transient
    @Transactional
    public static List<AboveBelow> getAll(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        String queryString = "from AboveBelow where ";
        if (market != null) {
            queryString += " market = :market";
        } else {
            queryString += " market like '%'";
        }
        if (startDate != null) {
            queryString += " and date >= :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        Query<AboveBelow> query = hu.createQuery(queryString);
        if (market != null) {
            query.setParameter("market", market);
        }
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
    public static void delete(String market, Date startDate, Date endDate) throws Exception {
        String queryString = "delete AboveBelow where market = :market";
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        HibernateUtil hu = new HibernateUtil(true);
        Query<IncDec> query = hu.createWriteQuery(queryString);
        query.setParameter("market", market);
        //query.setParameter("action", action);
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        Queues.queuedeleteq.add(query);
    }
}

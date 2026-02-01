package roart.db.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;

import org.hibernate.query.SelectionQuery;
import roart.db.thread.Queues;

@Entity
@Table(name = "AboveBelow")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AboveBelow implements Serializable {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    @Column
    private Long dbid;

    @Column
    private LocalDate record;
    
    @Column
    private Date date;
    
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
    public static List<AboveBelow> getAll(String market, Date startDate, Date endDate) throws Exception {
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
        SelectionQuery<AboveBelow> query = hu.createQuery(queryString);
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
        MutationQuery query = hu.createWriteQuery(queryString);
        query.setParameter("market", market);
        //query.setParameter("action", action);
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        Queues.queuedeleteq.add(new ImmutablePair(hu, query));
    }
    
    @Transient
    @Transactional
    public static List<AboveBelow> getAll() throws Exception {
        return new HibernateUtil(false).get("from AboveBelow");
    }

}

package roart.db.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.Transient;
import javax.transaction.Transactional;
import roart.db.thread.Queues;

@Entity
@Table(name = "Sim")
@org.hibernate.annotations.Table(appliesTo = "Sim")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SimData implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private String market;

    @Column
    private LocalDate startdate;

    @Column
    private LocalDate enddate;

    @Column
    private Double score;
    
    @Column
    LocalDate record;
    
    @Column
    byte[] filter;

    @Column
    byte[] config;

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public LocalDate getStartdate() {
        return startdate;
    }

    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
    }

    public LocalDate getEnddate() {
        return enddate;
    }

    public void setEnddate(LocalDate enddate) {
        this.enddate = enddate;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public byte[] getFilter() {
        return filter;
    }

    public void setFilter(byte[] filter) {
        this.filter = filter;
    }

    public byte[] getConfig() {
        return config;
    }

    public void setConfig(byte[] config) {
        this.config = config;
    }

    @Transient
    @Transactional
    public static List<SimData> getAll(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        String queryString = "from Sim where ";
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
        Query<SimData> query = hu.createQuery(queryString);
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

}

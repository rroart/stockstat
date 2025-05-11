package roart.db.model;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.SelectionQuery;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import roart.db.thread.Queues;

@Entity
@Table(name = "SimRun")
@org.hibernate.annotations.Table(appliesTo = "SimRun")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class SimRunData {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    @Column
    private Long dbid;

    @Column
    private Long simdatadbid;

    @Column
    private LocalDate recorddate;

    @Column
    private String market;
        
    @Column
    private LocalDate startdate;
    
    @Column
    private LocalDate enddate;
    
    @Column
    private Double score;

    @Column
    private Double correlation;

    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public Long getSimdatadbid() {
        return simdatadbid;
    }

    public void setSimdatadbid(Long simdatadbid) {
        this.simdatadbid = simdatadbid;
    }

    public LocalDate getRecorddate() {
        return recorddate;
    }

    public void setRecorddate(LocalDate recorddate) {
        this.recorddate = recorddate;
    }

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

    public Double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(Double correlation) {
        this.correlation = correlation;
    }

    @Transient
    @Transactional
    public static List<SimRunData> getAll(String market, LocalDate startDate, LocalDate endDate) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        String queryString = "from SimData2 where ";
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
        SelectionQuery<SimRunData> query = hu.createQuery(queryString);
        if (market != null) {
            query.setParameter("market", market);
        }
        if (startDate != null) {
            query.setParameter("startdate", Date.from(startDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", Date.from(endDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()), TemporalType.DATE);
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
    public static List<SimRunData> getAll() throws Exception {
        return new HibernateUtil(false).get("from SimData2");
    }
}

package roart.db.model;

import java.io.Serializable;
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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import org.hibernate.query.SelectionQuery;
import roart.db.thread.Queues;

@Entity
@Table(name = "MLMetrics")
@org.hibernate.annotations.Table(appliesTo = "MLMetrics")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class MLMetrics implements Serializable {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    @Column
    private Long dbid;

    @Column
    private Date record;
    
    @Column
    private Date date;
    
    @Column
    private String market;

    @Column
    private String component;
    
    @Column
    private String subcomponent;
    
    @Column
    private String localcomponent;
    
    @Column
    private Double trainAccuracy;
    
    @Column
    private Double testAccuracy;
    
    @Column
    private Double valAccuracy;
    
    @Column
    private Double loss;
    
    @Column
    private Double threshold;

    @Column
    private String description;;
    
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

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
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

    public String getLocalcomponent() {
        return localcomponent;
    }

    public void setLocalcomponent(String localcomponent) {
        this.localcomponent = localcomponent;
    }

    public Double getTrainAccuracy() {
        return trainAccuracy;
    }

    public void setTrainAccuracy(Double trainAccuracy) {
        this.trainAccuracy = trainAccuracy;
    }

    public Double getTestAccuracy() {
        return testAccuracy;
    }

    public void setTestAccuracy(Double testAccuracy) {
        this.testAccuracy = testAccuracy;
    }

    public Double getValAccuracy() {
        return valAccuracy;
    }

    public void setValAccuracy(Double valAccuracy) {
        this.valAccuracy = valAccuracy;
    }

    public Double getLoss() {
        return loss;
    }

    public void setLoss(Double loss) {
        this.loss = loss;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    @Transactional
    public void save() throws Exception {
        Queues.queue.add(this);
    }

    @Transient
    @Transactional
    public static List<MLMetrics> getAll() throws Exception {
        return new HibernateUtil(false).get("from MLMetrics");
    }

    @Transient
    @Transactional
    public static List<MLMetrics> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<MLMetrics> query = hu.createQuery("from MLMetrics where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public static List<MLMetrics> getAll(String market, Date startDate, Date endDate) throws Exception {
        String queryString = "from MLMetrics where market = :market";
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<MLMetrics> query = hu.createQuery(queryString);
        query.setParameter("market", market);
        //query.setParameter("action", action);
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        return hu.get(query);
    }

}

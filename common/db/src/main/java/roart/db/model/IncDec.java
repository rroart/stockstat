package roart.db.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;

import org.hibernate.query.SelectionQuery;
import roart.db.thread.Queues;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "IncDec")
@org.hibernate.annotations.Table(appliesTo = "IncDec")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class IncDec implements Serializable /*,Comparable<Meta>*/ {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
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

    @Column
    private String description;

    @Column
    private Double score;

    @Column
    private Date record;

    @Column
    private Date date;

    @Column
    private String parameters;
    
    @Column
    private String component;
    
    @Column
    private String subcomponent;
    
    @Column
    private String localcomponent;
    
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
        if (description != null && description.length() > 250) {
            description = description.substring(0, 250);
        }
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
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

    @Transient
    @Transactional
    public static List<IncDec> getAll() throws Exception {
	return new HibernateUtil(false).get("from IncDec");
    }

    @Transient
    @Transactional
    public static List<IncDec> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<IncDec> query = hu.createQuery("from IncDec where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public static List<IncDec> getAll(String market, Date startDate, Date endDate, String parameters) throws Exception {
        String queryString = "from IncDec where market = :market";
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<IncDec> query = hu.createQuery(queryString);
        query.setParameter("market", market);
        //query.setParameter("action", action);
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        if (parameters != null) {
            query.setParameter("parameters", parameters);
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
    public static void delete(String market, String component, String subcomponent, Date startDate, Date endDate) throws Exception {
        String queryString = "delete IncDec where market = :market";
        if (component != null) {
            queryString += " and component = :component";
        }
        if (subcomponent != null) {
            queryString += " and subcomponent = :subcomponent";
        }
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
        if (component != null) {
            query.setParameter("component", component);
        }
        if (subcomponent != null) {
            query.setParameter("subcomponent", subcomponent);
        }
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        Queues.queuedeleteq.add(new ImmutablePair(hu, query));
    }
}

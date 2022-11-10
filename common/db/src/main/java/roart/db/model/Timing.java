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

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;

import org.hibernate.query.SelectionQuery;
import roart.db.thread.Queues;

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
    private String mlmarket;

    @Column
    private String action;

    @Column
    private boolean evolve;
    
    @Column
    private String component;
    
    @Column
    private String subcomponent;
    
    @Column
    private String parameters;
    
    @Column
    private Double time;

    @Column
    private Double score;

    @Column
    private Date record;

    @Column
    private Date date;

    @Column
    private Boolean buy;
    
    @Column
    private String description;
    
    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getMlmarket() {
        return mlmarket;
    }

    public void setMlmarket(String mlmarket) {
        this.mlmarket = mlmarket;
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

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
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

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
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

    @Transient
    @Transactional
    public static List<Timing> getAll() throws Exception {
	return new HibernateUtil(false).get("from Timing");
    }

    @Transient
    @Transactional
    public static List<Timing> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<Timing> query = hu.createQuery("from Timing where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public static List<Timing> getAll(String market, String action, Date startDate, Date endDate) throws Exception {
        String queryString = "from Timing where market = :market and action = :action";
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<Timing> query = hu.createQuery(queryString);
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
    public static void delete(String market, String action, String component, String subcomponent, Date startDate, Date endDate) throws Exception {
        String queryString = "delete Timing where market = :market";
        if (action != null) {
            queryString += " and action = :action";
        }
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
        if (action != null) {
            query.setParameter("action", action);
        }
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

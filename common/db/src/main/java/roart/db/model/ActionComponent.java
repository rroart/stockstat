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
@Table(name = "ActionComponent")
@org.hibernate.annotations.Table(appliesTo = "ActionComponent")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class ActionComponent implements Serializable {

    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long dbid;

    @Column
    private LocalDate record;

    @Column
    private String action;
    
    @Column
    private String component;
    
    @Column
    private String subcomponent;
    
    @Column
    private String market;

    @Column
    private Boolean buy;
    
    @Column
    private int priority;
    
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

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Transient
    @Transactional
    public static List<ActionComponent> getAll() throws Exception {
        return new HibernateUtil(false).get("from ActionComponent");
    }

    @Transient
    @Transactional
    public static List<ActionComponent> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        Query<ActionComponent> query = hu.createQuery("from ActionComponent where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public static List<ActionComponent> getAll(String market, String action, String component, String subcomponent, String parameters/*, Date startDate, Date endDate*/) throws Exception {
        String queryString = "from ActionComponent where market = :market and action = :action and component = :component";
        if (subcomponent != null) {
            queryString += " and subcomponent = :subcomponent";
        }
        if (parameters != null) {
            queryString += " and parameters = :parameters";
        }
        /*
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        */
        HibernateUtil hu = new HibernateUtil(false);
        Query<ActionComponent> query = hu.createQuery(queryString);
        query.setParameter("market", market);
        query.setParameter("action", action);
        query.setParameter("component", component);
        if (subcomponent != null) {
            query.setParameter("subcomponent", subcomponent);
        }
        if (parameters != null) {
            query.setParameter("parameters", parameters);
        }
        /*
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        */
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
        Queues.queuedelete.add("delete ActionComponent where dbid = " + id);
    }
}

package roart.db.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
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

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import roart.db.thread.Queues;

@Entity
@Table(name = "Memory")
@org.hibernate.annotations.Table(appliesTo = "Memory")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Memory implements Serializable {
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Id
    @Column
    private Long id;

    @Column
    private Date record;

    @Column
    private Date date;

    @Column
    private Integer usedsec;

    @Column
    private String action;
    
    @Column
    private String market;

    @Column
    private Double testaccuracy;

    @Column
    private Double testloss;

    @Column
    private Double confidence;

    @Column
    private Double learnConfidence;

    @Column
    private String category;

    @Column
    private String type;
    
    @Column
    private String component;

    @Column
    private String subcomponent;

    @Column
    private String localcomponent;

    @Column
    private String description;

    @Column
    private String info;

    @Column
    private Integer futuredays;

    @Column
    private Date futuredate;

    @Column
    private Long positives;

    @Column
    private Long size;

    @Column
    private Long abovepositives;
    
    @Column
    private Long abovesize;
    
    @Column
    private Long belowpositives;
    
    @Column
    private Long belowsize;
    
    @Column
    private String parameters;

    @Column
    private Long tp;

    @Column
    private Long tpSize;

    @Column
    private Double tpConf;

    @Column
    private Double tpProb;

    @Column
    private Double tpProbConf;

    @Column
    private Long tn;

    @Column
    private Long tnSize;

    @Column
    private Double tnConf;

    @Column
    private Double tnProb;

    @Column
    private Double tnProbConf;

    @Column
    private Long fp;

    @Column
    private Long fpSize;

    @Column
    private Double fpConf;

    @Column
    private Double fpProb;

    @Column
    private Double fpProbConf;

    @Column
    private Long fn;

    @Column
    private Long fnSize;

    @Column
    private Double fnConf;

    @Column
    private Double fnProb;

    @Column
    private Double fnProbConf;

    @Column
    private Integer position;

    public Memory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Integer getUsedsec() {
        return usedsec;
    }

    public void setUsedsec(Integer usedsec) {
        this.usedsec = usedsec;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public Double getTestaccuracy() {
        return testaccuracy;
    }

    public void setTestaccuracy(Double testaccuracy) {
        this.testaccuracy = testaccuracy;
    }

    public Double getTestloss() {
        return testloss;
    }

    public void setTestloss(Double testloss) {
        this.testloss = testloss;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public Double getLearnConfidence() {
        return learnConfidence;
    }

    public void setLearnConfidence(Double learnConfidence) {
        this.learnConfidence = learnConfidence;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Integer getFuturedays() {
        return futuredays;
    }

    public void setFuturedays(Integer futuredays) {
        this.futuredays = futuredays;
    }

    public Date getFuturedate() {
        return futuredate;
    }

    public void setFuturedate(Date futuredate) {
        this.futuredate = futuredate;
    }

    public Long getPositives() {
        return positives;
    }

    public void setPositives(Long positives) {
        this.positives = positives;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Long getAbovepositives() {
        return abovepositives;
    }

    public void setAbovepositives(Long abovepositives) {
        this.abovepositives = abovepositives;
    }

    public Long getAbovesize() {
        return abovesize;
    }

    public void setAbovesize(Long abovesize) {
        this.abovesize = abovesize;
    }

    public Long getBelowpositives() {
        return belowpositives;
    }

    public void setBelowpositives(Long belowpositives) {
        this.belowpositives = belowpositives;
    }

    public Long getBelowsize() {
        return belowsize;
    }

    public void setBelowsize(Long belowsize) {
        this.belowsize = belowsize;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public Long getTp() {
        return tp;
    }

    public void setTp(Long tp) {
        this.tp = tp;
    }

    public Long getTpSize() {
        return tpSize;
    }

    public void setTpSize(Long tpSize) {
        this.tpSize = tpSize;
    }

    public Double getTpConf() {
        return tpConf;
    }

    public void setTpConf(Double tpConf) {
        this.tpConf = tpConf;
    }

    public Double getTpProb() {
        return tpProb;
    }

    public void setTpProb(Double tpProb) {
        this.tpProb = tpProb;
    }

    public Double getTpProbConf() {
        return tpProbConf;
    }

    public void setTpProbConf(Double tpProbConf) {
        this.tpProbConf = tpProbConf;
    }

    public Long getTn() {
        return tn;
    }

    public void setTn(Long tn) {
        this.tn = tn;
    }

    public Long getTnSize() {
        return tnSize;
    }

    public void setTnSize(Long tnSize) {
        this.tnSize = tnSize;
    }

    public Double getTnConf() {
        return tnConf;
    }

    public void setTnConf(Double tnConf) {
        this.tnConf = tnConf;
    }

    public Double getTnProb() {
        return tnProb;
    }

    public void setTnProb(Double tnProb) {
        this.tnProb = tnProb;
    }

    public Double getTnProbConf() {
        return tnProbConf;
    }

    public void setTnProbConf(Double tnProbConf) {
        this.tnProbConf = tnProbConf;
    }

    public Long getFp() {
        return fp;
    }

    public void setFp(Long fp) {
        this.fp = fp;
    }

    public Long getFpSize() {
        return fpSize;
    }

    public void setFpSize(Long fpSize) {
        this.fpSize = fpSize;
    }

    public Double getFpConf() {
        return fpConf;
    }

    public void setFpConf(Double fpConf) {
        this.fpConf = fpConf;
    }

    public Double getFpProb() {
        return fpProb;
    }

    public void setFpProb(Double fpProb) {
        this.fpProb = fpProb;
    }

    public Double getFpProbConf() {
        return fpProbConf;
    }

    public void setFpProbConf(Double fpProbConf) {
        this.fpProbConf = fpProbConf;
    }

    public Long getFn() {
        return fn;
    }

    public void setFn(Long fn) {
        this.fn = fn;
    }

    public Long getFnSize() {
        return fnSize;
    }

    public void setFnSize(Long fnSize) {
        this.fnSize = fnSize;
    }

    public Double getFnConf() {
        return fnConf;
    }

    public void setFnConf(Double fnConf) {
        this.fnConf = fnConf;
    }

    public Double getFnProb() {
        return fnProb;
    }

    public void setFnProb(Double fnProb) {
        this.fnProb = fnProb;
    }

    public Double getFnProbConf() {
        return fnProbConf;
    }

    public void setFnProbConf(Double fnProbConf) {
        this.fnProbConf = fnProbConf;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Transient
    @Transactional
    public static List<Memory> getAll() throws Exception {
	return new HibernateUtil(false).get("from Memory");
    }

    @Transient
    @Transactional
    public static List<Memory> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        Query<Memory> query = hu.createQuery("from Memory where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public void save() throws Exception {
        Queues.queue.add(this);
    }

    @Transient
    @Transactional
    public static List<Memory> getAll(String market, String action, String component, String subcomponent, String parameters, Date startDate, Date endDate) throws Exception {
        String queryString = "from Memory where market = :market";
        if (action != null) {
            queryString += " and action = :action";
        }
        if (component != null) {
            queryString += " and component = :component";
        }
        if (subcomponent != null) {
            queryString += " and subcomponent = :subcomponent";
        }
        if (parameters != null) {
            queryString += " and parameters = :parameters";
        }
        if (startDate != null) {
            queryString += " and date > :startdate";
        }
        if (endDate != null) {
            queryString += " and date <= :enddate";
        }
        HibernateUtil hu = new HibernateUtil(false);
        Query<Memory> query = hu.createQuery(queryString);
        query.setParameter("market", market);
        if (action != null) {
            query.setParameter("action", action);
        }
        if (component != null) {
            query.setParameter("component", component);
        }
        if (subcomponent != null) {
            query.setParameter("subcomponent", subcomponent);
        }
        if (parameters != null) {
            query.setParameter("parameters", parameters);
        }
        if (startDate != null) {
            query.setParameter("startdate", startDate, TemporalType.DATE);
        }
        if (endDate != null) {
            query.setParameter("enddate", endDate, TemporalType.DATE);
        }
        return hu.get(query);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    @Transactional
    public static void delete(String market, String component, String subcomponent, Date startDate, Date endDate) throws Exception {
        String queryString = "delete Memory where market = :market";
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
        Query<IncDec> query = hu.createWriteQuery(queryString);
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
        Queues.queuedeleteq.add(query);
    }
}

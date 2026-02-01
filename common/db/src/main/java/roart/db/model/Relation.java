package roart.db.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;
import org.hibernate.query.SelectionQuery;

@Entity
@Table(name = "Relation")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Relation implements Serializable {

    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Id
    @Column
    private Long dbid;

    @Column
    private LocalDate record;

    @Column
    private String market;

    @Column
    private String id;

    @Column
    private String altId;

    @Column
    private String type;

    @Column
    private String otherMarket;

    @Column
    private String otherId;

    @Column
    private String otherAltId;

    @Column
    private Double value;

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAltId() {
        return altId;
    }

    public void setAltId(String altId) {
        this.altId = altId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOtherMarket() {
        return otherMarket;
    }

    public void setOtherMarket(String otherMarket) {
        this.otherMarket = otherMarket;
    }

    public String getOtherId() {
        return otherId;
    }

    public void setOtherId(String otherId) {
        this.otherId = otherId;
    }

    public String getOtherAltId() {
        return otherAltId;
    }

    public void setOtherAltId(String otherAltId) {
        this.otherAltId = otherAltId;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    @Transient
    @Transactional
    public static Relation ensureExistence(HibernateUtil hu) throws Exception {
        Relation fi = new Relation();
        hu.save(fi);
        return fi;
    }

    @Transient
    @Transactional
    public static List<Relation> getAll() throws Exception {
        return new HibernateUtil(false).get("from Relation");
    }

    @Transient
    @Transactional
    public static List<Relation> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<Relation> query = hu.createQuery("from Relation where market = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public void save() throws Exception {
        new HibernateUtil(false).save(this);
    }

}

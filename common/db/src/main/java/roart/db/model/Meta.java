package roart.db.model;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;
import org.hibernate.query.SelectionQuery;

@Entity
@Table(name = "Meta")
@org.hibernate.annotations.Table(appliesTo = "Meta")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Meta implements Serializable /*,Comparable<Meta>*/ {

    @Id
    @Column
    private String marketid;
    private String period1;
    @Column
    private String period2;
    @Column
    private String period3;
    @Column
    private String period4;
    @Column
    private String period5;
    @Column
    private String period6;
    @Column
    private String period7;
    @Column
    private String period8;
    @Column
    private String period9;
    @Column
    private String priority;
    @Column
    private String reset;
    @Column
    private Boolean lhc;
    
    public String getMarketid() {
        return marketid;
    }

    public void setMarketid(String marketid) {
        this.marketid = marketid;
    }

    public String getPeriod1() {
        return period1;
    }

    public void setPeriod1(String period1) {
        this.period1 = period1;
    }

    public String getPeriod2() {
        return period2;
    }

    public void setPeriod2(String period2) {
        this.period2 = period2;
    }

    public String getPeriod3() {
        return period3;
    }

    public void setPeriod3(String period3) {
        this.period3 = period3;
    }

    public String getPeriod4() {
        return period4;
    }

    public void setPeriod4(String period4) {
        this.period4 = period4;
    }

    public String getPeriod5() {
        return period5;
    }

    public void setPeriod5(String period5) {
        this.period5 = period5;
    }

    public String getPeriod6() {
        return period6;
    }

    public void setPeriod6(String period6) {
        this.period6 = period6;
    }

    public String getPeriod7() {
        return period7;
    }

    public void setPeriod7(String period7) {
        this.period7 = period7;
    }

    public String getPeriod8() {
        return period8;
    }

    public void setPeriod8(String period8) {
        this.period8 = period8;
    }

    public String getPeriod9() {
        return period9;
    }

    public void setPeriod9(String period9) {
        this.period9 = period9;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getReset() {
        return reset;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }

    public Boolean isLhc() {
        return lhc;
    }

    public void setLhc(Boolean lhc) {
        this.lhc = lhc;
    }

    @Transient
    @Transactional
    public static Meta ensureExistence(String id, HibernateUtil hu) throws Exception {
        Meta fi = getById(id);
        if (fi == null) {
            fi = new Meta();
            fi.setMarketid(id);
            hu.save(fi);
        }
        return fi;
    }

    @Transient
    @Transactional
    public static Meta getById(String id) throws Exception {
        return new HibernateUtil(false).get(Meta.class, id);                                           
    }

    @Transient
    @Transactional
    public static List<Meta> getAll() throws Exception {
        return new HibernateUtil(false).get("from Meta");
    }

    @Transient
    @Transactional
    public static List<String> getMarkets() throws Exception {
        return new HibernateUtil(false).get("select distinct (marketid) from Meta") ;
    }

    @Transient
    @Transactional
    public static List<Meta> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<Meta> query = hu.createQuery("from Meta where marketid = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

}

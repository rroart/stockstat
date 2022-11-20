package roart.db.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.hibernate.annotations.Index;
import org.hibernate.query.Query;
import org.hibernate.query.SelectionQuery;

@Entity
@Table(name = "Stock")
@org.hibernate.annotations.Table(appliesTo = "Stock")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Stock implements Serializable /*,Comparable<Stock>*/ {

    @Id
    @Column
    private String dbid;
    @Column
    private String marketid;
    @Column
    private String id;
    @Column
    private String isin;
    @Column
    private String name;
    @Column
    private Date date;
    @Column
    private Double indexvalue;
    @Column
    private Double indexvaluelow;
    @Column
    private Double indexvaluehigh;
    @Column
    private Double indexvalueopen;
    @Column
    private Double price;
    @Column
    private Double pricelow;
    @Column
    private Double pricehigh;
    @Column
    private Double priceopen;
    @Column
    private Long volume;
    @Column
    private String currency;
    @Column
    private Double period1;
    @Column
    private Double period2;
    @Column
    private Double period3;
    @Column
    private Double period4;
    @Column
    private Double period5;
    @Column
    private Double period6;
    @Column
    private Double period7;
    @Column
    private Double period8;
    @Column
    private Double period9;

    /*
	public Stock(String dbid, String id, String name, Date date, Double kurs, String currency, Double day, Double week, Double month, Double sofaryear) {
		this.dbid = dbid;
		this.id = id;
		this.name = name;
		this.date = date;
		this.kurs = kurs;
		this.currency = currency;
		this.day = day;
		this.week = week;
		this.month = month;
		this.sofaryear = sofaryear;
	}
     */

    public String getDbid() {
        return dbid;
    }

    public void setDbid(String dbid) {
        this.dbid = dbid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsin() {
        return isin;
    }

    public void setIsin(String isin) {
        this.isin = isin;
    }

    public String getMarketid() {
        return marketid;
    }

    public void setMarketid(String marketid) {
        this.marketid = marketid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getIndexvalue() {
        return indexvalue;
    }

    public void setIndexvalue(Double indexvalue) {
        this.indexvalue = indexvalue;
    }

    public Double getIndexvaluelow() {
        return indexvaluelow;
    }

    public void setIndexvaluelow(Double indexvaluelow) {
        this.indexvaluelow = indexvaluelow;
    }

    public Double getIndexvaluehigh() {
        return indexvaluehigh;
    }

    public void setIndexvaluehigh(Double indexvaluehigh) {
        this.indexvaluehigh = indexvaluehigh;
    }

    public Double getIndexvalueopen() {
        return indexvalueopen;
    }

    public void setIndexvalueopen(Double indexvalueopen) {
        this.indexvalueopen = indexvalueopen;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getPricelow() {
        return pricelow;
    }

    public void setPricelow(Double pricelow) {
        this.pricelow = pricelow;
    }

    public Double getPricehigh() {
        return pricehigh;
    }

    public void setPricehigh(Double pricehigh) {
        this.pricehigh = pricehigh;
    }

    public Double getPriceopen() {
        return priceopen;
    }

    public void setPriceopen(Double priceopen) {
        this.priceopen = priceopen;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getPeriod1() {
        return period1;
    }

    public void setPeriod1(Double period1) {
        this.period1 = period1;
    }

    public Double getPeriod2() {
        return period2;
    }

    public void setPeriod2(Double period2) {
        this.period2 = period2;
    }

    public Double getPeriod3() {
        return period3;
    }

    public void setPeriod3(Double period3) {
        this.period3 = period3;
    }

    public Double getPeriod4() {
        return period4;
    }

    public void setPeriod4(Double period4) {
        this.period4 = period4;
    }

    public Double getPeriod5() {
        return period5;
    }

    public void setPeriod5(Double period5) {
        this.period5 = period5;
    }

    public Double getPeriod6() {
        return period6;
    }

    public void setPeriod6(Double period6) {
        this.period6 = period6;
    }

    public Double getPeriod7() {
        return period7;
    }

    public void setPeriod7(Double period7) {
        this.period7 = period7;
    }

    public Double getPeriod8() {
        return period8;
    }

    public void setPeriod8(Double period8) {
        this.period8 = period8;
    }

    public Double getPeriod9() {
        return period9;
    }

    public void setPeriod9(Double period9) {
        this.period9 = period9;
    }

    @Transient
    @Transactional
    public static Stock ensureExistence(String dbid, HibernateUtil hu) throws Exception {
        Stock fi = getByDbid(dbid);
        if (fi == null) {
            fi = new Stock();
            fi.setDbid(dbid);
            hu.saveOrUpdate(fi);
        }
        return fi;
    }

    @Transient
    @Transactional
    public static Stock getByDbid(String dbid) throws Exception {
        return (Stock) new HibernateUtil(false).get(Stock.class, dbid);                                           
    }

    @Transient
    @Transactional
    public static List<Stock> getAll() throws Exception {
        return new HibernateUtil(false).get("from Stock");
    }

    @Transient
    @Transactional
    public static List<String> getMarkets() throws Exception {
        return new HibernateUtil(false).get("select distinct (marketid) from Stock");
    }

    @Transient
    @Transactional
    public static List<Stock> getAll(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<Stock> query = hu.createQuery("from Stock where marketid = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

    @Transient
    @Transactional
    public static List<Date> getDates(String mymarket) throws Exception {
        HibernateUtil hu = new HibernateUtil(false);
        SelectionQuery<Date> query = hu.createQuery("select distinct(date) from Stock where marketid = :mymarket").setParameter("mymarket",  mymarket);
        return hu.get(query);
    }

}

package roart.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.hibernate.annotations.Index;





import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

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
private String name;
@Column
private Date date;
@Column
private Double price;
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

public Double getPrice() {
	return price;
}

public void setPrice(Double price) {
	this.price = price;
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

@Transient
	public static Stock ensureExistence(String dbid) throws Exception {
        Stock fi = getByDbid(dbid);
        if (fi == null) {
            fi = new Stock();
            fi.setDbid(dbid);
            HibernateUtil.currentSession().save(fi);
        }
        return fi;
    }

    @Transient
        public static Stock getByDbid(String dbid) throws Exception {
        //return (Stock) HibernateUtil.getHibernateSession().createQuery("from Stock where dbid = :dbid").setParameter("dbid", dbid).uniqueResult();
        // this is slower:                                                  
        return (Stock) HibernateUtil.getHibernateSession().get(Stock.class, dbid);                                           
    }

    @Transient
        public static List<Stock> getAll() throws Exception {
        return HibernateUtil.convert(HibernateUtil.currentSession().createQuery("from Stock").list(), Stock.class);
    }

    @Transient
       public static List<String> getMarkets() throws Exception {
        return (List<String>) HibernateUtil.getHibernateSession().createQuery("select distinct (marketid) from Stock").list() ;
    }

    @Transient
        public static List<Stock> getAll(String mymarket) throws Exception {
        return (List<Stock>) HibernateUtil.convert(HibernateUtil.currentSession().createQuery("from Stock where marketid = :mymarket").setParameter("mymarket",  mymarket).list(), Stock.class);
    }

    /*
    @Transient
    public int compareTo(Stock arg0) {
        // TODO Auto-generated method stub
        return 0;
    }
    */
    
    //@Transient

}

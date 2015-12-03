package roart.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
//import org.hibernate.annotations.Index;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "Stock")
@org.hibernate.annotations.Table(appliesTo = "Stock")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Stock implements Serializable {

	@Id
@Column
private String dbid;
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
private Double day;
@Column
private Double month;
@Column
private Double week;
@Column
private Double thisyear;

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

public Double getDay() {
	return day;
}

public void setDay(Double day) {
	this.day = day;
}

public Double getMonth() {
	return month;
}

public void setMonth(Double month) {
	this.month = month;
}

public Double getWeek() {
	return week;
}

public void setWeek(Double week) {
	this.week = week;
}

public Double getThisyear() {
	return thisyear;
}

public void setThisyear(Double thisyear) {
	this.thisyear = thisyear;
}

	public static Stock ensureExistence(String dbid) throws Exception {
        Stock fi = getByDbid(dbid);
        if (fi == null) {
            fi = new Stock();
            fi.setDbid(dbid);
            HibernateUtil.currentSession().save(fi);
        }
        return fi;
    }

    public static Stock getByDbid(String dbid) throws Exception {
        //return (Stock) HibernateUtil.getHibernateSession().createQuery("from Stock where dbid = :dbid").setParameter("dbid", dbid).uniqueResult();
        // this is slower:                                                  
        return (Stock) HibernateUtil.getHibernateSession().get(Stock.class, dbid);                                           
    }

    public static List<Stock> getAll() throws Exception {
        return HibernateUtil.convert(HibernateUtil.currentSession().createQuery("from Stock").list(), Stock.class);
    }
}

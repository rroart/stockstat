package roart.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roart.db.DbDao;
import roart.util.StockDao;
import roart.util.StockUtil;

public class StockItem {

	private String dbid;
	private String marketid;
	private String id;
	private String name;
	private Date date;
	private Double indexvalue;
	private Double price;
	private String currency;
	private Double[] period = new Double[6];

	public StockItem(String dbid, String marketid, String id, String name, Date date, Double indexvalue, Double price, String currency, Double period1, Double period2, Double period3, Double period4, Double period5, Double period6) throws Exception {
		this.dbid = dbid;
		this.marketid = marketid;
		this.id = id;
		this.name = name;
		this.date = date;
		this.indexvalue = indexvalue;
		this.currency = currency;
		this.price = price;
		this.period[0] = period1;
		this.period[1] = period2;
		this.period[2] = period3;
		this.period[3] = period4;
		this.period[4] = period5;
		this.period[5] = period6;
	}

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

	public Double getIndexvalue() {
		return indexvalue;
	}

	public void setIndexvalue(Double indexvalue) {
		this.indexvalue = indexvalue;
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

	public Double getPeriod(int i) {
		return period[i];
	}

	public static List<StockItem> getAll(String market) throws Exception {
		return DbDao.instance().getAll(market);
	}

	public static List<String> getMarkets() throws Exception {
		return Stock.getMarkets();
	}

}

package roart.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

	public StockItem(Stock stock) throws Exception {
		this.dbid = stock.getDbid();
		this.marketid = stock.getMarketid();
		this.id = stock.getId();
		this.name = stock.getName();
		this.date = stock.getDate();
		this.indexvalue = stock.getIndexvalue();
		this.price = stock.getPrice();
		for (int i = 0; i < StockUtil.PERIODS; i++) {
			this.period[i] = StockDao.getPeriod(stock, i);
		}
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
		List<Stock> stocks = Stock.getAll(market);
		List<StockItem> stockitems = new ArrayList();
		for (Stock stock : stocks) {
			StockItem stockItem = new StockItem(stock);
			stockitems.add(stockItem);
		}
		return stockitems;
	}

	public static List<String> getMarkets() throws Exception {
		return Stock.getMarkets();
	}

}

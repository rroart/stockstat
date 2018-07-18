package roart.model;

import java.util.Date;
import java.util.List;

import roart.config.MyMyConfig;
import roart.db.DbDao;
import roart.util.StockUtil;

public class StockItem {

    private String dbid;
    private String marketid;
    private String id;
    private String name;
    private Date date;
    private Double indexvalue;
    private Double indexvaluelow;
    private Double indexvaluehigh;
    private Double price;
    private Double pricelow;
    private Double pricehigh;
    private Long volume;
    private String currency;
    private Double[] period = new Double[9];

    public StockItem(String dbid, String marketid, String id, String name, Date date, Double indexvalue, Double indexvaluelow, Double indexvaluehigh, Double price, Double pricelow, Double pricehigh, Long volume, String currency, Double period1, Double period2, Double period3, Double period4, Double period5, Double period6, Double period7, Double period8, Double period9) throws Exception {
        this.dbid = dbid;
        this.marketid = marketid;
        this.id = id;
        this.name = name;
        this.date = date;
        this.indexvalue = indexvalue;
        this.indexvaluelow = indexvaluelow;
        this.indexvaluehigh = indexvaluehigh;
        this.currency = currency;
        this.price = price;
        this.pricelow = pricelow;
        this.pricehigh = pricehigh;
        this.volume = volume;
        this.period[0] = period1;
        this.period[1] = period2;
        this.period[2] = period3;
        this.period[3] = period4;
        this.period[4] = period5;
        this.period[5] = period6;
        this.period[6] = period7;
        this.period[7] = period8;
        this.period[8] = period9;
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

    public Double[] getIndexvalues() {
        Double[] indexvalues = new Double[3];
        indexvalues[0] = indexvalue;
        indexvalues[1] = indexvaluelow;
        indexvalues[2] = indexvaluehigh;
        return indexvalues;
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

    public Double[] getPrices() {
        Double[] prices = new Double[3];
        prices[0] = price;
        prices[1] = pricelow;
        prices[2] = pricehigh;
        return prices;
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

    public Double getPeriod(int i) {
        return period[i];
    }

    public static List<StockItem> getAll(String market, MyMyConfig conf) throws Exception {
        return StockUtil.filterWeekend(conf, DbDao.instance(conf).getAll(market));
    }

    public static List<String> getMarkets() throws Exception {
        return Stock.getMarkets();
    }

}

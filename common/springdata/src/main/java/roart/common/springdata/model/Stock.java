package roart.common.springdata.model;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import roart.common.model.OHLC;

@Table
public class Stock {

    @Id
    private String dbid;
    private String marketid;
    private String id;
    private String isin;
    private String name;
    private Date date;
    private Double indexvalue;
    private Double indexvaluelow;
    private Double indexvaluehigh;
    private Double indexvalueopen;
    private Double price;
    private Double pricelow;
    private Double pricehigh;
    private Double priceopen;
    private Long volume;
    private String currency;
    private Double period1;
    private Double period2;
    private Double period3;
    private Double period4;
    private Double period5;
    private Double period6;
    private Double period7;
    private Double period8;
    private Double period9;

    public Stock(String dbid, String marketid, String id, String isin, String name, Date date, Double indexvalue, Double indexvaluelow, Double indexvaluehigh, Double indexvalueopen, Double price, Double pricelow, Double pricehigh, Double priceopen, Long volume, String currency, Double period1, Double period2, Double period3, Double period4, Double period5, Double period6, Double period7, Double period8, Double period9) throws Exception {
        this.dbid = dbid;
        this.marketid = marketid;
        this.id = id;
        this.isin = isin;
        this.name = name;
        this.date = date;
        this.indexvalue = indexvalue;
        this.indexvaluelow = indexvaluelow;
        this.indexvaluehigh = indexvaluehigh;
        this.indexvalueopen = indexvalueopen;
        this.currency = currency;
        this.price = price;
        this.pricelow = pricelow;
        this.pricehigh = pricehigh;
        this.priceopen = priceopen;
        this.volume = volume;
        this.period1 = period1;
        this.period2 = period2;
        this.period3 = period3;
        this.period4 = period4;
        this.period5 = period5;
        this.period6 = period6;
        this.period7 = period7;
        this.period8 = period8;
        this.period9 = period9;
    }

    public Stock() {
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

    public Double[] getIndexvalues() {
        return new Double[] { indexvalue, indexvaluelow, indexvaluehigh, indexvalueopen };
    }

    public OHLC getIndexvalueOHLC() {
        return new OHLC(indexvalueopen, indexvaluehigh, indexvaluelow, indexvalue);
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

    public Double[] getPrices() {
        return new Double[] { price, pricelow, pricehigh, priceopen };
    }

    public OHLC getPriceOHLC() {
        return new OHLC(priceopen, pricehigh, pricelow, price);
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

}

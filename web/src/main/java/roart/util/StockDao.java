package roart.util;

import java.util.Date;
import java.util.List;

import javax.persistence.Transient;

import roart.model.HibernateUtil;
import roart.model.Stock;

public class StockDao {
    @Transient
    public static Double getPeriod(Stock stock, int i) throws Exception {
        if (i == 1) {
            return stock.getPeriod1();
        }
        if (i == 2) {
            return stock.getPeriod2();
        }
        if (i == 3) {
            return stock.getPeriod3();
        }
        if (i == 4) {
            return stock.getPeriod4();
        }
        if (i == 5) {
            return stock.getPeriod5();
        }
        if (i == 6) {
            return stock.getPeriod6();
        }
        throw new Exception("Out of range " + i);
    }

    @Transient
    public static Double getValue(Stock stock, int i) throws Exception {
        if (i > 0) {
            return getPeriod(stock, i);
        } else {
            return getSpecial(stock, i);
        }
    }
    
    @Transient
    public static Double getSpecial(Stock stock, int i) throws Exception {
        if (i == Constants.INDEXVALUE) {
            return stock.getIndexvalue();
        }
        if (i == Constants.PRICE) {
            return stock.getPrice();
        }
        throw new Exception("Out of range " + i);
    }

    @Transient
    public static List<Date> getDates() throws Exception {
        return (List<Date>) HibernateUtil.convert(HibernateUtil.currentSession().createQuery("select distinct(date) from Stock").list(), Date.class);
    }

}

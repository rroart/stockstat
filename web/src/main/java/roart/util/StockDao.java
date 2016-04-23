package roart.util;

import javax.persistence.Transient;

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
        throw new Exception("Out of range " + i);
    }

}

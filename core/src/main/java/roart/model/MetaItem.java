package roart.model;

import roart.config.MyMyConfig;
import roart.db.DbDao;
import roart.util.StockUtil;

public class MetaItem {

    private String marketid;
    private String[] period = new String[StockUtil.PERIODS];

    public MetaItem(String marketid, String period1, String period2, String period3, String period4, String period5, String period6, String period7, String period8, String period9) {
        this.marketid = marketid;
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

    public static MetaItem getById(String market, MyMyConfig conf) throws Exception {
        System.out.println("mymarket " + market);
        return DbDao.instance(conf).getMarket(market);
    }
    public String getperiod(int i) {
        return period[i];
    }

}

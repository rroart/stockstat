package roart.model;

import roart.common.constants.Constants;

public class MetaItem {

    private String marketid;
    private String[] period = new String[Constants.PERIODS];

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

    public String getperiod(int i) {
        return period[i];
    }

}

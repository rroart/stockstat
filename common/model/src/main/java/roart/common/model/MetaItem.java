package roart.common.model;

import roart.common.constants.Constants;

public class MetaItem {

    private String marketid;
    private String[] period = new String[Constants.PERIODS];
    private String priority;
    private String reset;
    private boolean lhc;

    public MetaItem(String marketid, String period1, String period2, String period3, String period4, String period5, String period6, String period7, String period8, String period9, String priority, String reset, boolean lhc) {
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
        this.priority = priority;
        this.reset = reset;
        this.lhc = lhc;
    }

    public String getMarketid() {
        return marketid;
    }

    public void setMarketid(String marketid) {
        this.marketid = marketid;
    }

    public String getperiod(int i) {
        return period[i];
    }

    public String[] getPeriod() {
        return period;
    }

    public void setPeriod(String[] period) {
        this.period = period;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getReset() {
        return reset;
    }

    public void setReset(String reset) {
        this.reset = reset;
    }

    public boolean isLhc() {
        return lhc;
    }

    public void setLhc(boolean lhc) {
        this.lhc = lhc;
    }

}

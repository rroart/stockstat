package roart.common.pipeline.data;

import roart.common.constants.Constants;

public class SerialMeta extends SerialObject {

    private String marketid;
    private String[] period = new String[Constants.PERIODS];
    private String priority;
    private String reset;
    private Boolean lhc;

    public SerialMeta() {
        super();
    }

    public SerialMeta(String marketid, String[] period, String priority, String reset, Boolean lhc) {
        super();
        this.marketid = marketid;
        this.period = period;
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

    public Boolean getLhc() {
        return lhc;
    }

    public void setLhc(Boolean lhc) {
        this.lhc = lhc;
    }
    
}

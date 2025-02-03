package roart.common.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class ActionComponentItem {

    private Long dbid;
    private String action;
    private String component;
    private String subcomponent;
    private String market;
    private double time;
    private boolean haverun;
    private int priority;
    //List<TimingItem> timings;
    private Boolean buy;
    private String parameters;
    private LocalDate record;
    private BlockingQueue result;
    private LocalDate date;
    
    public ActionComponentItem() {
        super();
    }

    public ActionComponentItem(String market, String action, String component, String subcomponent, int priority,
            String parameters) {
        super();
        this.market = market;
        this.action = action;
        this.component = component;
        this.subcomponent = subcomponent;
        this.priority = priority;
        this.parameters = parameters;
    }

    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getSubcomponent() {
        return subcomponent;
    }

    public void setSubcomponent(String subcomponent) {
        this.subcomponent = subcomponent;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public double getTime() {
        return time;
    }

    public void setTime(double time) {
        this.time = time;
    }

    public boolean isHaverun() {
        return haverun;
    }

    public void setHaverun(boolean haverun) {
        this.haverun = haverun;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public BlockingQueue getResult() {
        return result;
    }

    public void setResult(BlockingQueue result) {
        this.result = result;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public String toString() {
        String paramString = parameters;
        return record != null ? record.toString() : "" + " " + " " + market + " " + action + " " + component + " " + subcomponent + " " + paramString + " " + buy + " " + priority + " " + time + " " + haverun;
    }

    public String toStringId() {
        String paramString = parameters;
        return market + " " + action + " " + component + " " + subcomponent + " " + paramString + " " + buy + " " + priority;
    }

}

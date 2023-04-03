package roart.common.springdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;

@Table("actioncomponent")
public class ActionComponent {

    @Id
    private Long dbid;
    private String action;
    private String component;
    private String subcomponent;
    private String market;
    private int priority;
    private Boolean buy;
    private String parameters;
    private LocalDate record;
    
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

    @Override
    public String toString() {
        String paramString = JsonUtil.convert(parameters);
        return record != null ? record.toString() : "" + " " + " " + market + " " + action + " " + component + " " + subcomponent + " " + paramString + " " + buy + " " + priority;
    }

    public String toStringId() {
        String paramString = JsonUtil.convert(parameters);
        return market + " " + action + " " + component + " " + subcomponent + " " + paramString + " " + buy + " " + priority;
    }

}

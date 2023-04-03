package roart.common.springdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;

@Table
public class Config {
    @Id
    private Long dbid;

    private Date record;
    
    private Date date;
    
    private String market;

    private String component;

    private String subcomponent;

    private String parameters;
    
    private String action;
    
    private String id;
    
    private byte[] value;
    
    @Deprecated
    private Double score;

    private Boolean buy;
    
    public Date getRecord() {
        return record;
    }

    public void setRecord(Date record) {
        this.record = record;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getValue() {
        return value;
    }

    public void setValue(byte[] value) {
        //if (value != null && value.length() > 510) {
        //    value = value.substring(0, 510);
        //}
        this.value = value;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Boolean getBuy() {
        return buy;
    }

    public void setBuy(Boolean buy) {
        this.buy = buy;
    }

    @Override
    public String toString() {
        return market + " " + component + " " + subcomponent + " " + parameters + " " + action + " " + record + " " + date + " " + id + " " + value + " " + score + "\n"; 
    }

}

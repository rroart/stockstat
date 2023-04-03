package roart.common.springdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import roart.common.util.TimeUtil;

@Table
public class Timing {
    @Id
    private Long dbid;

    private LocalDate record;
    
    private LocalDate date;
    
    private String market;

    private String mlmarket;
    
    private String action;
    
    private boolean evolve;
    
    private String component;
    
    private String subcomponent;
    
    private String parameters;
    
    private Double time;

    private Double score;
    
    private Boolean buy;
    
    private String description;
    
    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getMlmarket() {
        return mlmarket;
    }

    public void setMlmarket(String mlmarket) {
        this.mlmarket = mlmarket;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public boolean isEvolve() {
        return evolve;
    }

    public void setEvolve(boolean evolve) {
        this.evolve = evolve;
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

    public Double getTime() {
        return time;
    }

    public void setTime(Double time) {
        this.time = time;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setTime(long time0) {
        this.time = ((double) (System.currentTimeMillis()) - time0) / 1000;
    }

    @Override
    public String toString() {
        return market + " " + mlmarket + " " + component + " " + subcomponent + " " + parameters + " " + action + " " + record + " " + date + " " + evolve + " " + time + " " + score + " " + description + "\n"; 
    }

}

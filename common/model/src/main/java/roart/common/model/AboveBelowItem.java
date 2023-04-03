package roart.common.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AboveBelowItem {
    private LocalDate record;
    
    private Date date;
    
    private String market;

    private String components;
    
    private String subcomponents;

    private Double score;

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
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

    public String getComponents() {
        return components;
    }

    public void setComponents(String components) {
        this.components = components;
    }

    public String getSubcomponents() {
        return subcomponents;
    }

    public void setSubcomponents(String subcomponents) {
        this.subcomponents = subcomponents;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
    

    @Override
    public String toString() {
        return market + " " + score + " " + date + " " + components + " " + subcomponents + "\n"; 
    }
    
}

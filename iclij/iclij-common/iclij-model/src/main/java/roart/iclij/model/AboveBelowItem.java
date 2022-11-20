package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import roart.db.model.AboveBelow;

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
    
    public void save() throws Exception {
        AboveBelow data = new AboveBelow();
        data.setComponents(getComponents());
        data.setDate(getDate());
        data.setMarket(getMarket());
        data.setRecord(getRecord());
        data.setScore(getScore());
        data.setSubcomponents(getSubcomponents());
        data.save();
     }
    
    public static List<AboveBelowItem> getAll(String market, Date startdate, Date enddate) throws Exception {
        List<AboveBelow> sims = AboveBelow.getAll(market, startdate, enddate);
        List<AboveBelowItem> simItems = new ArrayList<>();
        for (AboveBelow sim : sims) {
            AboveBelowItem simItem = getAboveBelowItem(sim);
            simItems.add(simItem);
        }
        return simItems;        
    }
    
    private static AboveBelowItem getAboveBelowItem(AboveBelow data) {
        AboveBelowItem item = new AboveBelowItem();
        item.setComponents(data.getComponents());
        item.setDate(data.getDate());
        item.setMarket(data.getMarket());
        item.setRecord(data.getRecord());
        item.setScore(data.getScore());
        item.setSubcomponents(data.getSubcomponents());
        return item;
    }

    public void delete(String market, Date startDate, Date endDate) throws Exception {
        AboveBelow.delete(market, startDate, endDate);
    }

}

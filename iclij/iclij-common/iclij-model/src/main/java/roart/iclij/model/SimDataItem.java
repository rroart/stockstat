package roart.iclij.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import roart.db.model.SimData;

public class SimDataItem {

    private Long dbid;
    
    private LocalDate record;
    
    private String market;

    private LocalDate startdate;
    
    private LocalDate enddate;
    
    private Double score;
    
    private String filter;
    
    private String config;
    
    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public LocalDate getRecord() {
        return record;
    }

    public void setRecord(LocalDate record) {
        this.record = record;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }
 
    public LocalDate getStartdate() {
        return startdate;
    }

    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
    }

    public LocalDate getEnddate() {
        return enddate;
    }

    public void setEnddate(LocalDate enddate) {
        this.enddate = enddate;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return market + " " + score + " " + startdate + " " + enddate + "\n"; 
    }
    
    public void save() throws Exception {
        SimData data = new SimData();
        data.setConfig(getConfig().getBytes());
        data.setEnddate(getEnddate());
        if (getFilter() != null) {
            data.setFilter(getFilter().getBytes());
        }
        data.setMarket(getMarket());
        data.setRecord(getRecord());
        data.setScore(getScore());
        data.setStartdate(getStartdate());
        data.save();
     }
    
    public static List<SimDataItem> getAll(String market, LocalDate startdate, LocalDate enddate) throws Exception {
        List<SimData> sims = SimData.getAll(market, startdate, enddate);
        List<SimDataItem> simDataItems = new ArrayList<>();
        for (SimData sim : sims) {
            SimDataItem simDataItem = getSimDataItem(sim);
            simDataItems.add(simDataItem);
        }
        return simDataItems;        
    }
    
    private static SimDataItem getSimDataItem(SimData data) {
        SimDataItem item = new SimDataItem();
        item.setConfig(new String(data.getConfig()));
        item.setDbid(data.getDbid());
        item.setEnddate(data.getEnddate());
        if (data.getFilter() != null) {
            item.setFilter(new String(data.getFilter()));
        }
        item.setMarket(data.getMarket());
        item.setRecord(data.getRecord());
        item.setScore(data.getScore());
        item.setStartdate(data.getStartdate());
        return item;
    }

}

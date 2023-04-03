package roart.common.springdata.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("simdata")
public class SimData {

    @Id
    private Long dbid;
    
    private LocalDate record;
    
    private String market;

    private LocalDate startdate;
    
    private LocalDate enddate;
    
    private Double score;
    
    private byte[] filter;
    
    private byte[] config;
    
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

    public byte[] getFilter() {
        return filter;
    }

    public void setFilter(byte[] filter) {
        this.filter = filter;
    }

    public byte[] getConfig() {
        return config;
    }

    public void setConfig(byte[] config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return market + " " + score + " " + startdate + " " + enddate + "\n"; 
    }

}

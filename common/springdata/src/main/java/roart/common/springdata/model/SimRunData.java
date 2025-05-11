package roart.common.springdata.model;

import java.time.LocalDate;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("simrun")
public class SimRunData {

    @Id
    private Long dbid;

    private Long simdatadbid;

    private LocalDate recorddate;

    private String market;
        
    private LocalDate startdate;
    
    private LocalDate enddate;
    
    private Double score;

    private Double correlation;
    
    public Long getDbid() {
        return dbid;
    }

    public void setDbid(Long dbid) {
        this.dbid = dbid;
    }

    public Long getSimdatadbid() {
        return simdatadbid;
    }

    public void setSimdatadbid(Long simdatadbid) {
        this.simdatadbid = simdatadbid;
    }

    public LocalDate getRecorddate() {
        return recorddate;
    }

    public void setRecorddate(LocalDate recorddate) {
        this.recorddate = recorddate;
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

    public Double getCorrelation() {
        return correlation;
    }

    public void setCorrelation(Double correlation) {
        this.correlation = correlation;
    }

}

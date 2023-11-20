package roart.iclij.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import roart.common.constants.Constants;
import roart.common.util.JsonUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AutoSimulateInvestConfig {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String startdate;
    
    private String enddate;
    
    private Integer futurecount;
    
    private Integer futuretime;
    
    private Integer interval;

    private Integer period;

    private Integer lastcount;
    
    private Double dellimit;
    
    private Double scorelimit;
    
    private Double autoscorelimit;
    
    private Boolean keepAdviser;
    
    private Double keepAdviserLimit;
    
    private Boolean intervalwhole;
    
    private Map<String, Double> volumelimits;
    
    private List<SimulateFilter> filters;
    
    private Boolean improveFilters;
    
    private Integer ga;
    
    private Boolean vote;
    
    public String getStartdate() {
        return startdate;
    }

    public void setStartdate(String startdate) {
        this.startdate = startdate;
    }

    public String getEnddate() {
        return enddate;
    }

    public void setEnddate(String enddate) {
        this.enddate = enddate;
    }

    public Integer getFuturecount() {
        return futurecount;
    }

    public void setFuturecount(Integer futurecount) {
        this.futurecount = futurecount;
    }

    public Integer getFuturetime() {
        return futuretime;
    }

    public void setFuturetime(Integer futuretime) {
        this.futuretime = futuretime;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public Integer getPeriod() {
        return period;
    }

    public void setPeriod(Integer period) {
        this.period = period;
    }

    public Integer getLastcount() {
        return lastcount;
    }

    public void setLastcount(Integer lastcount) {
        this.lastcount = lastcount;
    }

    public Double getDellimit() {
        return dellimit;
    }

    public void setDellimit(Double dellimit) {
        this.dellimit = dellimit;
    }

    public Double getScorelimit() {
        return scorelimit;
    }

    public void setScorelimit(Double scorelimit) {
        this.scorelimit = scorelimit;
    }

    public Double getAutoscorelimit() {
        return autoscorelimit;
    }

    public void setAutoscorelimit(Double autoscorelimit) {
        this.autoscorelimit = autoscorelimit;
    }

    public Boolean getKeepAdviser() {
        return keepAdviser;
    }

    public void setKeepAdviser(Boolean keepAdviser) {
        this.keepAdviser = keepAdviser;
    }

    public Double getKeepAdviserLimit() {
        return keepAdviserLimit;
    }

    public void setKeepAdviserLimit(Double keepAdviserLimit) {
        this.keepAdviserLimit = keepAdviserLimit;
    }

    public Boolean getIntervalwhole() {
        return intervalwhole;
    }

    public void setIntervalwhole(Boolean intervalwhole) {
        this.intervalwhole = intervalwhole;
    }

    public Map<String, Double> getVolumelimits() {
        return volumelimits;
    }

    public void setVolumelimits(Map<String, Double> volumelimits) {
        this.volumelimits = volumelimits;
    }

    public List<SimulateFilter> getFilters() {
        return filters;
    }

    public void setFilters(List<SimulateFilter> filters) {
        this.filters = filters;
    }

    public Boolean getImproveFilters() {
        return improveFilters;
    }

    public void setImproveFilters(Boolean improveFilters) {
        this.improveFilters = improveFilters;
    }

    public Integer getGa() {
        return ga;
    }

    public void setGa(Integer ga) {
        this.ga = ga;
    }

    public Boolean getVote() {
        return vote;
    }

    public void setVote(Boolean vote) {
        this.vote = vote;
    }

    public AutoSimulateInvestConfig() {
        super();
    }

    public Map<String, Object> asMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTINTERVALWHOLE, intervalwhole);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTINTERVAL, interval);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTPERIOD, period);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTDELLIMIT, dellimit);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTSCORELIMIT, scorelimit);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTAUTOSCORELIMIT, autoscorelimit);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISER, keepAdviser);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTKEEPADVISERLIMIT, keepAdviserLimit);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTLASTCOUNT, lastcount);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE, startdate);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE, enddate);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTVOTE, vote);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTFUTURECOUNT, futurecount);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTFUTURETIME, futuretime);
        String volumelimitString = null;
        if (volumelimits != null) {
            volumelimitString = convert(volumelimits);
        }
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTVOLUMELIMITS, volumelimitString);
        String simfilterString = null;
        if (filters != null) {
            simfilterString = convert(filters);
        }
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS, simfilterString);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTIMPROVEFILTERS, improveFilters);
        return map;
    }
    
    public Map<String, Object> asValuedMap() {
        Map<String, Object> map = new HashMap<>();
        for (Entry<String, Object> entry : asMap().entrySet()) {
            if (entry.getValue() != null) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        return map;
    }
    
    private String convert(Object object) {
        if (object != null) {
            try {
                return JsonUtil.convert(object);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }
}

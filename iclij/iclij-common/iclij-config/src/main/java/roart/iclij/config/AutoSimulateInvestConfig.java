package roart.iclij.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.constants.Constants;

public class AutoSimulateInvestConfig {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String startdate;
    
    private String enddate;
    
    private Integer interval;

    private Integer period;

    private Integer lastcount;
    
    private Double dellimit;
    
    private Double scorelimit;
    
    private Boolean intervalwhole;
    
    private Map<String, Double> volumelimits;
    
    private List<SimulateFilter> filters;
    
    private Integer ga;
    
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

    public Integer getGa() {
        return ga;
    }

    public void setGa(Integer ga) {
        this.ga = ga;
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
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTLASTCOUNT, lastcount);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE, startdate);
        map.put(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE, enddate);
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
        ObjectMapper mapper = new ObjectMapper();
        if (object != null) {
            try {
                return mapper.writeValueAsString(object);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        return null;
    }
}

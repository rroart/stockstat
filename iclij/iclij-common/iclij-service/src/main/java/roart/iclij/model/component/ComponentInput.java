package roart.iclij.model.component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;

public class ComponentInput {

    private IclijConfig config;
    
    private LocalDate startdate;
    
    private String market;

    private String mlmarket;

    private LocalDate enddate;
    
    private Integer loopoffset;
    
    private boolean doSave;
    
    private boolean doPrint;

    private List<String> keys;
    
    private Map<String, Object> valuemap;
    
    public ComponentInput(String market, LocalDate enddate, Integer offset, boolean doSave, boolean doPrint) {
        super();
        this.market = market;
        this.enddate = enddate;
        this.loopoffset = offset != null ? offset : 0;
        this.doSave = doSave;
        this.doPrint = doPrint;
        if (doSave == false) {
            int jj = 0;
        }
    }

    public ComponentInput(IclijConfig config, LocalDate startdate, String market, LocalDate enddate, Integer loopoffset,
            boolean doSave, boolean doPrint, List<String> keys, Map<String, Object> valuemap) {
        super();
        this.config = config;
        this.startdate = startdate;
        this.market = market;
        if (enddate == null) {
            LocalDate date = config.getConfigData().getDate();
            if (date == null) {
                date = LocalDate.now();
            }
            enddate = date;
        }
        this.enddate = enddate;
        this.loopoffset = loopoffset; // != null ? loopoffset : 0;
        this.doSave = doSave;
        if (doSave == false) {
            int jj = 0;
        }
        this.doPrint = doPrint;
        this.keys = keys;
        this.valuemap = valuemap;
    }

    public IclijConfig getConfig() {
        return config;
    }

    public void setConfig(IclijConfig config) {
        this.config = config;
    }

    public LocalDate getStartdate() {
        return startdate;
    }

    public void setStartdate(LocalDate startdate) {
        this.startdate = startdate;
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

    public LocalDate getEnddate() {
        return enddate;
    }

    public void setEnddate(LocalDate enddate) {
        this.enddate = enddate;
    }

    public Integer getLoopoffset() {
        return loopoffset;
    }

    public void setLoopoffset(Integer loopoffset) {
        this.loopoffset = loopoffset;
    }

    public boolean isDoSave() {
        return doSave;
    }

    public void setDoSave(boolean doSave) {
        this.doSave = doSave;
    }

    public boolean isDoPrint() {
        return doPrint;
    }

    public void setDoPrint(boolean doPrint) {
        this.doPrint = doPrint;
    }

    public List<String> getKeys() {
        return keys;
    }

    public void setKeys(List<String> keys) {
        this.keys = keys;
    }

    public Map<String, Object> getValuemap() {
        return valuemap;
    }

    public void setValuemap(Map<String, Object> valuemap) {
        this.valuemap = valuemap;
    }

}

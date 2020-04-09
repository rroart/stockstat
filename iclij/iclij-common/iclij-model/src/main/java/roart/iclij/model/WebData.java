package roart.iclij.model;

import java.util.List;
import java.util.Map;

import roart.service.model.ProfitData;

public class WebData {
    private ProfitData profitData;
   
    private List<MemoryItem> memoryItems;
    
    private List<IncDecItem> incs;
    
    private List<IncDecItem> decs;
    
    private Map<String, Object> updateMap;
    
    private Map<String, Object> updateMap2;
    
    private Map<String, Object> timingMap;
    
    private Map<String, Object> timingMap2;
    
    public WebData() {
        super();
    }

    public ProfitData getProfitData() {
        return profitData;
    }

    public void setProfitData(ProfitData profitData) {
        this.profitData = profitData;
    }

    public List<MemoryItem> getMemoryItems() {
        return memoryItems;
    }

    public void setMemoryItems(List<MemoryItem> memoryItems) {
        this.memoryItems = memoryItems;
    }

    public List<IncDecItem> getIncs() {
        return incs;
    }

    public void setIncs(List<IncDecItem> incs) {
        this.incs = incs;
    }

    public List<IncDecItem> getDecs() {
        return decs;
    }

    public void setDecs(List<IncDecItem> decs) {
        this.decs = decs;
    }

    public Map<String, Object> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<String, Object> updateMap) {
        this.updateMap = updateMap;
    }

    public Map<String, Object> getUpdateMap2() {
        return updateMap2;
    }

    public void setUpdateMap2(Map<String, Object> updateMap2) {
        this.updateMap2 = updateMap2;
    }

    public Map<String, Object> getTimingMap() {
        return timingMap;
    }

    public void setTimingMap(Map<String, Object> timingMap) {
        this.timingMap = timingMap;
    }

    public Map<String, Object> getTimingMap2() {
        return timingMap2;
    }

    public void setTimingMap2(Map<String, Object> timingMap2) {
        this.timingMap2 = timingMap2;
    }
        
}

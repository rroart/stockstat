package roart.iclij.model;

import java.util.List;
import java.util.Map;

import roart.common.model.IncDecItem;
import roart.common.model.MemoryItem;
import roart.common.model.TimingItem;

public class WebDataJson {
    private List<MemoryItem> memoryItems;
    
    private List<IncDecItem> incs;
    
    private List<IncDecItem> decs;
    
    private Map<String, List<TimingItem>> timingMap;
    
    private Map<String, Object> updateMap;
    
    public WebDataJson() {
        super();
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

    public Map<String, List<TimingItem>> getTimingMap() {
        return timingMap;
    }

    public void setTimingMap(Map<String, List<TimingItem>> timingMap) {
        this.timingMap = timingMap;
    }

    public Map<String, Object> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<String, Object> updateMap) {
        this.updateMap = updateMap;
    }

}

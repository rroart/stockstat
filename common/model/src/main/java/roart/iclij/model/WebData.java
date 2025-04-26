package roart.iclij.model;

import java.util.List;
import java.util.Map;

import roart.common.model.IncDecDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.TimingDTO;
import roart.service.model.ProfitData;

public class WebData {
    private ProfitData profitData;
   
    private List<MemoryDTO> memoryDTOS;
    
    private List<IncDecDTO> incs;
    
    private List<IncDecDTO> decs;
    
    private Map<String, Object> updateMap;
    
    private Map<String, Object> updateMap2;
    
    private Map<String, List<TimingDTO>> timingMap;
    
    private Map<String, List<TimingDTO>> timingMap2;
    
    public WebData() {
        super();
    }

    public ProfitData getProfitData() {
        return profitData;
    }

    public void setProfitData(ProfitData profitData) {
        this.profitData = profitData;
    }

    public List<MemoryDTO> getMemoryDTOs() {
        return memoryDTOS;
    }

    public void setMemoryDTOs(List<MemoryDTO> memoryDTOS) {
        this.memoryDTOS = memoryDTOS;
    }

    public List<IncDecDTO> getIncs() {
        return incs;
    }

    public void setIncs(List<IncDecDTO> incs) {
        this.incs = incs;
    }

    public List<IncDecDTO> getDecs() {
        return decs;
    }

    public void setDecs(List<IncDecDTO> decs) {
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

    public Map<String, List<TimingDTO>> getTimingMap() {
        return timingMap;
    }

    public void setTimingMap(Map<String, List<TimingDTO>> timingMap) {
        this.timingMap = timingMap;
    }

    public Map<String, List<TimingDTO>> getTimingMap2() {
        return timingMap2;
    }

    public void setTimingMap2(Map<String, List<TimingDTO>> timingMap2) {
        this.timingMap2 = timingMap2;
    }
        
}

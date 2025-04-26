package roart.iclij.model;

import java.util.List;
import java.util.Map;

import roart.common.model.IncDecDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.TimingDTO;

public class WebDataJson {
    private List<MemoryDTO> memoryDTOS;
    
    private List<IncDecDTO> incs;
    
    private List<IncDecDTO> decs;
    
    private Map<String, List<TimingDTO>> timingMap;
    
    private Map<String, Object> updateMap;
    
    public WebDataJson() {
        super();
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

    public Map<String, List<TimingDTO>> getTimingMap() {
        return timingMap;
    }

    public void setTimingMap(Map<String, List<TimingDTO>> timingMap) {
        this.timingMap = timingMap;
    }

    public Map<String, Object> getUpdateMap() {
        return updateMap;
    }

    public void setUpdateMap(Map<String, Object> updateMap) {
        this.updateMap = updateMap;
    }

}

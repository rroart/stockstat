package roart.service.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;

import roart.common.model.IncDecItem;
import roart.common.model.MemoryItem;

public class ProfitInputData {
        
    private Map<String, String> nameMap;

    public ProfitInputData() {
        super();
    }
    
    public Map<String, String> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, String> nameMap) {
        this.nameMap = nameMap;
    }

}

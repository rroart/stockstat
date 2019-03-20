package roart.service.model;

import java.util.List;
import java.util.Map;

import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;

public class ProfitInputData {
    Map<String, Map<String, Object>> resultMaps;
    
    List<Integer> positions;
    
    private Map<Object[], Double> confMap;
    
    private Map<Object[], List<MemoryItem>> listMap;
    
    Map<String, String> nameMap;

    public Map<String, Map<String, Object>> getResultMaps() {
        return resultMaps;
    }

    public void setResultMaps(Map<String, Map<String, Object>> resultMaps) {
        this.resultMaps = resultMaps;
    }

    public Map<Object[], Double> getConfMap() {
        return confMap;
    }

    public void setConfMap(Map<Object[], Double> confMap) {
        this.confMap = confMap;
    }

    public Map<Object[], List<MemoryItem>> getListMap() {
        return listMap;
    }

    public void setListMap(Map<Object[], List<MemoryItem>> listMap) {
        this.listMap = listMap;
    }

    public Map<String, String> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, String> nameMap) {
        this.nameMap = nameMap;
    }

}

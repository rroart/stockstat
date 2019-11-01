package roart.service.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;

public class ProfitInputData {
    Map<String, Map<String, Object>> resultMaps;
    
    List<Integer> positions;
    
    private Map<Pair<String, Integer>, Double> confMap;
    
    private Map<Pair<String, Integer>, List<MemoryItem>> listMap;
    
    Map<String, String> nameMap;

    public Map<String, Map<String, Object>> getResultMaps() {
        return resultMaps;
    }

    public void setResultMaps(Map<String, Map<String, Object>> resultMaps) {
        this.resultMaps = resultMaps;
    }

    public Map<Pair<String, Integer>, Double> getConfMap() {
        return confMap;
    }

    public void setConfMap(Map<Pair<String, Integer>, Double> confMap) {
        this.confMap = confMap;
    }

    public Map<Pair<String, Integer>, List<MemoryItem>> getListMap() {
        return listMap;
    }

    public void setListMap(Map<Pair<String, Integer>, List<MemoryItem>> listMap) {
        this.listMap = listMap;
    }

    public Map<String, String> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, String> nameMap) {
        this.nameMap = nameMap;
    }

}

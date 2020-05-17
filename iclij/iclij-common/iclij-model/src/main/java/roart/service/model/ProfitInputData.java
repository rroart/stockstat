package roart.service.model;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;

import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;

public class ProfitInputData {
    private  Map<String, Map<String, Object>> resultMaps;
    
    private List<Integer> positions;
    
    private Map<Triple<String, String, String>, Double> confMap;
    
    private Map<Triple<String, String, String>, List<MemoryItem>> listMap;
    
    private Map<Triple<String, String, String>, Double> aboveConfMap;
    
    private Map<Triple<String, String, String>, List<MemoryItem>> aboveListMap;
    
    private Map<Triple<String, String, String>, Double> belowConfMap;
    
    private Map<Triple<String, String, String>, List<MemoryItem>> belowListMap;
    
    private Map<String, String> nameMap;

    public ProfitInputData() {
        super();
    }
    
    public Map<String, Map<String, Object>> getResultMaps() {
        return resultMaps;
    }

    public void setResultMaps(Map<String, Map<String, Object>> resultMaps) {
        this.resultMaps = resultMaps;
    }

    public Map<Triple<String, String, String>, Double> getConfMap() {
        return confMap;
    }

    public void setConfMap(Map<Triple<String, String, String>, Double> confMap) {
        this.confMap = confMap;
    }

    public Map<Triple<String, String, String>, List<MemoryItem>> getListMap() {
        return listMap;
    }

    public void setListMap(Map<Triple<String, String, String>, List<MemoryItem>> listMap) {
        this.listMap = listMap;
    }

    public Map<Triple<String, String, String>, Double> getAboveConfMap() {
        return aboveConfMap;
    }

    public void setAboveConfMap(Map<Triple<String, String, String>, Double> aboveConfMap) {
        this.aboveConfMap = aboveConfMap;
    }

    public Map<Triple<String, String, String>, List<MemoryItem>> getAboveListMap() {
        return aboveListMap;
    }

    public void setAboveListMap(Map<Triple<String, String, String>, List<MemoryItem>> aboveListMap) {
        this.aboveListMap = aboveListMap;
    }

    public Map<Triple<String, String, String>, Double> getBelowConfMap() {
        return belowConfMap;
    }

    public void setBelowConfMap(Map<Triple<String, String, String>, Double> belowConfMap) {
        this.belowConfMap = belowConfMap;
    }

    public Map<Triple<String, String, String>, List<MemoryItem>> getBelowListMap() {
        return belowListMap;
    }

    public void setBelowListMap(Map<Triple<String, String, String>, List<MemoryItem>> belowListMap) {
        this.belowListMap = belowListMap;
    }

    public Map<String, String> getNameMap() {
        return nameMap;
    }

    public void setNameMap(Map<String, String> nameMap) {
        this.nameMap = nameMap;
    }

}

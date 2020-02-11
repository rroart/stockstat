package roart.iclij.model;

import java.util.List;
import java.util.Map;

import roart.service.model.ProfitData;

public class WebData {
    public ProfitData profitData;
    public List<MemoryItem> memoryItems;
    public List<IncDecItem> incs;
    public List<IncDecItem> decs;
    public Map<String, Object> updateMap;
    public Map<String, Object> updateMap2;
    public Map<String, Object> timingMap;
    public Map<String, Object> timingMap2;
}
package roart.service;

import java.util.List;
import java.util.Map;

import roart.model.IncDecItem;

public class IclijServiceResult {
    public List<String> markets;
    public Map<String, String> stocks;
    public Map<String, Map<String, Object>> maps;
    public String error;
    public List<IclijServiceList> lists;
}

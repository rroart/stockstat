package roart.common.pipeline.data;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapOneDim {

    private Map<String, OneDim> map;

    public MapOneDim() {
        super();
    }

    public MapOneDim(Map<String, OneDim> map) {
        super();
        this.map = map;
    }

    public Map<String, OneDim> getMap() {
        return map;
    }

    public void setMap(Map<String, OneDim> map) {
        this.map = map;
    }

    public OneDim get(String key) {
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }
        
}

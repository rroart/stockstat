package roart.common.pipeline.data;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;

public class PipelineData extends SerialObject {

    private String id;
    
    private String name;

    private Map<String, Object> map = new HashMap<>();

    private SerialMap smap = new SerialMap();

    public PipelineData() {
        super();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public void put(String key, Object object) {
        if (object instanceof SerialObject serialobject) {
            smap.put(key, serialobject);
            return;
        }
        if (object instanceof String serialobject) {
            smap.put(key, new SerialString(serialobject));
            return;
        }
        if (object instanceof Integer serialobject) {
            smap.put(key, new SerialInteger(serialobject));
            return;
        }
        map.put(key, object);
    }
    
    public Object get(String key) {
        if (smap.containsKey(key)) {
            return smap.get(key);
        }
        return map.get(key);
    }

    public Map<String, Object> getMap(String key) {
        return (Map<String, Object>) map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }
    
    
    public SerialMap getSmap() {
        return smap;
    }

    public void setSmap(SerialMap smap) {
        this.smap = smap;
    }

    @Deprecated
    public SerialMap smap() {
        return smap;
    }
}

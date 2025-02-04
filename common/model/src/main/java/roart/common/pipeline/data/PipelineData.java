package roart.common.pipeline.data;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;
import java.util.List;

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
        if (object == null) {
            return;
        }
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
        if (object instanceof Double serialobject) {
            smap.put(key, new SerialDouble(serialobject));
            return;
        }
        log.error("Should not use ordinary map");
        map.put(key, object);
    }
    
    public Object get(String key) {
        if (smap.containsKey(key)) {
            return smap.get(key);
        }
        return map.get(key);
    }

    public Map<String, Object> getMap(String key) {
        if (smap.containsKey(key)) {
            SerialMapPlain amap = (SerialMapPlain) smap.get(key);
            return amap.getMap();
        }
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

    public void putAll(Map<String, Object> amap) {
        for (Entry<String, Object> entry : amap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public List<SerialKeyValue> getListMap(String key) {
        if (smap.containsKey(key)) {
            SerialListMap amap = (SerialListMap) smap.get(key);
            return amap.getMap();
        }
        return null;
    }

    public void putAll(List<SerialKeyValue> listMap) {
        for (SerialKeyValue entry : listMap) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public String toString() {
        return "PipelineData " + name + " " + map + " " + smap();
    }
}

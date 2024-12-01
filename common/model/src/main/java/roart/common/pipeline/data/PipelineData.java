package roart.common.pipeline.data;

import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.HashMap;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
public class PipelineData {

    private String id;
    
    private String name;

    private Map<String, Object> map = new HashMap<>();

    private final SerialMap smap = new SerialMap();

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
        map.put(key, object);
    }
    
    public Object get(String key) {
        return map.get(key);
    }

    public Map<String, Object> getMap(String key) {
        return (Map<String, Object>) map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }
    
    public SerialMap smap() {
        return smap;
    }
}

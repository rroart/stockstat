package roart.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.springframework.context.annotation.Configuration;

public class ConfigMaps {
    public Map<String, Class> map = new HashMap();
    public Map<String, Object> deflt = new HashMap();
    public Map<String, String> text = new HashMap();
    public Map<String, Double[]> range = new HashMap();
    public Map<String, String> conv = new HashMap();
    
    public ConfigMaps() {
        // for jackson
    }
    
    public ConfigMaps(Map<String, Class> map, Map<String, Object> deflt, Map<String, String> text,
            Map<String, Double[]> range, Map<String, String> conv) {
        super();
        this.map = map;
        this.deflt = deflt;
        this.text = text;
        this.range = range;
        this.conv = conv;
    }

    public void add(ConfigMaps instance) {
        intersect(new HashSet(map.keySet()), new HashSet(instance.map.keySet()));
        intersect(new HashSet(deflt.keySet()), new HashSet(instance.deflt.keySet()));
        intersect(new HashSet(text.keySet()), new HashSet(instance.text.keySet()));
        intersect(new HashSet(range.keySet()), new HashSet(instance.range.keySet()));
        intersect(new HashSet(conv.keySet()), new HashSet(instance.conv.keySet()));
        map.putAll(instance.map);
        deflt.putAll(instance.deflt);
        text.putAll(instance.text);
        range.putAll(instance.range);
        conv.putAll(instance.conv);
    }

    private void intersect(Set<String> setA, Set<String> setB) {
        setA.retainAll(setB);
        System.out.println("diff " + setA);
    }
    
}

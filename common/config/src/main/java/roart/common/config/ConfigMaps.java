package roart.common.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

public class ConfigMaps {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Map<String, Class> map = new HashMap();
    public Map<String, Object> deflt = new HashMap();
    public Map<String, String> text = new HashMap();
    public Map<String, Double[]> range = new HashMap();
    public Map<String, String> conv = new HashMap();
    public Set<String> keys = new HashSet();
    
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

    public ConfigMaps copy() {
        ConfigMaps copy = new ConfigMaps(new HashMap<>(map), new HashMap<>(deflt), new HashMap<>(text), new HashMap<>(range), new HashMap<>(conv));
        copy.keys = new HashSet<>(keys);
        return copy;
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
        log.debug("Diff {}", setA);
    }

    public void keys(Set<String> mykeys) {
        log.debug("Keys: {}", mykeys);
        log.debug("Deflt keys {}", deflt.keySet());
        map.keySet().retainAll(mykeys);
        deflt.keySet().retainAll(mykeys);
        text.keySet().retainAll(mykeys);
        log.debug("Deflt keys {}", deflt.keySet());
        if (true) return;
        retain(map.keySet(), mykeys);
        retain(deflt.keySet(), mykeys);
        retain(text.keySet(), mykeys);        
    }

    private void retain(Set<String> keySet, Set<String> mykeys) {
        Set<String> retain = new HashSet<>();
        for (String aKey : keySet) {
            String origKey = aKey;
            aKey = aKey.replaceAll("\\[.*\\]", "");
            for (String anotherKey : mykeys) {
                anotherKey = anotherKey.replaceAll("\\[.*\\]", "");
                if (aKey.equals(anotherKey)) {
                    retain.add(origKey);
                }
            }
        }
        keySet.retainAll(retain);
    }
    
}

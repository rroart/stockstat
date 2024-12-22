package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class SerialMap<K, V extends SerialObject> extends SerialObject {

    private Map<K, V> map = new HashMap<>();

    public SerialMap() {
        super();
    }

    public SerialMap(Map<K, V> map) {
        super();
        this.map = map;
    }

    public Map<K, V> getMap() {
        return map;
    }

    public void setMap(Map<K, V> map) {
        this.map = map;
    }

    public SerialMap<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    public V get(K key) {
        return map.get(key);
    }

    public Set<K> keySet() {
        return map.keySet();
    }

    public Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }
    
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }
}
    

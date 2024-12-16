package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SerialMapdd extends SerialObject {

    private Map<String, double[][]> map = new HashMap<>();

    public SerialMapdd() {
        super();
    }

    public SerialMapdd(Map<String, double[][]> map) {
        super();
        this.map = map;
    }

    public Map<String, double[][]> getMap() {
        return map;
    }

    public void setMap(Map<String, double[][]> map) {
        this.map = map;
    }

    public SerialMapdd put(String key, double[][] value) {
        map.put(key, value);
        return this;
    }

    public Object get(String key) {
        return map.get(key);
    }
    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, double[][]>> entrySet() {
        return map.entrySet();
    }
}

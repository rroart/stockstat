package roart.common.pipeline.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class SerialMapVolume extends SerialObject {
    private Map<String, SerialVolume[]> map = new HashMap<>();

    public SerialMapVolume() {
        super();
    }

    public SerialMapVolume(Map<String, SerialVolume[]> map) {
        super();
        if (map == null) {
            int jj = 0;
        }
        this.map = map;
    }

    public Map<String, SerialVolume[]> getMap() {
        return map;
    }

    public void setMap(Map<String, SerialVolume[]> map) {
        this.map = map;
    }

    public SerialMapVolume put(String key, SerialVolume[] value) {
        map.put(key, value);
        return this;
    }

    public SerialVolume[] get(String key) {
        if (map == null) {
            int jj = 0;
        }
        return map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public Set<Entry<String, SerialVolume[]>> entrySet() {
        return map.entrySet();
    }
}

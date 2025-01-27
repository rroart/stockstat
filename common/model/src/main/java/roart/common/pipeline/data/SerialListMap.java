package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SerialListMap extends SerialObject {
    private List<SerialKeyValue> map = new ArrayList<>();

    public SerialListMap() {
        super();
    }

    public SerialListMap(List<SerialKeyValue> map) {
        super();
        this.map = map;
    }

    public SerialListMap(Map<String, Object> aMap) {
        super();
        for (Entry<String, Object> entry : aMap.entrySet()) {
            Object object = entry.getValue();
            if (object instanceof String serialobject) {
                object = new SerialString(serialobject);
            }
            map.add(new SerialKeyValue(entry.getKey(), (SerialObject) object));
        }
    }

    public List<SerialKeyValue> getMap() {
        return map;
    }

    public void setMap(List<SerialKeyValue> map) {
        this.map = map;
    }

    public SerialObject put(String key, SerialObject value) {
        int index = indexOf(key);
        if (index >= 0) {
            SerialKeyValue kv = map.get(index);
            map.set(index, new SerialKeyValue(key, value));
            return kv;
        } else {
            map.add(new SerialKeyValue(key, value));
            return null;
        }
    }

    public SerialObject get(String key) {
        int index = indexOf(key);
        if (index >= 0) {
            return map.get(index).getValue();
        } else {
            return null;
        }
    }

    public Set<String> keySet() {
        return map.stream().map(SerialKeyValue::getKey).collect(Collectors.toSet());
    }

    public boolean containsKey(String key) {
        return indexOf(key) >= 0;
    }
    
    private int indexOf(String key) {
        return IntStream.range(0, map.size())
                .filter(streamIndex -> key.equals(map.get(streamIndex).getKey()))
                .findFirst()
                .orElse(-1);
    }
}

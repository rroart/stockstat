package roart.common.pipeline.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SerialListMapPlain extends SerialObject {
    private List<SerialPairPlain> map = new ArrayList<>();

    public SerialListMapPlain() {
        super();
    }

    public SerialListMapPlain(List map) {
        super();
        this.map = map;
    }

    public SerialListMapPlain(Map<String, Object> aMap) {
        super();
        for (Entry<String, Object> entry : aMap.entrySet()) {
            Object object = entry.getValue();
            map.add(new SerialPairPlain(entry.getKey(), (Object) object));
        }
    }

    public List<SerialPairPlain> getMap() {
        return map;
    }

    public void setMap(List<SerialPairPlain> map) {
        this.map = map;
    }

    public Object put(String key, Object value) {
        int index = indexOf(key);
        if (index >= 0) {
            Object kv = map.get(index);
            map.set(index, new SerialPairPlain(key, value));
            return kv;
        } else {
            map.add(new SerialPairPlain(key, value));
            return null;
        }
    }

    public Object get(String key) {
        int index = indexOf(key);
        if (index >= 0) {
            return map.get(index).getRight();
        } else {
            return null;
        }
    }

    public Set<Object> keySet() {
        return map.stream().map(SerialPairPlain::getLeft).collect(Collectors.toSet());
    }

    public boolean containsKey(String key) {
        return indexOf(key) >= 0;
    }
    
    private int indexOf(String key) {
        return IntStream.range(0, map.size())
                .filter(streamIndex -> key.equals(map.get(streamIndex).getLeft()))
                .findFirst()
                .orElse(-1);
    }

}

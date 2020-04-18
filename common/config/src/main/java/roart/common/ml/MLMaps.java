package roart.common.ml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;

public abstract class MLMaps {

    public abstract Map<Pair<String, String>, String> getMap();

    public abstract List<String> getOtherList();
    
    public abstract Map<Pair<String, String>, String> getMapPersist();

    public Map<String, Pair<String, String>> getMapRev() {
        Map<Pair<String, String>, String> aMap = getMap();
        Map<String, Pair<String, String>> retMap = new HashMap<>();
        for (Entry<Pair<String, String>, String> entry : aMap.entrySet()) {
            retMap.put(entry.getValue(), entry.getKey());
        }
        return retMap;
    }

}

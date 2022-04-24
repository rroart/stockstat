package roart.common.util;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ImmutabilityUtil {
    private ImmutabilityUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static Map<String, Object> immute(Map<Object, Object> map) {
        Map<String, Object> retMap = new HashMap<>();
        for (Entry<Object, Object> entry : map.entrySet()) {
            String key = (String) entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Collection collection) {
                value = Collections.unmodifiableCollection(collection);
            }
            if (value instanceof Map amap) {
                value = immute(amap);
            }
            retMap.put(key, value);
        }
        return Collections.unmodifiableMap(retMap);
    }
}

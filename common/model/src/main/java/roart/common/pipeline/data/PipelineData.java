package roart.common.pipeline.data;

import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;

public class PipelineData extends SerialObject {

    private String id;
    
    private boolean loaded = true;
    
    private boolean old = false;
    
    private String message;
    
    //private Map<String, Object> map = new HashMap<>();

    //private SerialMap smap = new SerialMap();

    //private Set<String> usedKeys = new HashSet<>();

    private SerialPipelineKey key;

    private SerialObject value;

    private List<PipelineDataBatch> batch = new ArrayList<>();

    private boolean useInmemory;

    public PipelineData() {
        super();
        this.loaded = true;
        this.old = false;
    }

    public PipelineData(String key, SerialObject value, boolean useInmemory) {
        this(new SerialPipelineKey(new String[] {key, null, null, null, null}), value, useInmemory);
    }

    public PipelineData(String key, String secondKey, SerialObject value, boolean useInmemory) {
        this(new SerialPipelineKey(new String[] { key, secondKey, null, null, null }), value, useInmemory);
    }

    public PipelineData(String key, String secondKey, String thirdKey, SerialObject value, boolean useInmemory) {
        this(new SerialPipelineKey(new String[] { key, secondKey, thirdKey, null, null }), value, useInmemory);
    }

    public PipelineData(String key, String secondKey, String thirdKey, String fourthKey, SerialObject value, boolean useInmemory) {
        this(new SerialPipelineKey(new String[] { key, secondKey, thirdKey, fourthKey, null }), value, useInmemory);
    }

    public PipelineData(String key, String secondKey, String thirdKey, String fourthKey, String fifthKey, SerialObject value, boolean useInmemory) {
        this(new SerialPipelineKey(new String[] { key, secondKey, thirdKey, fourthKey, fifthKey }), value, useInmemory);
    }

    public PipelineData(SerialPipelineKey key, SerialObject value, boolean useInmemory) {
        super();
        this.loaded = true;
        this.old = false;
        this.key = key;
        this.value = value;
        this.useInmemory = useInmemory;
    }

    public PipelineData clone() {
        PipelineData newData = new PipelineData();
        newData.setId(this.id);
        newData.setLoaded(this.loaded);
        newData.setOld(this.old);
        newData.setMessage(this.message);
        newData.key = this.key.clone();
        newData.setValue(this.value);
        newData.useInmemory = this.useInmemory;
        return newData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /*
    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    public void put(String key, Object object) {
        if (object == null) {
            return;
        }
        if (object instanceof SerialObject serialobject) {
            smap.put(key, serialobject);
            return;
        }
        if (object instanceof String serialobject) {
            smap.put(key, new SerialString(serialobject));
            return;
        }
        if (object instanceof Integer serialobject) {
            smap.put(key, new SerialInteger(serialobject));
            return;
        }
        if (object instanceof Double serialobject) {
            smap.put(key, new SerialDouble(serialobject));
            return;
        }
        log.error("Should not use ordinary map");
        map.put(key, object);
    }
    
    public Object get(String key) {
        usedKeys.add(key);
        if (smap.containsKey(key)) {
            return smap.get(key);
        }
        return map.get(key);
    }

    public Map<String, Object> getMap(String key) {
        usedKeys.add(key);
        if (smap.containsKey(key)) {
            SerialMapPlain amap = (SerialMapPlain) smap.get(key);
            return amap.getMap();
        }
        return (Map<String, Object>) map.get(key);
    }

    public Set<String> keySet() {
        return map.keySet();
    }
    
    
    public SerialMap getSmap() {
        return smap;
    }

    public void setSmap(SerialMap smap) {
        this.smap = smap;
    }

     */

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public boolean isOld() {
        return old;
    }

    public void setOld(boolean old) {
        this.old = old;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public SerialPipelineKey getKey() {
        return key;
    }

    public SerialObject getValue() {
        return value;
    }

    public void setValue(SerialObject value) {
        this.value = value;
    }

    public boolean isUseInmemory() {
        return useInmemory;
    }
/*
    @Deprecated
    public SerialMap smap() {
        return smap;
    }

    @Deprecated
    public void putAll(Map<String, Object> amap) {
        for (Entry<String, Object> entry : amap.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public List<SerialKeyValue> getListMap(String key) {
        usedKeys.add(key);
        if (smap.containsKey(key)) {
            SerialListMap amap = (SerialListMap) smap.get(key);
            return amap.getMap();
        }
        return null;
    }

    public void putAll(List<SerialKeyValue> listMap) {
        for (SerialKeyValue entry : listMap) {
            put(entry.getKey(), entry.getValue());
        }
    }

    @JsonIgnore
    public Set<String> getAllKeys() {
        return smap.keySet();
    }

    @JsonIgnore
    public Set<String> getUsedKeys() {
        return usedKeys;
    }

    @JsonIgnore
    public Set<String> getUnusedKeys() {
        Set<String> unusedKeys = new HashSet<>(getAllKeys());
        unusedKeys.removeAll(getUsedKeys());
        return unusedKeys;
    }

     */

    public String toString() {
        return "PipelineData " + ArrayUtils.toString(key);
    }

    public void restKey() {
        this.key = key.rotateLeft(null);
    }

    public void add(PipelineData datum, int batchnum) {
        batch.add(new PipelineDataBatch(datum));
        if (batch.size() != batchnum + 1) {
            log.error("Wrong batch");
        }
    }

    public SerialObject getValue(int batchnum) {
        if (batchnum >= batch.size()) {
            return null;
        }
        return batch.get(batchnum).getValue();
    }

    public String getMessage(int batchnum) {
        return batch.get(batchnum).getMessage();
    }

    public void setLoaded(boolean b, int batchnum) {
        batch.get(batchnum).setLoaded(b);

    }

    public void setValue(SerialObject value, int batchnum) {
        batch.get(batchnum).setValue(value);
    }
}

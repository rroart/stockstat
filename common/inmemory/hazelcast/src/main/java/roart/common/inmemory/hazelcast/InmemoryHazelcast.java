package roart.common.inmemory.hazelcast;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;

import com.hazelcast.core.HazelcastInstance;

public class InmemoryHazelcast  extends Inmemory {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    Map<String, String> map = null;
    
    public String put(String k, String v) {
        try {
            return map.put(k, v);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }

    public String remove(String k) {
        try {
            return map.remove(k);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }

    public InmemoryHazelcast(String mapname) {
        super(mapname);
        mapname = Constants.HZ;
        HazelcastInstance hz = GetHazelcastInstance.instance();
        map = hz.getMap(mapname);       
    }

    public Map<String, String> getAll() {
        return map;
    }
    
    public int size() {
        return map.size();
    }

    @Override
    protected String getServer() {
        return Constants.HAZELCAST;
    }

    @Override
    public int getLimit() {
        return 0;
    }

    @Override
    protected void set(String key, String value) {
        map.put(key, value);
    }

    @Override
    protected String get(String key) {
        String string = map.get(key);
        return string;
    }

    @Override
    protected void del(String key) {
        map.remove(key);
    }
}

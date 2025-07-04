package roart.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.util.JsonUtil;

public class TestInmemory extends Inmemory {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private static Map<String, Object> map = new HashMap<>();
    
    public TestInmemory(String server) {
        super(server);
    }

    @Override
    public InmemoryMessage send(String id, Object data, String md5) {
        Object old = map.put(id, JsonUtil.convert(data, mapper));
        System.out.println("old" + map.get(id));
        if (old != null) {
            log.error("TODO already sent {}", id);
        }
        return new InmemoryMessage(getServer(), id, 0, md5);
    }
    
    @Override
    public String read(InmemoryMessage m) {
        log.info("reading {}", m.getId());
        return (String) map.get(m.getId());
    }
    
    @Override
    public void delete(InmemoryMessage m) {        
        Object prev = map.remove(m.getId());
        if (prev == null) {
            log.error("Key did not exist {}", m.getId());
        }
    }

    @Override
    protected int getLimit() {
        return 0;
    }

    @Override
    protected String getServer() {
        return null;
    }

    @Override
    protected void set(String key, String value) {
    }

    @Override
    protected String get(String key) {
        return null;
    }

    @Override
    protected void del(String key) {        
        Object prev = map.remove(key);
        if (prev == null) {
            log.error("Key did not exist {}", key);
        }
    }

    public void stat() {
        log.info("Stats {}", map.keySet());
    }

    public boolean isEmpty() {
        return map.size() == 0;
    }

    public void clear() {
        map.clear();
    }
    
    public Map<String, Object> getMap() {
        return map;
    }
}

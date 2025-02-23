package roart.controller;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.util.JsonUtil;

public class TestInmemory extends Inmemory {
    private static final ObjectMapper mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();

    private static Map<String, Object> map = new HashMap<>();
    
    public TestInmemory(String server) {
        super(server);
    }

    @Override
    public InmemoryMessage send(String id, Object data, String md5) {
        map.put(id, JsonUtil.convert(data, mapper));
        return new InmemoryMessage(getServer(), id, 0, md5);
    }
    
    @Override
    public String read(InmemoryMessage m) {
        return (String) map.get(m.getId());
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
        map.remove(key);
    }

}

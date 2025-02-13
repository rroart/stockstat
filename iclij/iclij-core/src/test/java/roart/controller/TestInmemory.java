package roart.controller;

import java.util.HashMap;
import java.util.Map;

import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.util.JsonUtil;

public class TestInmemory extends Inmemory {
    private static Map<String, Object> map = new HashMap<>();
    
    public TestInmemory(String server) {
        super(server);
    }

    @Override
    public InmemoryMessage send(String id, Object data, String md5) {
        map.put(id, JsonUtil.convert(data));
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

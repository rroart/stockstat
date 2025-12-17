package roart.common.inmemory.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.util.JsonUtil;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public abstract class Inmemory {
    
    private static Logger log = LoggerFactory.getLogger(Inmemory.class);

    protected abstract int getLimit();
    
    protected abstract String getServer();

    private static final ObjectMapper mapper = new JsonMapper().builder().build();
 
    public Inmemory(String server) {
            
    }
    
    public void t() {
        
    }
    
    public InmemoryMessage send(String id, Object data) {
        return send(id, data, null);
    }
    
    public InmemoryMessage send(String id, Object data, String md5) {
        String string;
        if (data instanceof String) {
            string = (String) data;
        } else {
            string = JsonUtil.convert(data, mapper);
        }
        if (string == null) {
            string = "";
        }
        log.info("MATCH {} {}", id, string.length());
        int count = 1;
        int limit = string.length();
        if (getLimit() > 0) {
            count = (int) Math.ceil(((double) string.length()) / getLimit());
            limit = getLimit();
        }
        InmemoryMessage message = new InmemoryMessage(getServer(), id, count);
        for (int i = 0; i < count; i++) {
            InmemoryMessage messageKey = new InmemoryMessage(getServer(), id, i, md5);
            String messageKeyString = JsonUtil.convert(messageKey);
            int max = Math.min(string.length(), (i + 1) * limit);
            String value = string.substring(i * limit, max);
            set(messageKeyString, value);
        }
        return message;
    }

    public String read(InmemoryMessage m) {
        StringBuilder stringBuilder = new StringBuilder("");
        int count = m.getCount();
        for (int i = 0; i < count; i++) {
            InmemoryMessage messkageKey = new InmemoryMessage(m.getServer(), m.getId(), i);
            String messageKeyString = JsonUtil.convert(messkageKey);
            String string = get(messageKeyString);
            stringBuilder.append(string);
        }        
        return stringBuilder.toString();
    }
    
    public void delete(InmemoryMessage m) {
        StringBuilder stringBuilder = new StringBuilder("");
        int count = m.getCount();
        for (int i = 0; i < count; i++) {
            InmemoryMessage messkageKey = new InmemoryMessage(m.getServer(), m.getId(), i);
            String messageKeyString = JsonUtil.convert(messkageKey);
            del(messageKeyString);
        }        
    }
    
    protected abstract void set(String key, String value);
    
    protected abstract String get(String key);
    
    protected abstract void del(String key);
}

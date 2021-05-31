package roart.common.inmemory.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.util.JsonUtil;

public abstract class Inmemory {
    
    protected abstract int getLimit();
    
    protected abstract String getServer();
    
    public Inmemory(String server) {
            
    }
    
    public void t() {
        
    }
    
    public InmemoryMessage send(String id, Object data) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        String string = JsonUtil.convert(data, mapper);
        if (string == null) {
            string = "";
        }
        int count = 1;
        int limit = string.length();
        if (getLimit() > 0) {
            count = (int) Math.ceil(((double) string.length()) / getLimit());
            limit = getLimit();
        }
        InmemoryMessage message = new InmemoryMessage(getServer(), id, count);
        for (int i = 0; i < count; i++) {
            InmemoryMessage messageKey = new InmemoryMessage(getServer(), id, i);
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

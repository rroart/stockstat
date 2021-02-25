package roart.common.inmemory.redis;

import redis.clients.jedis.Jedis;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;

public class InmemoryJedis extends Inmemory {

    Jedis jedis;
    
    public InmemoryJedis(String server) {
        super(server);
        jedis = new Jedis(server);
    }

    @Override
    protected String getServer() {
        return Constants.REDIS;
    }

    @Override
    public int getLimit() {
        return 512000000;
    }

    @Override
    protected void set(String key, String value) {
        jedis.set(key, value);
    }

    @Override
    protected String get(String key) {
        String string = jedis.get(key);
        jedis.del(key);
        return string;
    }
}

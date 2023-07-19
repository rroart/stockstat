package roart.common.inmemory.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;

public class InmemoryJedis extends Inmemory {

    private JedisPool pool;

    public InmemoryJedis(String server) {
        super(server);
        pool = new JedisPool(server);
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
        try (Jedis jedis = pool.getResource()) {
            jedis.set(key, value);
        }
    }

    @Override
    protected String get(String key) {
        try (Jedis jedis = pool.getResource()) {
            String string = jedis.get(key);
            return string;
        }
    }

    @Override
    protected void del(String key) {
        try (Jedis jedis = pool.getResource()) {
            jedis.del(key);
        }
    }
}

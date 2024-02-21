package roart.common.inmemory.redis;

import java.util.HashMap;
import java.util.Map;

import redis.clients.jedis.JedisPool;

public class JedisPools {
 
    private static Map<String, JedisPool> map = new HashMap<>();

    public static JedisPool get(String server) {
        map.computeIfAbsent(server, v -> new JedisPool(server));
        return map.get(server);
    }

}

package roart.common.inmemory.redis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import redis.clients.jedis.JedisPool;

public class JedisPools {
 
    private static Map<String, JedisPool> map = new ConcurrentHashMap<>();

    public static JedisPool get(String server) {
        map.computeIfAbsent(server, v -> new JedisPool(server));
        return map.get(server);
    }

}

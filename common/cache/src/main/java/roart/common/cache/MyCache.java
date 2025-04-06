package roart.common.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;

import roart.common.constants.Constants;

public class MyCache {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static boolean cacheme;
    private static int ttl;
    
    public boolean isCacheme() {
        return cacheme;
    }

    public static void setCache(boolean acacheme) {
        cacheme = acacheme;
    }

    public int getTtl() {
        return ttl;
    }

    public static void setCacheTTL(int attl) {
        ttl = attl;
    }

    public static void setInstance(MyCache instance) {
        MyCache.instance = instance;
    }

    private CacheLoader<String, Object> loader;
    private Cache<Object, Object> cache;
    
    private static MyCache instance = null;
    
    private MyCache() {
        if (!cacheme) {
            return;
        }
        loader = new CacheLoader<String, Object>() {
            @Override
            public String load(String key) {
                return key;
            }
        };
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(ttl, TimeUnit.SECONDS)
                .build();
    }
    
    public static MyCache getInstance() {
        if (instance == null) {
            synchronized(MyCache.class) {
                instance = new MyCache();
            }
        }
        return instance;
    }
    
    public void put(String key, Object object) {
        if (!cacheme) {
            return;
        }
        cache.put(key, object);
        log.info("Cache put {} {}", key.hashCode(), key.substring(0, Math.min(key.length(), 20)));
    }
    
    public Object get(String key) {
        if (!cacheme) {
            return null;
        }
        Object object = null;
        try {
            object = cache.getIfPresent(key);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (object == null) {
            cache.invalidate(key);
        }
        log.debug("CacheStats {}", cache.stats());
        String keyHead = key.substring(0, Math.min(80, key.length()));
        log.debug("Cache key {} {}", keyHead, object != null);
        if (object != null) {
            log.info("Cache get {} {}", key.hashCode(), key.substring(0, Math.min(key.length(), 20)));
        }
        return object;
    }
    
    public void invalidate() {
        if (!cacheme) {
            return;
        }
        cache.invalidateAll();
    }
    
    public String toString() {
        Set keySet = new HashSet(cache.asMap().keySet());
        List<String> keys = ((Set<String>) keySet).stream().map(s -> s.substring(0, Math.min(s.length(), 20))).toList();
        return "" + cache.size() + " " + keys;
    }
}

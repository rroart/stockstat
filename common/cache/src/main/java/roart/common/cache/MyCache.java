package roart.common.cache;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import roart.common.constants.Constants;

public class MyCache {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private static boolean cacheme;
    private static int ttl;
    
    public Logger getLog() {
        return log;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

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

    public CacheLoader<String, Object> getLoader() {
        return loader;
    }

    public void setLoader(CacheLoader<String, Object> loader) {
        this.loader = loader;
    }

    public LoadingCache<String, Object> getCache() {
        return cache;
    }

    public void setCache(LoadingCache<String, Object> cache) {
        this.cache = cache;
    }

    public static void setInstance(MyCache instance) {
        MyCache.instance = instance;
    }

    private CacheLoader<String, Object> loader;
    private LoadingCache<String, Object> cache;
    
    private static MyCache instance = null;
    
    private MyCache() {
        if (!cacheme) {
            return;
        }
        loader = new CacheLoader<String, Object>() {
            @Override
            public String load(String key) {
                return key.toUpperCase();
            }
        };
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(ttl, TimeUnit.SECONDS)
                .build(loader);
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
    }
    
    public Object get(String key) {
        if (!cacheme) {
            return null;
        }
        Object object = null;
        try {
            object = cache.get(key);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        log.info("Cache key {} {}", key, object != null);
        return object;
    }
    
}

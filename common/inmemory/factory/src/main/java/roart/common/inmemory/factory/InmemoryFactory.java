package roart.common.inmemory.factory;

import roart.common.inmemory.model.Inmemory;
import roart.common.constants.Constants;
import roart.common.inmemory.hazelcast.InmemoryHazelcast;
import roart.common.inmemory.redis.InmemoryJedis;

public class InmemoryFactory {

    public static Inmemory get(String server, String hz, String redis) {
        switch (server) {
        case Constants.REDIS:
            return new InmemoryJedis(redis);
        case Constants.HAZELCAST:
            return new InmemoryHazelcast(hz);
        default:
            return null;    
        }
        
    }
}

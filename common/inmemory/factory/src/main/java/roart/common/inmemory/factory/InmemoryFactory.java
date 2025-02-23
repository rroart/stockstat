package roart.common.inmemory.factory;

import roart.common.inmemory.model.Inmemory;
import roart.common.constants.Constants;
import roart.common.inmemory.hazelcast.InmemoryHazelcast;
import roart.common.inmemory.redis.InmemoryJedis;
import roart.iclij.config.IclijConfig;

public class InmemoryFactory {

    public Inmemory get(String server, String hz, String redis) {
        switch (server) {
        case Constants.REDIS:
            return new InmemoryJedis(redis);
        case Constants.HAZELCAST:
            return new InmemoryHazelcast(hz);
        default:
            return null;    
        }
    }

    public Inmemory get(IclijConfig config) {
        return get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
    }
}

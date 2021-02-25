package roart.common.inmemory.hazelcast;

import com.hazelcast.config.Config;
import com.hazelcast.config.SerializerConfig;

import roart.common.inmemory.model.InmemoryMessage;

public class HazelcastConfig {

    static Config getHazelcastConfig() {
        SerializerConfig sc = new SerializerConfig()
            .setImplementation(new InmemoryMessageSerializer())
            .setTypeClass(InmemoryMessage.class);
        Config config = new Config();
        config.getSerializationConfig().addSerializerConfig(sc);
       return config;
    }
}

package roart.common.inmemory.hazelcast;

import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.config.Config;

public class GetHazelcastInstance {
    static HazelcastInstance hz = null;
    public static HazelcastInstance instance() {
        if (hz == null) {
            Config config = HazelcastConfig.getHazelcastConfig();
            hz = Hazelcast.newHazelcastInstance(config);
        }
        return hz;
    }
}

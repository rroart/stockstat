package roart.common.synchronization.impl;

import com.hazelcast.core.HazelcastInstance;
import org.apache.curator.framework.CuratorFramework;

import roart.common.constants.Constants;
import roart.common.synchronization.MySemaphore;

public class MySemaphoreFactory {
    public static MySemaphore create(String name, String locker, CuratorFramework curatorFramework, HazelcastInstance hz) {
        if (locker == null) {
            return new MyDummySemaphore();
        }
        switch (locker) {
        case Constants.HAZELCAST:
            return new MyHazelcastSemaphore(name, hz);
        case Constants.CURATOR:
            return new MyCuratorSemaphore(name, curatorFramework);
        case Constants.LOCAL:
            return new MyLocalSemaphore(name);
            /*
        case Constants.ZOOKEEPER:
            return new MyZookeeperLock();
            */
        }
        return new MyDummySemaphore();
    }

}

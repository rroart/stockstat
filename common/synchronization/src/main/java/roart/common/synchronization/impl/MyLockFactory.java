package roart.common.synchronization.impl;

import com.hazelcast.core.HazelcastInstance;
import org.apache.curator.framework.CuratorFramework;

import roart.common.constants.Constants;
import roart.common.synchronization.MyLock;

public class MyLockFactory {
    public static MyLock create(String name, String locker, CuratorFramework curatorFramework, HazelcastInstance hz) {
        if (locker == null) {
            return new MyDummyLock();
        }
        switch (locker) {
        case Constants.HAZELCAST:
            return new MyHazelcastLock(name, hz);
        case Constants.CURATOR:
            return new MyCuratorLock(name, curatorFramework);
        case Constants.LOCAL:
            return new MyLocalLock(name);
            /*
        case Constants.ZOOKEEPER:
            return new MyZookeeperLock();
            */
        }
        return new MyDummyLock();
    }

}

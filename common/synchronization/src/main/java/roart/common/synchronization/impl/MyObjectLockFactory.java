package roart.common.synchronization.impl;

import org.apache.curator.framework.CuratorFramework;

import roart.common.constants.Constants;
import roart.common.synchronization.MyObjectLock;

public class MyObjectLockFactory {
    public static MyObjectLock create(String name, String locker, CuratorFramework curatorFramework) {
        switch (locker) {
        case Constants.HAZELCAST:
            //return new MyHazelcastObjectLock(name, hz);
        case Constants.CURATOR:
            return new MyCuratorObjectLock(name, curatorFramework);
        }
        return null;
    }

}

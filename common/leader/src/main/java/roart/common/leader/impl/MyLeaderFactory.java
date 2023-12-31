package roart.common.leader.impl;

import org.apache.curator.framework.CuratorFramework;

import com.hazelcast.core.HazelcastInstance;

import roart.common.leader.MyLeader;

import roart.iclij.config.IclijConfig;

public class MyLeaderFactory {
    public MyLeader create(String id, IclijConfig conf, CuratorFramework curatorFramework, HazelcastInstance hz) {
        if (conf.getZookeeper() != null) {
            return new MyCuratorLeader(id, curatorFramework, hz);
        } else {
            return new MyHazelcastLeader(id, curatorFramework, hz);
        }
    }

}

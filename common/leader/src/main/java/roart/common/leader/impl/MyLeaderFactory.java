package roart.common.leader.impl;

import org.apache.curator.framework.CuratorFramework;

import com.hazelcast.core.HazelcastInstance;

import roart.common.leader.MyLeader;

import roart.iclij.config.IclijConfig;

public class MyLeaderFactory {
    public MyLeader create(String category, String id, IclijConfig conf, CuratorFramework curatorFramework, HazelcastInstance hz) {
        if (conf.getZookeeper() != null) {
            return new MyCuratorLeader(category, id, curatorFramework, hz);
        } else {
            return new MyHazelcastLeader(category, id, curatorFramework, hz);
        }
    }

}

package roart.common.leader.impl;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import roart.common.leader.MyLeader;

public class MyHazelcastLeader extends MyLeader {
    private static final String LOCKNAME = "leaderlock";

    private HazelcastInstance hz;
    
    private FencedLock lock;
    
    public MyHazelcastLeader(String id, CuratorFramework curatorFramework, HazelcastInstance hz) {
        this.hz = hz;
        lock = hz.getCPSubsystem().getLock(LOCKNAME);
    }
    
    @Override
    public boolean isLeader() {
        return lock.isLockedByCurrentThread();
    }

    @Override
    public void await() {
        lock.lock();        
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        return lock.tryLock(timeout, unit);
    }

    @Override
    public void close() {
        lock.unlock();
    }

}

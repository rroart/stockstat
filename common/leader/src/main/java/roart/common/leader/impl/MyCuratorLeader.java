package roart.common.leader.impl;

import java.io.EOFException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatch;

import com.hazelcast.core.HazelcastInstance;

import roart.common.constants.Constants;
import roart.common.leader.MyLeader;

public class MyCuratorLeader extends MyLeader {

    private CuratorFramework curatorFramework;

    private LeaderLatch latch;
    
    public MyCuratorLeader(String id, CuratorFramework curatorFramework, HazelcastInstance hz) {
        this.curatorFramework = curatorFramework;
        try {
            latch = new LeaderLatch(curatorFramework, "/latch", id);
            latch.start();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    @Override
    public boolean isLeader() {
        return latch.hasLeadership();
    }

    @Override
    public void await() {
        try {
            latch.await();
        } catch (EOFException | InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        try {
            return latch.await(timeout, unit);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return false;
        }
    }

    @Override
    public void close() {
        try {
            latch.close();
        } catch (IOException e) {
            log.error(Constants.EXCEPTION, e);
        }
        
    }
}

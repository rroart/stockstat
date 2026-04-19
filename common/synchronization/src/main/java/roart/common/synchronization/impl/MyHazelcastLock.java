package roart.common.synchronization.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.lock.FencedLock;

import roart.common.synchronization.MyLock;

public class MyHazelcastLock extends MyLock {
    private HazelcastInstance hz;
    
    FencedLock lock;
    
    public MyHazelcastLock(String path, HazelcastInstance hz) {
        super();
        this.hz = hz;
        this. lock = hz.getCPSubsystem().getLock(path);
    }

    @Override
    public void lock() throws Exception {
        lock.lock();
    }

    @Override
    public boolean tryLock() throws Exception {
        return lock.tryLock();
    }

    @Override
    public void unlock() {
        lock.unlock();
    }

    @Override
    public boolean isLocked() {
        return lock.isLocked();
    }

    public FencedLock getLock() {
        return lock;
    }

    public void setLock(FencedLock lock) {
        this.lock = lock;
    }

}

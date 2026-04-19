package roart.common.synchronization.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.ISemaphore;

import roart.common.synchronization.MySemaphore;

public class MyHazelcastSemaphore extends MySemaphore {
    private HazelcastInstance hz;
    
    ISemaphore lock;
    
    public MyHazelcastSemaphore(String path, HazelcastInstance hz) {
        super();
        this.hz = hz;
        this. lock = hz.getCPSubsystem().getSemaphore(path);
    }

    @Override
    public void lock() throws Exception {
        lock.acquire();;
    }

    @Override
    public boolean tryLock() throws Exception {
        return lock.tryAcquire(1);
    }

    @Override
    public void unlock() {
        lock.release();;
    }

    @Override
    public boolean isLocked() {
        return lock.availablePermits() == 0;
    }

    public ISemaphore getSemaphore() {
        return lock;
    }

    public void setSemaphore(ISemaphore lock) {
        this.lock = lock;
    }

}

package roart.common.synchronization.impl;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;

import roart.common.constants.Constants;
import roart.common.synchronization.MySemaphore;
//import roart.common.zkutil.ZKUtil;

import org.apache.curator.framework.CuratorFramework;

public class MyCuratorSemaphore extends MySemaphore {

    private String path;
    
    private CuratorFramework curatorClient;
       
    private InterProcessSemaphoreMutex lock;

    public MyCuratorSemaphore(String path, CuratorFramework curatorClient) {
        super();
        this.path = path;
        this.curatorClient = curatorClient;
        this.lock = new InterProcessSemaphoreMutex(curatorClient, /*ZKUtil*/MyCuratorLock.getAppidPath(Constants.DB) + path);
    }

    @Override
    public void lock() throws Exception {
        log.info("lock {}", path);
        lock.acquire();
        log.info("locka {}", path);
    }

    @Override
    public boolean tryLock() throws Exception {
        log.info("lock {}", path);
        long time = System.currentTimeMillis();
        boolean locked = lock.acquire(1, TimeUnit.SECONDS);
        log.info("locka {} {} {}", path, locked, System.currentTimeMillis() - time);
        return locked;
    }

    @Override
    public void unlock() {
        log.info("unlock {}", path);
        if (lock != null) {
            try {
                lock.release();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
           }
        }
        log.info("unlocka {}", path);
    }

    @Override
    public boolean isLocked() {
        return lock.isAcquiredInThisProcess();
    }

}

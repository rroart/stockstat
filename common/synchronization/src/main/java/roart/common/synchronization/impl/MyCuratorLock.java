package roart.common.synchronization.impl;

import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import roart.common.constants.Constants;
import roart.common.synchronization.MyLock;
//import roart.common.zkutil.ZKUtil;

import org.apache.curator.framework.CuratorFramework;

public class MyCuratorLock extends MyLock {

    private String path;
    
    private CuratorFramework curatorClient;
       
    private InterProcessMutex lock;

    public MyCuratorLock(String path, CuratorFramework curatorClient) {
        super();
        this.path = path;
        this.curatorClient = curatorClient;
        this.lock = new InterProcessMutex(curatorClient, /*ZKUtil.*/getAppidPath(Constants.DB) + path);
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
        boolean locked = lock.acquire(1, TimeUnit.SECONDS);
        log.info("locka {}", path);
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
        return lock.isOwnedByCurrentThread();
    }

    public static String getAppidPath(String elem) {
        return "/" + Constants.AETHER + appid() + "/" + elem + "/";
    }
    public static String appid() {
        String appid = System.getenv(Constants.APPID);
        if (appid != null) {
            return appid;
        } else {
            return "";
        }
    }}

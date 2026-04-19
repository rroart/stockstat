package roart.common.synchronization.impl;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import roart.common.synchronization.MyLock;

public class MyLocalLock extends MyLock {

    private static ConcurrentHashMap<String, ReentrantLock> map = new ConcurrentHashMap();

    ReentrantLock lock;

    private String path;

    public MyLocalLock(String path) {
        super();
        this.path = path;
    }

    @SuppressWarnings("squid:S2222")
    @Override
    public void lock() throws Exception {
        synchronized (MyLocalLock.class) {
            log.debug("before lock {}", path);
            //Lock lock = map.get(path);
            //synchronized (MyLocalLock.class) {
            //if (lock == null) {
            lock = map.computeIfAbsent(path, e -> new ReentrantLock());
            // }
            //}
            lock.lock();
            log.debug("after lock {}", path);
        }
    }

    @SuppressWarnings("squid:S2222")
    @Override
    public boolean tryLock() throws Exception {
        synchronized (MyLocalLock.class) {
            log.debug("before lock {}", path);
            lock = map.computeIfAbsent(path, e -> new ReentrantLock());
            //log.debug("after lock {}", path);
            return lock.tryLock();
        }
    }

    @Override
    public void unlock() {
        synchronized (MyLocalLock.class) {
            /*
            lock = map.get(path);
        if (lock == null) {
            System.out.println("Map " + path + " " + map);
        }
             */
            if (!lock.hasQueuedThreads()) {
                map.remove(path);
                if (lock.hasQueuedThreads()) {
                    map.put(path, lock);
                    //System.out.println("Back " + path);
                }
                //System.out.println("Rem " + path + " " + lock.getHoldCount() + " " + lock.getQueueLength());
            } else {
                //System.out.println("Lock " + path + " " + lock.getHoldCount() + " " + lock.getQueueLength());
            }
            // TODO?
            lock.unlock();
        }
        log.debug("after unlock {}", path);
    }

    @Override
    public boolean isLocked() {
        return lock.isLocked();
    }
}

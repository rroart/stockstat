package roart.common.synchronization.impl;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;

import roart.common.synchronization.MySemaphore;

public class MyLocalSemaphore extends MySemaphore {

    private static ConcurrentHashMap<String, Semaphore> map = new ConcurrentHashMap();

    Semaphore lock;

    private String path;

    public MyLocalSemaphore(String path) {
        super();
        this.path = path;
    }

    @SuppressWarnings("squid:S2222")
    @Override
    public void lock() throws Exception {
        synchronized (MyLocalSemaphore.class) {
            log.debug("before lock {}", path);
            //Semaphore lock = map.get(path);
            //synchronized (MyLocalSemaphore.class) {
            //if (lock == null) {
            lock = map.computeIfAbsent(path, e -> new Semaphore(1, true));
            // }
            //}
            lock.acquire();
            log.debug("after lock {}", path);
        }
    }

    @SuppressWarnings("squid:S2222")
    @Override
    public boolean tryLock() throws Exception {
        synchronized (MyLocalSemaphore.class) {
            log.debug("before lock {}", path);
            lock = map.computeIfAbsent(path, e -> new Semaphore(1, true));
            //log.debug("after lock {}", path);
            return lock.tryAcquire();
        }
    }

    @Override
    public void unlock() {
        synchronized (MyLocalSemaphore.class) {
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
                //System.out.println("Semaphore " + path + " " + lock.getHoldCount() + " " + lock.getQueueLength());
            }
            // TODO?
            lock.release();
        }
        log.debug("after unlock {}", path);
    }

    @Override
    public boolean isLocked() {
        return lock.availablePermits() == 0;
    }
}

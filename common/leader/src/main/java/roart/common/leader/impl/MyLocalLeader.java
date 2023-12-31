package roart.common.leader.impl;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import roart.common.constants.Constants;
import roart.common.leader.MyLeader;

public class MyLocalLeader extends MyLeader {

    private static ConcurrentHashMap<String, ReentrantLock> map = new ConcurrentHashMap();

    ReentrantLock lock;

    private String path;

    public MyLocalLeader() {
        super();
        this.path = "leaderlock";
    }

    @Override
    public boolean isLeader() {
        return lock.isHeldByCurrentThread();
    }

    @Override
    public void await() {
        synchronized (MyLocalLeader.class) {
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

    @Override
    public boolean await(long timeout, TimeUnit unit) {
        try {
            synchronized (MyLocalLeader.class) {
                lock = map.computeIfAbsent(path, e -> new ReentrantLock());
                return lock.tryLock(timeout, unit);
            }
        } catch (InterruptedException e) {
            log.error(Constants.EXCEPTION, e);
            return false;
        }
    }

    @Override
    public void close() {
        lock.unlock();
    }

}

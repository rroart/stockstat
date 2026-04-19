package roart.common.synchronization.impl;

import org.redisson.Redisson;
import org.redisson.RedissonSemaphore;
import org.redisson.api.RedissonClient;
import org.redisson.command.CommandAsyncExecutor;
import org.redisson.config.Config;

import com.hazelcast.cp.ISemaphore;

import roart.common.synchronization.MySemaphore;

public class MyRedissonSemaphore extends MySemaphore {

    private volatile RedissonSemaphore lock;

    public MyRedissonSemaphore(String jserver, String name) {
        String server = jserver.replace("http", "redis");
        Config config = new Config();
        config.useSingleServer().setAddress(server);
        RedissonClient client = Redisson.create(config);
        CommandAsyncExecutor exe = ((Redisson) client).getCommandExecutor();
        lock = new RedissonSemaphore(exe, name);
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

    public RedissonSemaphore getSemaphore() {
        return lock;
    }

    public void setSemaphore(RedissonSemaphore lock) {
        this.lock = lock;
    }

}

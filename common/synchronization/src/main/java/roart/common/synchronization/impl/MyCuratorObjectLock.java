package roart.common.synchronization.impl;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.atomic.AtomicValue;
import org.apache.curator.framework.recipes.atomic.DistributedAtomicValue;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.fasterxml.jackson.annotation.JsonIgnore;

import roart.common.constants.Constants;
import roart.common.synchronization.MyObjectLock;
import roart.common.synchronization.MyObjectLockData;
//import roart.common.zkutil.ZKUtil;

public class MyCuratorObjectLock extends MyObjectLock {

    private MyObjectLockData path;
    
    @JsonIgnore
    private CuratorFramework curatorClient;
       
    @JsonIgnore
    private DistributedAtomicValue lock;
    
    public MyCuratorObjectLock() {
        super();
        // for json
    }
    
    public MyCuratorObjectLock(String path, CuratorFramework curatorClient) {
        super();
        this.path = new MyObjectLockData(path);
        this.curatorClient = curatorClient;
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3); 
        this.lock = new DistributedAtomicValue(curatorClient, /*ZKUtil*/MyCuratorLock.getAppidPath(Constants.DB) + path, retryPolicy);
    }

    @Override
    public boolean tryLock(String id) throws Exception {
        lock.initialize("".getBytes());
        return lock.compareAndSet("".getBytes(), id.getBytes()).succeeded();
    }

    @Override
    public void unlock() {
        try {
            lock.forceSet("".getBytes());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }

    @JsonIgnore
    @Override
    public boolean isLocked() {
        try {
            AtomicValue<byte[]> value = lock.get();
            if (value.succeeded()) { 
                return !"".equals(new String(value.postValue()));
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return true;
    }

    public String getPath() {
        return path.id;
    }

    public void setPath(String path) {
        this.path = new MyObjectLockData(path);
    }

    @JsonIgnore
    public void setCuratorClient(CuratorFramework curatorClient) {
        this.curatorClient = curatorClient;
    }

    @JsonIgnore
    public void setLock(DistributedAtomicValue lock) {
        this.lock = lock;
    }

}

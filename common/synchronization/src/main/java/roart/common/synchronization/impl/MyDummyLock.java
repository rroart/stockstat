package roart.common.synchronization.impl;

import roart.common.synchronization.MyLock;

public class MyDummyLock extends MyLock {
    
    @Override
    public void lock() throws Exception {
    }

    @Override
    public boolean tryLock() throws Exception {
        return false;
    }

    @Override
    public void unlock() {
    }

    @Override
    public boolean isLocked() {
        return false;
    }

}

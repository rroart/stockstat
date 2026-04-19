package roart.common.synchronization.impl;

import roart.common.synchronization.MySemaphore;

public class MyDummySemaphore extends MySemaphore {
    
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

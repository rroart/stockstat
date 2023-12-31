package roart.common.leader;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MyLeader {
    protected static Logger log = LoggerFactory.getLogger(MyLeader.class);
    public abstract boolean isLeader();
    public abstract void await();
    public abstract boolean await(long timeout, TimeUnit unit);
    public abstract void close();
}

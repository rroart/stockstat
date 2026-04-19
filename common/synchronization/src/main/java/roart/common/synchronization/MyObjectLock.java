package roart.common.synchronization;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roart.common.synchronization.impl.MyCuratorObjectLock;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = MyCuratorObjectLock.class, name = "roart.common.synchronization.MyCuratorObjectLock"),  
})  
public abstract class MyObjectLock {
    protected static Logger log = LoggerFactory.getLogger(MyLock.class);
    public abstract boolean tryLock(String id) throws Exception;
    public abstract void unlock();
    public abstract boolean isLocked();

}

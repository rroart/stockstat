package roart.common.queue;

import java.util.Map;
import roart.common.inmemory.model.InmemoryMessage;

public class QueueElement {
    private String id;
    
    private String myid;
    
    private String opid;
    
    private String queue;

    private String md5;
    
    private String oldMd5;
    
    private Map<String, String> metadata;
    
    private InmemoryMessage message;

    private long timestamp;

    // for Jackson
    public QueueElement() {
        super();
    }
    
}

package roart.common.queue;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import roart.common.inmemory.model.InmemoryMessage;

public class QueueElement {
    private String id = UUID.randomUUID().toString();
    
    private String myid;
    
    private String opid;
    
    private String queue;

    private String md5;
    
    private Map<String, String> metadata;
    
    private InmemoryMessage message;

    private long timestamp;

    // for Jackson
    public QueueElement() {
        super();
    }
    
    public QueueElement(String myid, String queue) {
        super();
        this.id = UUID.randomUUID().toString();
        this.myid = myid;
        this.queue = queue;
        this.metadata = new HashMap<>();
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMyid() {
        return myid;
    }

    public void setMyid(String myid) {
        this.myid = myid;
    }

    public String getOpid() {
        return opid;
    }

    public void setOpid(String opid) {
        this.opid = opid;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public InmemoryMessage getMessage() {
        return message;
    }

    public void setMessage(InmemoryMessage message) {
        this.message = message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


}

package roart.common.inmemory.model;

public class InmemoryMessage {

    private String server;

    private String id;

    private int count;

    public InmemoryMessage() {
        super();
    }

    public InmemoryMessage(String bus, String id, int count) {
        super();
        this.server = bus;
        this.id = id;
        this.count = count;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    
}

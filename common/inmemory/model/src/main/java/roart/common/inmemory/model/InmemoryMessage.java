package roart.common.inmemory.model;

public class InmemoryMessage {

    private String server;

    private String id;

    private int count;

    private String md5;
    
    public InmemoryMessage() {
        super();
    }

    public InmemoryMessage(String bus, String id, int count) {
        super();
        this.server = bus;
        this.id = id;
        this.count = count;
    }

    public InmemoryMessage(String bus, String id, int count, String md5) {
        super();
        this.server = bus;
        this.id = id;
        this.count = count;
	this.md5 = md5;
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

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
}

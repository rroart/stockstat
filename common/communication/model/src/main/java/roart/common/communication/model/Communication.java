package roart.common.communication.model;

import java.lang.reflect.Array;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.util.JsonUtil;
import roart.common.util.MathUtil;

public abstract class Communication {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String service;
    protected ObjectMapper mapper;
    protected Class myclass;
    private String returnService;
    protected String myname;
    protected boolean send;
    protected boolean receive;
    protected boolean sendreceive;
    protected String connection;
    public Communication(String myname, Class myclass, String service, ObjectMapper mapper, boolean send, boolean receive, boolean sendreceive, String connection) {
        this.myclass = myclass;
        this.service = service;
        this.mapper = mapper;
        this.myname = myname;
        this.send = send;
        this.receive = receive;
        this.sendreceive = sendreceive;
        this.connection = connection;
        if (sendreceive) {
            try {
                this.returnService = service + InetAddress.getLocalHost().getHostAddress() + System.currentTimeMillis();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public ObjectMapper getMapper() {
        return mapper;
    }

    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public Class getMyclass() {
        return myclass;
    }

    public void setMyclass(Class myclass) {
        this.myclass = myclass;
    }

    public String getReturnService() {
        return returnService;
    }

    public void setReturnService(String returnService) {
        this.returnService = returnService;
    }

    public String getMyname() {
        return myname;
    }

    public void setMyname(String myname) {
        this.myname = myname;
    }

    protected String getSendService() {
        return service;
    }
    
    protected String getReceiveService() {
        if (returnService != null) {
            return returnService;            
        } else {
            return service;
        }
    }
        
    public abstract void send(String s);

    public void send(Object o) {
        System.out.println("sendchn " + getSendService());
        System.out.println("sendchn " + JsonUtil.convert(o));
        send(JsonUtil.convert(o));
    }

    public abstract String[] receiveString();

    public <T> T[] sendReceive2(Object param) {
        System.out.println("xxyy00");
        long time = System.currentTimeMillis();
        send(param);
        T[] r = receive();
        log.info("Rq time {}s for {} ", MathUtil.round((double) (System.currentTimeMillis() - time) / 1000, 1), service);
        //destroy();
        System.out.println("xxyy"+r);
        System.out.println("xxyy"+r.getClass().getName());
        return r;
    }

    public <T> T[] sendReceive(Object param) {
        System.out.println("xxyyzz00");
        long time = System.currentTimeMillis();
        send(param);
        T[] r = receive();
        log.info("Rq time {}s for {} ", MathUtil.round((double) (System.currentTimeMillis() - time) / 1000, 1), service);
        destroyTmp();
        System.out.println("xxyy"+r);
        System.out.println("xxyy"+r.getClass().getName());
        return r;
    }

    protected void destroyTmp() {
    }

    public <T> T[] receive() {
        System.out.println("recvchn " + getReceiveService());
        Class<T> aclass = myclass;
        String[] receives = receiveString();
        //T[] ts = new T[receives.length];
        T[] ts = (T[]) Array.newInstance(myclass, receives.length);
        int count = 0;
        for (String aReceive : receives) {
            System.out.println("t0"+aReceive);
            T t = JsonUtil.convertnostrip(aReceive, aclass);
            System.out.println("t"+t);
            ts[count++] = t;
        }
        return ts;
    }

    public abstract void destroy();
}

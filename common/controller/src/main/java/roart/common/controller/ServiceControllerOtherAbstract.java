package roart.common.controller;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.constants.CommunicationConstants;
import roart.common.constants.Constants;
import roart.common.queueutil.QueueUtils;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.iclij.config.IclijConfig;
import roart.model.io.IO;

public abstract class ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private String myservices;
    protected String services;
    protected String communications;
    private Class replyclass;
    protected IclijConfig iclijConfig;
    
    private Function<String, Boolean> zkRegister;

    protected IO io;
    
    public ServiceControllerOtherAbstract(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        this.myservices = myservices;
        this.services = services;
        this.communications = communications;
        this.replyclass = replyclass;
        this.iclijConfig = iclijConfig;
        this.io = io;
    }

    public void get(final Communication c) {
        Executor executor = Executors.newFixedThreadPool(4);
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (;;) {
                    Object[] params = new Object[0];
                    try {
                        params = c.receiveAndStore();
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                    for (Object param : params) {
                        Thread t2 = new Thread(new Runnable() {
                            public void run() { 
                                get(param, c);
                            }});
                        executor.execute(t2);
                        //t2.start();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        log.error(Constants.EXCEPTION, e);
                        Thread.currentThread().interrupt();
                    }
                }
            }});
        t.start();
    }

    public abstract void get(Object param, Communication c);

    public void start() {
        if (myservices == null || myservices.isEmpty()) {
            myservices = "{}";
        }
        Map<String, String> myservicelist = JsonUtil.convert(myservices, Map.class);
        Map<String, String> serviceMap = JsonUtil.convert(services, Map.class);
        Map<String, String> communicationsMap = JsonUtil.convert(communications, Map.class);
        for (Entry<String, String> myserviceEntry : myservicelist.entrySet()) {
            String myservice = myserviceEntry.getKey();
            String serviceType = myserviceEntry.getValue();
            boolean reply = serviceType.contains("r");
            String communication = serviceMap.get(myservice);
            if (communication == null) {
                communication = CommunicationConstants.REST;
            }
            if (communication.equals(CommunicationConstants.REST)) {
                continue;
            }
            String connection = communicationsMap.get(communication);
            if (connection == null) {
                connection = "localhost";
            }
            ObjectMapper objectMapper = new ObjectMapper();
            Class myclass = String.class;
            if (reply) {
                myclass = replyclass;
            }
            String appid = System.getenv(Constants.APPID);
            if (appid != null) {
                myservice = myservice + appid; // can not handle domain, only eureka
            }
            zkRegister = (new QueueUtils(io.getCuratorClient()))::zkRegister;
            Communication comm = io.getCommunicationFactory().get(communication, myclass, myservice, objectMapper, false, true, false, connection, zkRegister, null);
            get(comm);
        }        
    }
    
    protected void sendReply(String replypath, Communication c, Object r) {
        ObjectMapper objectMapper = new ObjectMapper();
        log.info("ReplyPath {}" + replypath);
        if (replypath != null) {
            String service = replypath;
            Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(c.getService(), services, communications);
            log.info("ServiceConnection {} {}", sc.getLeft(), sc.getRight());
            Communication c2 = io.getCommunicationFactory().get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight(), zkRegister, null);
            c2.send(r);
            c2.destroy();
        }
    }
    
    protected boolean serviceMatch(String str, Communication c) {
        String appid = System.getenv(Constants.APPID);
        if (appid == null) {
            appid = "";
        }
        return c.getService().equals(str + appid);
    }

}

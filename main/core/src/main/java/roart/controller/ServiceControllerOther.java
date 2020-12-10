package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.config.MyMyConfig;
import roart.common.constants.CommunicationConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.config.MyXMLConfig;

public class ServiceControllerOther {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public ServiceResult get(final Communication c, MyMyConfig config) { 
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (;;) {
                ServiceParam[] params = c.receive();
                for (ServiceParam param : params) {
                    Thread t2 = new Thread(new Runnable() {
                        public void run() { 
                            get(param, c, config);
                        }});
                    t2.start();
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                }
            }});
        t.start();
        return null;
    }

    public ServiceResult get(ServiceParam param, Communication c, MyMyConfig config) { 
        ServiceResult r = null;
        System.out.println("Cserv"+c.getService());
        switch (c.getService()) {
        case EurekaConstants.GETCONFIG:
            r = new ServiceResult();
            try {
                r.setConfig(MyXMLConfig.getConfigInstance());
                System.out.println("configs " + r.getConfig());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                r.setError(e.getMessage());
            }
            break;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        System.out.println("webp " + param.getWebpath());
        if (param.getWebpath() != null) {
            Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(c.getService(), config.getServices(), config.getCommunications());
            System.out.println("scsc" + sc.getLeft() + " " + sc.getRight());
            Communication c2 = CommunicationFactory.get(sc.getLeft(), ServiceResult.class, param.getWebpath(), objectMapper, true, false, false, sc.getRight());
            c2.send(r);
            c2.destroy();
        }
        return null;
    }

    public void start() {
        MyMyConfig instance = new MyMyConfig(MyXMLConfig.getConfigInstance());
        String myservices = instance.getMyservices();
        String services = instance.getServices();
        String communications = instance.getCommunications();
        String[] myservicelist = JsonUtil.convert(myservices, String[].class);
        Map<String, String> serviceMap = JsonUtil.convert(services, Map.class);
        Map<String, String> communicationsMap = JsonUtil.convert(communications, Map.class);
        for (String myservice : myservicelist) {
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
            Communication comm = CommunicationFactory.get(communication, ServiceParam.class, myservice, objectMapper, false, true, false, connection);
            get(comm, instance);
        }        
    }
}

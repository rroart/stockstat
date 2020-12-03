package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.model.Communication;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.communication.factory.CommunicationFactory;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.util.ServiceUtil;

public class ServiceControllerOther {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public IclijServiceResult getConfigOuter(final Communication c, IclijConfig config) { 
        Thread t = new Thread(new Runnable() {
            public void run() {
                for (;;) {
                IclijServiceParam[] params = c.receive();
                for (IclijServiceParam param : params) {
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

    @Deprecated
    public IclijServiceResult getVerifyOuter(final Communication c, IclijConfig config) { 
        Thread t = new Thread(new Runnable() {
            public void run() { 
                IclijServiceParam[] params = c.receive();
                for (IclijServiceParam param : params) {
                    Thread t2 = new Thread(new Runnable() {
                        public void run() { 
                            get(param, c, config);
                        }});
                    t2.start();
                }
            }});
        t.start();
        return null;
    }

    public IclijServiceResult get(IclijServiceParam param, Communication c, IclijConfig config) { 
        IclijServiceResult r = null;
        System.out.println("Cserv"+c.getService());
        switch (c.getService()) {
        case "TSTSR":
        case "TSTSR2":
        case "TSTSR3":
        case "TSTSR4":
            r = new IclijServiceResult();
            break;
        case EurekaConstants.GETVERIFY:
            r = ServiceUtil.getVerify(new ComponentInput(param.getIclijConfig(), null, null, null, param.getOffset(), false, false, new ArrayList<>(), new HashMap<>()));
            break;
        case EurekaConstants.GETCONFIG:
            r = new IclijServiceResult();
            try {
                r.setIclijConfig(IclijXMLConfig.getConfigInstance());
                System.out.println("configs " + r.getIclijConfig());
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
            Communication c2 = CommunicationFactory.get(sc.getLeft(), IclijServiceResult.class, param.getWebpath(), objectMapper, true, false, false, sc.getRight());
            c2.send(r);
            c2.destroy();
        }
        return null;
    }

    public void start() {
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        String services = instance.getServices();
        String communications = instance.getCommunications();
        Map<String, String> serviceMap = JsonUtil.convert(services, Map.class);
        Map<String, String> communicationsMap = JsonUtil.convert(communications, Map.class);
        for (Entry<String, String> entry : serviceMap.entrySet()) {
            String service = entry.getKey();
            String communication = serviceMap.get(service);
            if (communication == null) {
                communication = "REST";
            }
            if (communication.equals("REST")) {
                continue;
            }
            String connection = communicationsMap.get(communication);
            if (connection == null) {
                connection = "localhost";
            }
        }        
    }

}

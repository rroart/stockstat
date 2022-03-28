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
import roart.common.config.MyXMLConfig;
import roart.common.constants.CommunicationConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.controller.ServiceControllerOtherAbstract;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass) {
        super(myservices, services, communications, replyclass);
    }

    public void get(Object param, Communication c) { 
        ServiceResult r = null;
        System.out.println("Cserv"+c.getService());
        if (serviceMatch(EurekaConstants.GETCONFIG, c)) {
            r = new ServiceResult();
            try {
                r.setConfig(MyXMLConfig.getConfigInstance());
                System.out.println("configs " + r.getConfig());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                r.setError(e.getMessage());
            }
        }
        if (param instanceof ServiceParam) {
            sendReply(((ServiceParam) param).getWebpath(), c, r);
        }
    }
}

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
import roart.common.constants.CommunicationConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.util.ServiceUtil;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass) {
        super(myservices, services, communications, replyclass);
    }

    public void get(Object param, Communication c) { 
        IclijServiceResult r = null;
        System.out.println("Cserv"+c.getService());
        switch (c.getService()) {
        case "TSTSR":
        case "TSTSR2":
        case "TSTSR3":
        case "TSTSR4":
            r = new IclijServiceResult();
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
        if (param instanceof IclijServiceParam) {
            sendReply(((IclijServiceParam) param).getWebpath(), c, r);
        }
    }

}

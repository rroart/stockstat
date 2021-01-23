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
import roart.common.constants.ServiceConstants;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.common.controller.ServiceControllerOtherAbstract;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass) {
        super(myservices, services, communications, replyclass);
    }

    public void get(Object param, Communication c) { 
        IclijServiceResult r = null;
        System.out.println("Cserv"+c.getService());
        switch (c.getService()) {
        case ServiceConstants.EVOLVEFILTEREVOLVE:
            r = new IclijServiceResult();
            new Evolve().method((String) param);
            break;
        case ServiceConstants.EVOLVEFILTERPROFIT:
            r = new IclijServiceResult();
            new Evolve().method2((String) param);
            break;
        case ServiceConstants.EVOLVEFILTERFILTER:
            r = new IclijServiceResult();
            new Evolve().method3((String) param);
            break;
        case ServiceConstants.EVOLVEFILTERABOVEBELOW:
            r = new IclijServiceResult();
            new Evolve().method4((String) param);
            break;
        }
        if (param instanceof IclijServiceParam) {
            sendReply(((IclijServiceParam) param).getWebpath(), c, r);
        }
    }


}

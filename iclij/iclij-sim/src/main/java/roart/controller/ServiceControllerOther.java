package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.constants.CommunicationConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.ServiceConstants;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.common.controller.ServiceControllerOtherAbstract;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    //@Autowired
    //IclijConfig iclijConfig;
    
    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IclijDbDao dbDao) {
        super(myservices, services, communications, replyclass, iclijConfig, dbDao);
    }

    public void get(Object param, Communication c) { 
        IclijServiceResult r = null;
        System.out.println("Cserv"+c.getService());
        if (serviceMatch(ServiceConstants.SIMFILTER, c)) {
            new Sim(iclijConfig, dbDao).method((String) param, "sim", true);
        }
        if (serviceMatch(ServiceConstants.SIMAUTO, c)) {
            new Sim(iclijConfig, dbDao).method((String) param, "simauto", false);
        }
        if (param instanceof IclijServiceParam) {
            sendReply(((IclijServiceParam) param).getWebpath(), c, r);
        }
    }


}

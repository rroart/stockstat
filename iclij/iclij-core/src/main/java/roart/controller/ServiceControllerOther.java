package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutableTriple;
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
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.populate.PopulateThread;
import roart.util.ServiceUtil;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IclijDbDao dbDao) {
        super(myservices, services, communications, replyclass, iclijConfig, dbDao);
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
        }
        if (serviceMatch(EurekaConstants.GETCONFIG, c)) {
            r = new IclijServiceResult();
            try {
                r.setConfigData(iclijConfig.getConfigData());
                System.out.println("configs " + r.getConfigData());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                r.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.POPULATE, c)) {
        	String param2 = getParam((String) param);
        	String[] item = JsonUtil.convert(param2, String[].class);
            PopulateThread.queue.add(new ImmutableTriple(item[0], item[1], item[2]));
        }
        if (param instanceof IclijServiceParam) {
            sendReply(((IclijServiceParam) param).getWebpath(), c, r);
        }
    }

    private String getParam(String param) {
        InmemoryMessage message = JsonUtil.convert(param, InmemoryMessage.class);
        Inmemory inmemory = InmemoryFactory.get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String newparam = inmemory.read(message);
        inmemory.delete(message);
        return newparam;
    }

}

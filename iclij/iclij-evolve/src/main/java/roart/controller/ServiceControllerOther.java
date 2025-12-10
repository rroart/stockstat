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

import tools.jackson.databind.ObjectMapper;

import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.constants.CommunicationConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.ServiceConstants;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.evolve.Evolve;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.queue.QueueElement;
import roart.common.queueutil.QueueUtils;
import roart.model.io.IO;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        super(myservices, services, communications, replyclass, iclijConfig, io);
    }

    public void get(Object param, Communication c) { 
        QueueElement element = JsonUtil.convert((String) param, QueueElement.class);
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());
        IclijServiceResult r = null;
        log.info("Content {}", content);
        log.debug("Cserv {}", c.getService());
        if (serviceMatch(ServiceConstants.EVOLVEFILTEREVOLVE, c)) {
            r = new IclijServiceResult();
            new Evolve(iclijConfig, io).handleEvolve((String) content);
        }
        if (serviceMatch(ServiceConstants.EVOLVEFILTERPROFIT, c)) {
            r = new IclijServiceResult();
            new Evolve(iclijConfig, io).handleProfit((String) content);
        }
        if (serviceMatch(ServiceConstants.EVOLVEFILTERFILTER, c)) {
            r = new IclijServiceResult();
            new Evolve(iclijConfig, io).handleFilter((String) content);
        }
        if (serviceMatch(ServiceConstants.EVOLVEFILTERABOVEBELOW, c)) {
            r = new IclijServiceResult();
            new Evolve(iclijConfig, io).handleAboveBelow((String) content);
        }
        new QueueUtils(io.getCuratorClient()).zkUnregister((String) param);

        if (param instanceof IclijServiceParam) {
            sendReply(((IclijServiceParam) param).getWebpath(), c, r);
        }
    }


}

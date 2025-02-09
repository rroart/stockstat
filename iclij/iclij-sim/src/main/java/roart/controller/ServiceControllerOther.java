package roart.controller;

import java.io.InputStream;
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
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.sim.Sim;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryUtil;
import roart.common.queue.QueueElement;
import roart.common.queueutil.QueueUtils;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    //@Autowired
    //IclijConfig iclijConfig;
    
    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IclijDbDao dbDao, FileSystemDao fileSystemDao) {
        super(myservices, services, communications, replyclass, iclijConfig, dbDao, fileSystemDao);
    }

    public void get(Object param, Communication c) {
        log.info("param" + (String) param);
        QueueElement element = JsonUtil.convert((String) param, QueueElement.class);
        Inmemory inmemory = InmemoryFactory.get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());
        IclijServiceResult r = null;
        log.debug("Cserv {}", c.getService());
        log.info("Content {}", content);
        if (serviceMatch(ServiceConstants.SIMFILTER, c)) {
            new Sim(iclijConfig, dbDao, fileSystemDao).method((String) content, "sim", true);
        }
        if (serviceMatch(ServiceConstants.SIMAUTO, c)) {
            new Sim(iclijConfig, dbDao, fileSystemDao).method((String) content, "simauto", false);
        }
        new QueueUtils(IclijController.curatorClient).zkUnregister((String) param);
        if (param instanceof IclijServiceParam) {
            sendReply(((IclijServiceParam) param).getWebpath(), c, r);
        }
        // element ack
    }


}

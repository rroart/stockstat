package roart.controller;

import roart.common.communication.model.Communication;
import roart.common.constants.ServiceConstants;
import roart.common.util.JsonUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.common.service.IclijServiceResult;
import roart.sim.Sim;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.inmemory.model.Inmemory;
import roart.common.queue.QueueElement;
import roart.common.queueutil.QueueUtils;
import roart.model.io.IO;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    //@Autowired
    //IclijConfig iclijConfig;
    
    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        super(myservices, services, communications, replyclass, iclijConfig, io);
    }

    public void get(Object param, Communication c) {
        log.info("param" + (String) param);
        QueueElement element = JsonUtil.convert((String) param, QueueElement.class);
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());
        IclijServiceResult r = null;
        log.debug("Cserv {}", c.getService());
        log.debug("Content {}", content);
        if (serviceMatch(ServiceConstants.SIMFILTER, c)) {
            new Sim(iclijConfig, io).method((String) content, "sim", true);
        }
        if (serviceMatch(ServiceConstants.SIMAUTO, c)) {
            new Sim(iclijConfig, io).method((String) content, "simauto", false);
        }
        if (serviceMatch(ServiceConstants.SIMRUN, c)) {
            new Sim(iclijConfig, io).method3((String) content, "simrun", false);
        }
        new QueueUtils(io.getCuratorClient()).zkUnregister((String) param);
        if (param instanceof IclijServiceParam) {
            sendReply(((IclijServiceParam) param).getWebpath(), c, r);
        }
        // element ack
    }


}

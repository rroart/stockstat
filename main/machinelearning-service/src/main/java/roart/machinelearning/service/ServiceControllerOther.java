package roart.machinelearning.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.cache.MyCache;
import roart.common.communication.model.Communication;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.ServiceConstants;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.queue.QueueElement;
import roart.common.service.ServiceParam;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.common.service.IclijServiceResult;
import roart.machinelearning.service.evolution.MachineLearningEvolutionService;
import roart.model.io.IO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        super(myservices, services, communications, replyclass, iclijConfig, io);
    }

    public void get(Object object, Communication c) {
        QueueElement element = JsonUtil.convert((String) object, QueueElement.class);
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());

        IclijServiceParam param = JsonUtil.convertnostrip(content, IclijServiceParam.class);
        IclijServiceResult r = get(param, c);
        QueueElement elementReply = new QueueElement();
        InmemoryMessage msg = inmemory.send(element.getQueue() + UUID.randomUUID(), r, null);
        elementReply.setMessage(msg);
        log.info("replyto {}", element.getQueue());
        sendReply(element.getQueue(), c, elementReply);
    }

    public IclijServiceResult get(IclijServiceParam param, Communication c) {
        IclijServiceResult result = new IclijServiceResult();
        log.info("Cserv {}", c.getService());
        if (serviceMatch(ServiceConstants.GETMCONFIG, c)) {
            try {
                result.setConfigData(iclijConfig.getConfigData());
                log.debug("Configs {}", result.getConfigData());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.GETMCONTENT, c)) {
            try {
                long[] mem0 = MemUtil.mem();
                log.info("MEM {}", MemUtil.print(mem0));
                List<String> disableList = param.getConfList();
                if (disableList == null) {
                    disableList = new ArrayList<>();
                }
                result = new MachineLearningControlService(io).getContent( disableList, param);
                if (!param.isWantMaps()) {
                    result.setMaps(null);
                }
                long[] mem1 = MemUtil.mem();
                long[] memdiff = MemUtil.diff(mem1, mem0);
                log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
                log.info("Cache {}", MyCache.getInstance().toString());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        if (serviceMatch(ServiceConstants.GETEVOLVENN, c)) {
            try {
                List<String> disableList = param.getConfList();
                if (disableList == null) {
                    disableList = new ArrayList<>();
                }
                Set<String> ids = param.getIds();
                String ml = ids.iterator().next();
                result = new MachineLearningEvolutionService(io).getEvolveML( disableList, ml, param);
                if (!param.isWantMaps()) {
                    result.setMaps(null);
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
        }
        return result;
    }
}

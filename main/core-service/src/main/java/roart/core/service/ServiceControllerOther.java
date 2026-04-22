package roart.core.service;

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
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.config.IclijConfig;
import roart.iclij.common.service.IclijServiceResult;
import roart.model.io.IO;

import java.util.*;

public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private CoreControlService getInstance(IclijServiceParam param) {
        return new CoreControlService(io);
    }

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        super(myservices, services, communications, replyclass, iclijConfig, io);
    }

    public void get(Object object, Communication c) {
        QueueElement element = JsonUtil.convert((String) object, QueueElement.class);
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String content = inmemory.read(element.getMessage());
        inmemory.delete(element.getMessage());

        IclijServiceParam param = JsonUtil.convertnostrip(content, IclijServiceParam.class);
        IclijServiceResult r = null;
        log.info("Cserv {}", c.getService());
        QueueElement elementReply = new QueueElement();
        if (serviceMatch(EurekaConstants.GETCONFIG, c)) {
            r = new IclijServiceResult();
            try {
                r.setConfigData(iclijConfig.getConfigData());
                InmemoryMessage msg = inmemory.send(EurekaConstants.GETCONFIG + UUID.randomUUID(), r, null);
                elementReply.setMessage(msg);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                r.setError(e.getMessage());
            }
        }
        if (serviceMatch(EurekaConstants.GETCONTENT, c)) {
            r = new IclijServiceResult();
            IclijServiceResult result = r;
            Map<String, Map<String, Object>> maps = null;
            if (param.isWantMaps()) {
                maps = new HashMap<>();
            }
            try {
                long[] mem0 = MemUtil.mem();
                log.info("MEM {}", MemUtil.print(mem0));
                List<String> disableList = param.getConfList();
                if (disableList == null) {
                    disableList = new ArrayList<>();
                }
                getInstance(param).getContent( new IclijConfig(param.getConfigData()), disableList, result, param);
                long[] mem1 = MemUtil.mem();
                long[] memdiff = MemUtil.diff(mem1, mem0);
                log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
                log.info("Cache {}", MyCache.getInstance().toString());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
            element.setResult(result);
        }
        log.info("replyto {}", element.getQueue());
        sendReply(element.getQueue(), c, elementReply);
    }
}

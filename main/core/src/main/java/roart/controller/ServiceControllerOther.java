package roart.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import roart.common.cache.MyCache;
import roart.common.communication.model.Communication;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.controller.ServiceControllerOtherAbstract;
import roart.common.queue.QueueElement;
import roart.common.service.ServiceParam;
import roart.common.util.MemUtil;
import roart.core.service.CoreControlService;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.config.IclijConfig;
import roart.iclij.common.service.IclijServiceResult;
import roart.model.io.IO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ServiceControllerOther extends ServiceControllerOtherAbstract {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    IclijConfig iclijConfig;

    @Autowired
    IO io;

    private CoreControlService getInstance(IclijServiceParam param) {
        return new CoreControlService(io);
    }

    public ServiceControllerOther(String myservices, String services, String communications, Class replyclass, IclijConfig iclijConfig, IO io) {
        super(myservices, services, communications, replyclass, iclijConfig, io);
    }

    public void get(Object object, Communication c) {
        QueueElement element = (QueueElement) object;
        IclijServiceParam param = element.getParam();
        IclijServiceResult r = null;
        log.info("Cserv {}", c.getService());
        if (serviceMatch(EurekaConstants.GETCONFIG, c)) {
            r = new IclijServiceResult();
            try {
                r.setConfigData(iclijConfig.getConfigData());
                log.info("configs {}", r.getConfigData());
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
                if (maps != null) {
                    //log.info("Length {}", JsonUtil.convert(maps).length());
                }
                //System.out.println(VM.current().details());
                //System.out.println(GraphLayout.parseInstance(maps).toFootprint());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                result.setError(e.getMessage());
            }
            element.setResult(result);
            sendReply(element.getQueue(), c, element);
            //return result;

        }
        /*
        if (param instanceof ServiceParam) {
            sendReply(((ServiceParam) param).getWebpath(), c, r);
        }

         */
    }
}

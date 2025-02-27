package roart.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.util.JsonUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.ControlService;

public class PipelineThreadUtils {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig iclijConfig;

    private ControlService controlService;

    public PipelineThreadUtils(IclijConfig iclijConfig, ControlService controlService) {
        super();
        this.iclijConfig = iclijConfig;
        this.controlService = controlService;
    }

    public List<String> deleteOld(CuratorFramework curatorClient, String path, String id, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        List<String> list = new ArrayList<>();
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            log.error("Empty path " + path);
            return new ArrayList<>();
        }
        List<String> children = curatorClient.getChildren().forPath(path);
        log.debug("Children {}", children.size());
        log.info("Children {}", children);
        for (String child : children) {
            List<String> children2 = curatorClient.getChildren().forPath(path + "/" + child);
            log.info("Children2" + children2);
            for (String child2 : children2) {
                Stat stat = curatorClient.checkExists().forPath(path + "/" + child + "/" + child2);
                log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
                long time = System.currentTimeMillis() - stat.getMtime();
                log.debug("Time {}", time);
                byte[] data = curatorClient.getData().forPath(path + "/" + child + "/" + child2);
                String str = new String(data);
                log.info("Element deleted " + str);
                Inmemory inmemory = controlService.getIo().getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
                //InmemoryMessage m = new InmemoryMessage(iclijConfig.getInmemoryServer(), id + "-" + child, 0);
                InmemoryMessage m = JsonUtil.convert(str, InmemoryMessage.class);
                inmemory.delete(m);
                curatorClient.delete().forPath(path + "/" + child + "/" + child2);
            }
            curatorClient.delete().forPath(path + "/" + child);
        }
        curatorClient.delete().forPath(path);
        return list;
    }

}

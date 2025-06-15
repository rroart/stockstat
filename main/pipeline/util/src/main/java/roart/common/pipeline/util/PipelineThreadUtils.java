package roart.common.pipeline.util;

import java.util.ArrayList;
import java.util.List;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.util.JsonUtil;
import roart.iclij.config.IclijConfig;

public class PipelineThreadUtils {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig iclijConfig;

    private Inmemory inmemory;

    private CuratorFramework curatorClient;

    public PipelineThreadUtils(IclijConfig iclijConfig, Inmemory inmemory, CuratorFramework curatorClient) {
        super();
        this.iclijConfig = iclijConfig;
        this.inmemory = inmemory;
        this.curatorClient = curatorClient;
    }

    public List<String> deleteOldService(CuratorFramework curatorClient, String path, String id, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        List<String> list = new ArrayList<>();
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            log.error("Empty path {}", path);
            return new ArrayList<>();
        }
        List<String> children = curatorClient.getChildren().forPath(path);
        log.debug("Children {}", children.size());
        log.info("Children {}", children);
        if (false && !children.isEmpty()) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        for (String child : children) {
            List<String> children2 = curatorClient.getChildren().forPath(path + "/" + child);
            log.info("Children2 {}", children2);
            deleteOld(curatorClient, path + "/" + child, id, deleteTime, deleteQueue, deleteInmemory);
        }
        return list;
    }
    
    public List<String> deleteOld(CuratorFramework curatorClient, String path, String id, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        List<String> list = new ArrayList<>();
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            log.error("Empty path {}", path);
            return new ArrayList<>();
        }
        List<String> children = curatorClient.getChildren().forPath(path);
        log.debug("Children {}", children.size());
        log.info("Children {}", children);
        if (false && !children.isEmpty()) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        for (String child : children) {
            Stat stat = curatorClient.checkExists().forPath(path + "/" + child);
            log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
            long time = System.currentTimeMillis() - stat.getMtime();
            log.debug("Time {}", time);
            byte[] data = curatorClient.getData().forPath(path + "/" + child);
            String str = new String(data);
            log.info("Element deleted {}", str);
            //InmemoryMessage m = new InmemoryMessage(iclijConfig.getInmemoryServer(), id + "-" + child, 0);
            InmemoryMessage m = JsonUtil.convert(str, InmemoryMessage.class);
            inmemory.delete(m);
            curatorClient.delete().forPath(path + "/" + child);
            log.info("Path deleted {}", path + "/" + child);
        }
        curatorClient.delete().forPath(path);
        log.info("Path deleted {}", path);
       return list;
    }

    public List<String> deleteOldOrig(CuratorFramework curatorClient, String path, String id, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        List<String> list = new ArrayList<>();
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            log.error("Empty path {}", path);
            return new ArrayList<>();
        }
        List<String> children = curatorClient.getChildren().forPath(path);
        log.debug("Children {}", children.size());
        log.info("Children {}", children);
        if (false && !children.isEmpty()) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        for (String child : children) {
            List<String> children2 = curatorClient.getChildren().forPath(path + "/" + child);
            log.info("Children2 {}", children2);
            for (String child2 : children2) {
                Stat stat = curatorClient.checkExists().forPath(path + "/" + child + "/" + child2);
                log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
                long time = System.currentTimeMillis() - stat.getMtime();
                log.debug("Time {}", time);
                byte[] data = curatorClient.getData().forPath(path + "/" + child + "/" + child2);
                String str = new String(data);
                log.info("Element deleted {}", str);
                //InmemoryMessage m = new InmemoryMessage(iclijConfig.getInmemoryServer(), id + "-" + child, 0);
                InmemoryMessage m = JsonUtil.convert(str, InmemoryMessage.class);
                inmemory.delete(m);
                curatorClient.delete().forPath(path + "/" + child + "/" + child2);
                log.info("Path deleted {}", path + "/" + child + "/" + child2);
            }
            curatorClient.delete().forPath(path + "/" + child);
        }
        curatorClient.delete().forPath(path);
        return list;
    }

    public void cleanPipeline(String serviceId, String id) {
        if (false) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        if (iclijConfig.wantsInmemoryPipeline()) {
            String path3 = "/" + Constants.STOCKSTAT + "/" + Constants.PIPELINE + "/" + serviceId + "/" + id;
            try {
                deleteOld(curatorClient, path3, id, 2 * 60 * 1000, false, false);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }

}

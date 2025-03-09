package roart.queue;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.apache.zookeeper.data.Stat;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.leader.MyLeader;
import roart.common.leader.impl.MyLeaderFactory;
import roart.common.util.JsonUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.ControlService;
import roart.model.io.IO;
import roart.common.queue.QueueElement;
import roart.common.queueutil.QueueUtils;

public class PipelineThread extends Thread {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final int sleepsec = 60;
    
    public static Queue queue = new ConcurrentLinkedQueue();
    
    private IclijConfig iclijConfig;

    private ControlService controlService;
    
    private IO io;
    
    public PipelineThread(IclijConfig conf, ControlService controlService, IO io) {
        super();
        this.iclijConfig = conf;
        this.controlService = controlService;
        this.io = io;
    }

    public void run() {
        CuratorFramework curatorClient = io.getCuratorClient();
        
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        // duplicated in iclijcontroller
        String path = "/" + Constants.STOCKSTAT + "/" + Constants.PIPELINE + "/" + Constants.LIVE + "/" + controlService.id;
        try {
            io.getCuratorClient().create().creatingParentsIfNeeded().forPath(path, new byte[0]);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        long lastMain = 0;
        MyLeader leader = new MyLeaderFactory().create("queue", hostname, iclijConfig, io.getCuratorClient(), null /*GetHazelcastInstance.instance(conf.getInmemoryHazelcast())*/);
        while (true) {
            // if curatorclient
            // if other queue is dead, resend
            if (true) {
                try {
                    if (curatorClient.checkExists().forPath(path) != null) {
                        touch(curatorClient, path, 2 * 60 * 1000, false, false);
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                    break;
                }

                
            }
            try {
                TimeUnit.SECONDS.sleep(sleepsec);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    private List<String> touch(CuratorFramework curatorClient, String path, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        List<String> list = new ArrayList<>();
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            //continue;
        }
        curatorClient.setData().forPath(path, new byte[0]);
        if (true) return null;
        List<String> children = curatorClient.getChildren().forPath(path);
        log.debug("Children {}", children.size());
        log.info("Children" + children);
        for (String child : children) {
            curatorClient.setData().forPath(path + "/" + child, new byte[0]);
        }
        return list;
    }
}

package roart.queue;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
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
import roart.common.queue.QueueElement;
import roart.common.queueutil.QueueUtils;

public class QueueThread extends Thread {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final int sleepsec = 5;
    
    public static Queue queue = new ConcurrentLinkedQueue();
    
    private IclijConfig iclijConfig;

    private ControlService controlService;
    
    private IO io;
    
    public QueueThread(IclijConfig conf, ControlService controlService, IO io) {
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
        long lastMain = 0;
        MyLeader leader = new MyLeaderFactory().create("queue", hostname, iclijConfig, io.getCuratorClient(), null /*GetHazelcastInstance.instance(conf.getInmemoryHazelcast())*/);
        while (true) {
            // if curatorclient
            // if other queue is dead, resend
            boolean leading = leader.await(1, TimeUnit.SECONDS);
            if (!leading) {
                log.info("I am not queue leader");
            } else {
                log.info("I am queue leader");
                try {
                    getOldRequeueAndDelete(curatorClient, 2 * 60 * 1000);
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                    break;
                }

                
            }
            log.info("Leader queue status: {}", leader.isLeader());

            try {
                TimeUnit.SECONDS.sleep(sleepsec);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public void getOldRequeueAndDelete(CuratorFramework curatorClient, int deleteTime) throws Exception {
        // get a path like /stockstat/queueNNN/live
        String path = QueueUtils.getLivePath();
        if (curatorClient.checkExists().forPath(path) != null) {
            // get elems two levels down like,
            // /stockstat/queueNNN/live/HOST/SERVICE
            List<String> elems = getOld(curatorClient, path, deleteTime, false, false);
            for (String elem : elems) {
                String path3 = QueueUtils.getQueuePath(elem);
                requeueOld(curatorClient, path3, deleteTime, false, false);
                String path2 = QueueUtils.getLivePath();
                log.info("Deleting {}", path2 + "/" + elem);
                curatorClient.delete().forPath(path2 + "/" + elem);
            }
        }
    }

    private List<String> getOld(CuratorFramework curatorClient, String path, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        List<String> list = new ArrayList<>();
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            //continue;
        }
        List<String> children = curatorClient.getChildren().forPath(path);
        log.info("Children {}", children.size());
        for (String child : children) {
            List<String> children2 = curatorClient.getChildren().forPath(path + "/" + child);
            log.info("Children2 {}", children2);
            for (String child2 : children2) {
                Stat stat = curatorClient.checkExists().forPath(path + "/" + child + "/" + child2);
                log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
                long time = System.currentTimeMillis() - stat.getMtime();
                log.debug("Time {} {}", time, deleteTime);
                if (time > deleteTime) {
                    list.add(child + "/" + child2);
                    log.error("Service died {}", child + "/" + child2);
                }
            }
        }
        return list;
    }

    private List<String> requeueOld(CuratorFramework curatorClient, String path, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
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
            Stat stat = curatorClient.checkExists().forPath(path + "/" + child);
            log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
            long time = System.currentTimeMillis() - stat.getMtime();
            log.debug("Time {}", time);
            byte[] data = curatorClient.getData().forPath(path + "/" + child);
            String str = new String(data);
            QueueElement element = JsonUtil.convert(str, QueueElement.class);
            controlService.send(element.getQueue(), element, iclijConfig);
            log.info("Element requeued {} {}", element.getQueue(), element.getId());
            curatorClient.delete().forPath(path + "/" + child);
            
        }
        return list;
    }
}

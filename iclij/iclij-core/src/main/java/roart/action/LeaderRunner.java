package roart.action;

import java.util.concurrent.TimeUnit;
import roart.common.constants.Constants;
import java.util.Set;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.List;
import org.apache.zookeeper.data.Stat;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roart.common.leader.MyLeader;
import roart.common.leader.impl.MyLeaderFactory;
import roart.common.webflux.WebFluxUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.ControlService;

public class LeaderRunner implements Runnable {
    static Logger log = LoggerFactory.getLogger(LeaderRunner.class);

    private IclijConfig iclijConfig;

    private ControlService controlService;
    
    public LeaderRunner(IclijConfig conf, ControlService controlService) {
        super();
        this.iclijConfig = conf;
        this.controlService = controlService;
    }

    @SuppressWarnings("squid:S2189")
    public void run() {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        MyLeader leader = new MyLeaderFactory().create(hostname, iclijConfig, ControlService.curatorClient, null /*GetHazelcastInstance.instance(conf.getInmemoryHazelcast())*/);
        while (true) {
            boolean leading = leader.await(1, TimeUnit.SECONDS);
            if (!leading) {
                log.info("I am not leader");
            } else {
                log.info("I am leader");
                CuratorFramework curatorClient = controlService.curatorClient;
                while (true) {
                    try {
                        String path = "/" + Constants.STOCKSTAT + "/" + Constants.DB;
                        deleteOld(curatorClient, path, 15 * 60 * 1000, false, false);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                        break;
                    }

                    try {
                        String path = "/" + Constants.STOCKSTAT + "/" + Constants.DATA;
                        deleteOld(curatorClient, path, 20 * 60 * 1000, true, true);
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                        break;
                    }

                }
            }
            log.info("Leader status: {}", leader.isLeader());
            try {
                TimeUnit.SECONDS.sleep(60 /* 3600 */);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
    }

    private void deleteOld(CuratorFramework curatorClient, String path, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            //continue;
        }
        List<String> children = curatorClient.getChildren().forPath(path);
        log.debug("Children {}", children.size());
        for (String child : children) {
            Stat stat = curatorClient.checkExists().forPath(path + "/" + child);
            log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
            long time = System.currentTimeMillis() - stat.getMtime();
            log.debug("Time {}", time);
            if (stat.getNumChildren() > 0) {
                deleteOld(curatorClient, path + "/" + child, deleteTime, deleteQueue, deleteInmemory);
                continue;
            }
            if (time > deleteTime) {
                curatorClient.delete().forPath(path + "/" + child);                                
                log.info("Delete old lock {}", child);
                if (deleteQueue) {
                }
                if (deleteInmemory) {
                    
                }
            }
        }
    }
}

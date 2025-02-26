package roart.action;

import java.util.concurrent.TimeUnit;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;

import java.util.Set;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import org.apache.zookeeper.data.Stat;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import roart.common.leader.MyLeader;
import roart.common.leader.impl.MyLeaderFactory;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.ControlService;
import roart.model.io.IO;
import roart.util.PipelineThreadUtils;

public class LeaderRunner implements Runnable {
    static Logger log = LoggerFactory.getLogger(LeaderRunner.class);
    
    public static boolean commonleader = false;
    
    private IclijConfig iclijConfig;

    private ControlService controlService;
    
    private IO io;
    
    public LeaderRunner(IclijConfig conf, ControlService controlService, IO io) {
        super();
        this.iclijConfig = conf;
        this.controlService = controlService;
        this.io = io;
    }

    @SuppressWarnings("squid:S2189")
    public void run() {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        long lastMain = 0;
        MyLeader leader = new MyLeaderFactory().create("leader", hostname, iclijConfig, io.getCuratorClient(), null /*GetHazelcastInstance.instance(conf.getInmemoryHazelcast())*/);
        while (true) {
            boolean leading = leader.await(1, TimeUnit.SECONDS);
            commonleader = leading;
            if (!leading) {
                log.info("I am not leader");
            } else {
                log.info("I am leader");
                CuratorFramework curatorClient = io.getCuratorClient();
                Action action = new MainAction(iclijConfig, io);
                while (true) {
                    try {
                        // TODO pipeline
                        String path2 = "/" + Constants.STOCKSTAT + "/" + "pipeline" + "/" + "live";
                        List<String> elems = getOld(curatorClient, path2, 2 * 60 * 1000, false, false);
                        for (String elem : elems) {
                            String path3 = "/" + Constants.STOCKSTAT + "/" + "pipeline" + "/" + elem;
                            new PipelineThreadUtils(iclijConfig, controlService).deleteOld(curatorClient, path3, elem, 2 * 60 * 1000, false, false);
                            log.info("Deleting " + path2 + "/" + elem);
                            curatorClient.delete().forPath(path2 + "/" + elem);
                        }

                        // TODO only old borrowed leftover
                        String path = "/" + Constants.STOCKSTAT + "/" + Constants.DB;
                        if (curatorClient.checkExists().forPath(path) != null) {
                            deleteOld(curatorClient, path, 15 * 60 * 1000, false, false);
                        }
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                        break;
                    }

                    try {
                        // TODO only old borrowed leftover
                        String path = "/" + Constants.STOCKSTAT + "/" + Constants.DATA;
                        if (curatorClient.checkExists().forPath(path) != null) {
                            deleteOld(curatorClient, path, 20 * 60 * 1000, true, true);
                        }
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                        break;
                    }

                    long time = System.currentTimeMillis();
                    if (time - lastMain > 3600 * 1000) {
                        if (MainAction.wantsGoals(iclijConfig)) {
                            long time0 = System.currentTimeMillis();
                            try {
                                action.goal(null, null, null, iclijConfig, io);
                            } catch (InterruptedException e) {
                                log.error(Constants.EXCEPTION, e);
                            } catch (NullPointerException e) {
                                // in case core not ready?
                                log.error(Constants.EXCEPTION, e);
                            }
                            long newLastMain = System.currentTimeMillis();
                            log.info("Goals time {}", (newLastMain - time0) / 1000);
                            lastMain = newLastMain;
                        }
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
    
    private List<String> getOld(CuratorFramework curatorClient, String path, int deleteTime, boolean deleteQueue, boolean deleteInmemory) throws Exception {
        List<String> list = new ArrayList<>();
        Stat b = curatorClient.checkExists().forPath(path);
        if (b == null) {
            return List.of();
        }
        List<String> children = curatorClient.getChildren().forPath(path);
        log.debug("Children {}", children.size());
        log.info("Children" + children);
        for (String child : children) {
            Stat stat = curatorClient.checkExists().forPath(path + "/" + child);
            log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
            long time = System.currentTimeMillis() - stat.getMtime();
            log.info("Time {} {}", time, deleteTime);
            if (time > deleteTime) {
                list.add(child);
                log.error("Service died " + child);
            }
        }
        return list;
    }

}

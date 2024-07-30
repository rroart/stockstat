package roart.common.queueutil;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;

import org.apache.curator.framework.CuratorFramework;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;

public class QueueLiveThread extends Thread {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String name;
    
    private CuratorFramework curatorClient;

    public QueueLiveThread(String name, CuratorFramework curatorClient) {
        super();
        this.name = name;
        this.curatorClient = curatorClient;
    }
    
    public void run() {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        String path = QueueUtils.getLivePath(hostname, name);
        String str = path;
        while (true) {
            try {
                if (curatorClient.checkExists().forPath(str) == null) {
                    curatorClient.create().creatingParentsIfNeeded().forPath(str);
                }
                curatorClient.setData().forPath(str);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            try {
                TimeUnit.SECONDS.sleep(60 /* 3600 */);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
            }

        }
    }
}

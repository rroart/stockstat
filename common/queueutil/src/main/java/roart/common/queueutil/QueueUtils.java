package roart.common.queueutil;

import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;

import java.net.InetAddress;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;

public class QueueUtils {
    private Logger log = LoggerFactory.getLogger(this.getClass());
   
    private CuratorFramework curatorClient;

    public QueueUtils(CuratorFramework curatorClient) {
        this.curatorClient = curatorClient;
    }
    
    public static QueueElement get(String str) {
        return JsonUtil.convert(str, QueueElement.class);
    }
    
    public boolean zkRegister(String str2) {
        QueueElement elem = get(str2);
        String path = getPath(elem);
        String str = path;
        try {
            if (curatorClient.checkExists().forPath(str) == null) {
                curatorClient.create().creatingParentsIfNeeded().forPath(str);
            }
            curatorClient.setData().forPath(str, str2.getBytes());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return false;
        }

        return true;
    }

    public boolean zkUnregister(String str2) {
        QueueElement elem = get(str2);
        String path = getPath(elem);
        String str = path;
        try {
            if (curatorClient.checkExists().forPath(str) == null) {
                curatorClient.delete().forPath(str);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return false;
        }

        return true;
    }

    private String getPath(QueueElement elem) {
        String hostname = "localhost";
        try {
            hostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        String path = getQueuePath(hostname + "/" + elem.getOpid() + "/" + elem.getId());
        log.info("Path {}", path);
        return path;
    }
    
    public static String getLivePath() {
        String path = "/" + Constants.STOCKSTAT + "/" + "queue" + "/" + "live";
        return path;
    }
    
    public static String getQueuePath(String path) {
        return "/" + Constants.STOCKSTAT + "/" + "queue" + "/" + "run" + "/" + path;
    }
    
    public static String getLivePath(String hostname, String name) {
        String path = "/" + Constants.STOCKSTAT + "/" + "queue" + "/" + "live" + "/" + hostname + "/" + name;
        return path;
    }
    
    @Deprecated
    public static String getPath2(String str, String hostname) {
        QueueElement element = get(str);
        String path = "/" + Constants.STOCKSTAT + "/" + "queue" + "/" + hostname + "/" + element.getId();
        return path;
    }

    @Deprecated
    public static String getPath3(String id, String hostname) {
        //QueueElement element = get(str);
        String path = "/" + Constants.STOCKSTAT + "/" + "queue" + "/" + hostname + "/" + id;
        return path;
    }
}

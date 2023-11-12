package roart.filesystem;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.FileSystemConstants;
import roart.common.filesystem.MyFile;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.FileObject;
import roart.common.model.Location;
import roart.common.util.FsUtil;

import java.util.Map.Entry;

public class FileSystemDao {

    private static Logger log = LoggerFactory.getLogger(FileSystemDao.class);

    private static FileSystemAccess filesystemJpa = null;

    //private static Map<String, MyServer> myservers = new HashMap<>();

    private static IclijConfig conf;

    private static CuratorFramework curatorClient;
    
    public FileSystemDao(IclijConfig conf, CuratorFramework curatorClient) {
	this.conf = conf;
	this.curatorClient = curatorClient;
    }
    
    public static void instance(String type) {
    }

    public static List<FileObject> listFiles(FileObject f) {
        return getFileSystemAccess(f).listFiles(f);
    }

    public static List<MyFile> listFilesFull(FileObject f) {
        return getFileSystemAccess(f).listFilesFull(f);
    }

    public static boolean exists(FileObject f) {
        return getFileSystemAccess(f).exists(f);
    }

    public static boolean isDirectory(FileObject f) {
        return getFileSystemAccess(f).isDirectory(f);
    }

    public static String getAbsolutePath(FileObject f) {
        return getFileSystemAccess(f).getAbsolutePath(f);
    }
    
    public static InputStream getInputStream(FileObject f) {
        return getFileSystemAccess(f).getInputStream(f);
    }

    public static Map<FileObject, MyFile> getWithInputStream(Set<FileObject> filenames) {
        FileObject f = filenames.iterator().next();
        Map<String, MyFile> map = getFileSystemAccess(f).getWithInputStream(filenames);
        Map<FileObject, MyFile> retMap = new HashMap<>();
        for (Entry<String, MyFile> entry : map.entrySet()) {
            retMap.put(new FileObject(filenames.iterator().next().location, entry.getKey()), entry.getValue());
        }
        return retMap;
    }

    public static Map<FileObject, MyFile> getWithoutInputStream(Set<FileObject> filenames) {
        FileObject f = filenames.iterator().next();
        Map<String, MyFile> map = getFileSystemAccess(f).getWithoutInputStream(filenames);
        Map<FileObject, MyFile> retMap = new HashMap<>();
        for (Entry<String, MyFile> entry : map.entrySet()) {
            retMap.put(new FileObject(filenames.iterator().next().location, entry.getKey()), entry.getValue());
        }
        return retMap;
    }

    public static FileObject get(FileObject fo) {
        return getFileSystemAccess(fo).get(fo);
    }

    public static FileObject getParent(FileObject f) {
        return getFileSystemAccess(f).getParent(f);
    }

    public static InmemoryMessage readFile(FileObject f) {
        Set<FileObject> filenames = new HashSet<>();
        filenames.add(f);
        Map<FileObject, InmemoryMessage> map = readFile(filenames);
        return map.get(f);
    }

    public static Map<FileObject, InmemoryMessage> readFile(Set<FileObject> filenames) {
        FileObject f = filenames.iterator().next();
        Map<String, InmemoryMessage> map = getFileSystemAccess(f).readFile(filenames);
        Map<FileObject, InmemoryMessage> retMap = new HashMap<>();
        for (Entry<String, InmemoryMessage> entry : map.entrySet()) {
            retMap.put(new FileObject(filenames.iterator().next().location, entry.getKey()), entry.getValue());
        }
        return retMap;
    }

    public static String writeFile(String node, String path, String filename, String content) {
        if (filename == null) {
            Path mypath = Paths.get("" + System.currentTimeMillis() + "_" + UUID.randomUUID() + ".txt");
            filename = mypath.getFileName().toString();
        }
        Inmemory inmemory = InmemoryFactory.get(conf.getInmemoryServer(), conf.getInmemoryHazelcast(), conf.getInmemoryRedis());
        InmemoryMessage msg = inmemory.send(EurekaConstants.WRITEFILE + "_" + System.currentTimeMillis() + "_" + UUID.randomUUID(), content);
        FileObject f = new FileObject(FsUtil.getLocation(node), path + "/" + filename);
        writeFile(f, msg);
        return filename;
    }
    
    public static boolean writeFile(FileObject f, InmemoryMessage content) {
        Set<FileObject> filenames = new HashSet<>();
        filenames.add(f);
        boolean b = getFileSystemAccess(f).writeFile(f, content);
        return b;
    }

    public static Map<FileObject, String> getMd5(Set<FileObject> filenames) {
        FileObject f = filenames.iterator().next();
        Map<String, String> map = getFileSystemAccess(f).getMd5(filenames);
        Map<FileObject, String> retMap = new HashMap<>();
        for (Entry<String, String> entry : map.entrySet()) {
            retMap.put(new FileObject(filenames.iterator().next().location, entry.getKey()), entry.getValue());
        }
        return retMap;
    }

    // TODO make this OO
    private static FileSystemAccess getFileSystemAccess(FileObject f) {
        if (f == null) {
            log.error("f null");
            return new LocalFileSystemAccess(conf.getConfigData());
        }
        /*
        if (f.fs == null) {
            log.error("f.fs null " + f.object);
            return new LocalFileSystemAccess();
        }
    	if (f.fs.equals("HDFS")) {
    		return new HDFSAccess();
    	} else if (f.fs.equals("Swift")) {
     		return new SwiftAccess();
    	} else {
    		return new LocalFileSystemAccess();
    	}
         */
        return getFileSystemAccess(f.location, f.object);
    }
    
    private static FileSystemAccess getFileSystemAccess(Location fs, String path) {
        Location fs2 = new Location(fs.nodename, fs.fs, fs.extra);
        if (fs2.fs == null || fs2.fs.isEmpty()) {
            fs2.fs = FileSystemConstants.LOCALTYPE;
        }
        String url = getUrl(curatorClient, fs2, path, "");
        if (url == null) {
            log.error("URL null for {} {}", fs, path);
            return null;
        }
        FileSystemAccess access = new FileSystemAccess(conf.getConfigData());
        access.constructor("http://" + url + "/");
        return access;
    }

    static String getUrl(CuratorFramework curatorClient, Location fs, String path, String s) {
        // //fstype/path
        // node and openshift?
        // zk nodename type path
        //ControlServer.z
        String url = null;
        try {
            String str = "/" + Constants.STOCKSTAT + "/" + Constants.FS + stringOrNull(fs.nodename) + "/" + fs.fs + stringOrNull(fs.extra) + s;
            String zPath = "/" + Constants.STOCKSTAT + "/" + Constants.FS + stringOrNull(fs.nodename) + "/" + fs.fs + stringOrNull(fs.extra) + s;
            log.debug("Path {}", zPath);
            Stat b = curatorClient.checkExists().forPath(zPath);
            if (b == null) {
                return null;
            }
            List<String> children = curatorClient.getChildren().forPath(zPath);
            log.debug("Children {}", children.size());
            if (children.isEmpty()) {
                Stat stat = curatorClient.checkExists().forPath(zPath);
                log.debug("Time {} {}", System.currentTimeMillis(), stat.getMtime());;
                long time = System.currentTimeMillis() - stat.getMtime();
                log.debug("Time {}", time);
                if (time < 20000) {
                    return new String(curatorClient.getData().forPath(zPath));
                } else {
                    log.error("Timeout");
                    return null;
                }
            }
            for (String child : children) {
                log.debug("Child {}", child);
                String newPath = s + "/" + child;
                log.debug("Compare {} {}", path, newPath);
                if (path.startsWith(newPath + "/")) {
                    return getUrl(curatorClient, fs, path, newPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(Constants.EXCEPTION, e);
        }
        return url;
    }

    private static String stringOrNull(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        } else {
            return "/" + string;
        }
    }
}

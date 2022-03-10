package roart.filesystem;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.constants.FileSystemConstants;
import roart.common.filesystem.FileSystemBooleanResult;
import roart.common.filesystem.FileSystemByteResult;
import roart.common.filesystem.FileSystemConstructorParam;
import roart.common.filesystem.FileSystemConstructorResult;
import roart.common.filesystem.FileSystemFileObjectParam;
import roart.common.filesystem.FileSystemFileObjectResult;
import roart.common.filesystem.FileSystemMessageResult;
import roart.common.filesystem.FileSystemMyFileResult;
import roart.common.filesystem.FileSystemPathParam;
import roart.common.filesystem.FileSystemPathResult;
import roart.common.filesystem.FileSystemStringResult;
import roart.common.model.FileObject;
import roart.common.util.FsUtil;

import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@SpringBootApplication
public abstract class FileSystemAbstractController implements CommandLineRunner {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static Map<String, FileSystemOperations> operationMap = new HashMap();

    protected abstract FileSystemOperations createOperations(String nodename, String configid, MyMyConfig nodeConf);

    private FileSystemOperations getOperations(String nodename, String configid, MyMyConfig nodeConf) {
        FileSystemOperations operations = operationMap.get(configid);
        if (operations == null) {
            operations = createOperations(nodename, configid, nodeConf);
            operationMap.put(configid, operations);
        }
        return operations;
    }

    @RequestMapping(value = "/" + EurekaConstants.CONSTRUCTOR,
            method = RequestMethod.POST)
    public FileSystemConstructorResult processConstructor(@RequestBody FileSystemConstructorParam param)
            throws Exception {
        String error = null;
        try {
            FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        } catch (Exception e) {
            log.error(roart.common.constants.Constants.EXCEPTION, e);
            error = e.getMessage();
        }
        FileSystemConstructorResult result = new FileSystemConstructorResult();
        result.error = error;
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.DESTRUCTOR,
            method = RequestMethod.POST)
    public FileSystemConstructorResult processDestructor(@RequestBody FileSystemConstructorParam param)
            throws Exception {
        FileSystemOperations operations = operationMap.remove(param.configid);
        String error = null;
        if (operations != null) {
            try {
                operations.destroy();
            } catch (Exception e) {
                log.error(roart.common.constants.Constants.EXCEPTION, e);
                error = e.getMessage();
            }
        } else {
            error = "did not exist";
        }
        FileSystemConstructorResult result = new FileSystemConstructorResult();
        result.error = error;
        return result;
    }

    @RequestMapping(value = "/" + EurekaConstants.LISTFILES,
            method = RequestMethod.POST)
    public FileSystemFileObjectResult processListFiles(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemFileObjectResult ret = operations.listFiles(param);
        return ret;
    }

    @PostMapping(value = "/" + EurekaConstants.LISTFILESFULL)
    public FileSystemMyFileResult processListFilesFull(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemMyFileResult ret = operations.listFilesFull(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.EXIST,
            method = RequestMethod.POST)
    public FileSystemBooleanResult processExist(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemBooleanResult ret = operations.exists(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETABSOLUTEPATH,
            method = RequestMethod.POST)
    public FileSystemPathResult processGetAbsolutePath(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemPathResult ret = operations.getAbsolutePath(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.ISDIRECTORY,
            method = RequestMethod.POST)
    public FileSystemBooleanResult processIsDirectory(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemBooleanResult ret = operations.isDirectory(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETINPUTSTREAM,
            method = RequestMethod.POST)
    public FileSystemByteResult processGetInputStream(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemByteResult ret = operations.getInputStream(param);
        return ret;
    }

    @PostMapping(value = "/" + EurekaConstants.GETWITHINPUTSTREAM)
    public FileSystemMyFileResult processGetWithInputStream(@RequestBody FileSystemPathParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemMyFileResult ret = operations.getWithInputStream(param, true);
        return ret;
    }

    @PostMapping(value = "/" + EurekaConstants.GETWITHOUTINPUTSTREAM)
    public FileSystemMyFileResult processGetWithoutInputStream(@RequestBody FileSystemPathParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemMyFileResult ret = operations.getWithInputStream(param, false);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETPARENT,
            method = RequestMethod.POST)
    public FileSystemFileObjectResult processGetParent(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemFileObjectResult ret = operations.getParent(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.GET,
            method = RequestMethod.POST)
    public FileSystemFileObjectResult processGet(@RequestBody FileSystemPathParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemFileObjectResult ret = operations.get(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.READFILE,
            method = RequestMethod.POST)
    public FileSystemMessageResult processReadFile(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemMessageResult ret = operations.readFile(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.WRITEFILE,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            method = RequestMethod.POST)
    public FileSystemMessageResult processWriteFile(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemMessageResult ret = operations.writeFile(param);
        return ret;
    }

    @RequestMapping(value = "/" + EurekaConstants.GETMD5,
            method = RequestMethod.POST)
    public FileSystemStringResult processGetMd5(@RequestBody FileSystemFileObjectParam param)
            throws Exception {
        FileSystemOperations operations = getOperations(param.nodename, param.configid, param.conf);
        FileSystemStringResult ret = operations.getMd5(param);
        return ret;
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(FileSystemAbstractController.class, args);
    }

    @Autowired(required=true)
    MyListener aListener;

    @Override
    public void run(String... args) throws Exception {
        /*
	        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
                //context.scan("com.journaldev.spring");
                context.refresh();

                MyListener listener = (MyListener) context.getBean("listener");
                context.close();
         */
        //Thread.sleep(10000);
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);     

        String zookeeperConnectionString = System.getProperty("ZOO");
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
        curatorClient.start();
        String nodename = System.getProperty("NODE");
        String ip = System.getProperty("IP");
        String fs = System.getProperty("FS");
        String path = System.getProperty("PATH");
        log.info("Using {} {} {}", ip, fs, path);
        String[] paths = path.split(",");
        //int port = new MyListener().getPort();
        int port = aListener.getPort();
        String address = InetAddress.getLocalHost().getHostAddress();
        if (ip != null) {
            address = ip;
        }
        String whereami = address + ":" + port;
        System.out.println("Whereami " + whereami);
        log.info("Whereami {}", whereami);
        byte[] bytes = whereami.getBytes();
        for (String aPath : paths) {
            FileObject fo = FsUtil.getFileObject(aPath);
            if (fo.location.fs == null || fo.location.fs.isEmpty()) {
                fo.location.fs = FileSystemConstants.LOCALTYPE;
            }
            String str = "/stockstat/fs" + stringOrNull(fo.location.nodename) + "/" + fo.location.fs + stringOrNull(fo.location.extra) + fo.object;
            if (str.endsWith("/")) {
                str = str.substring(0, str.length() - 1);
            }
            if (curatorClient.checkExists().forPath(str) != null) {
                curatorClient.delete().deletingChildrenIfNeeded().forPath(str);
            }
            curatorClient.create().creatingParentsIfNeeded().forPath(str, bytes);
        }
        while (true) {
            Thread.sleep(10000);
            for (String aPath : paths) {
                FileObject fo = FsUtil.getFileObject(aPath);
                if (fo.location.fs == null || fo.location.fs.isEmpty()) {
                    fo.location.fs = FileSystemConstants.LOCALTYPE;
                }
                String str = "/" + Constants.STOCKSTAT + "/" + Constants.FS + stringOrNull(fo.location.nodename) + "/" + fo.location.fs + stringOrNull(fo.location.extra) + fo.object;
                if (str.endsWith("/")) {
                    str = str.substring(0, str.length() - 1);
                }
                curatorClient.setData().forPath(str, bytes);
            }
        }
    }

    private static String stringOrNull(String string) {
        if (string == null || string.isEmpty()) {
            return "";
        } else {
            return "/" + string;
        }
    }
}

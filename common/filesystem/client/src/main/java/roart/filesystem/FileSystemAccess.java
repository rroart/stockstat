package roart.filesystem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roart.iclij.config.IclijConfig;
import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;
import roart.common.filesystem.FileSystemBooleanResult;
import roart.common.filesystem.FileSystemByteResult;
import roart.common.filesystem.FileSystemConstructorParam;
import roart.common.filesystem.FileSystemConstructorResult;
import roart.common.filesystem.FileSystemFileObjectParam;
import roart.common.filesystem.FileSystemFileObjectResult;
import roart.common.filesystem.FileSystemMessageResult;
import roart.common.filesystem.MyFile;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.filesystem.FileSystemMyFileResult;
import roart.common.filesystem.FileSystemPathParam;
import roart.common.filesystem.FileSystemPathResult;
import roart.common.filesystem.FileSystemStringResult;
import roart.common.model.FileObject;
import roart.common.webflux.WebFluxUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemAccess {

    private String url;

    private ConfigData configData;
    
    private WebFluxUtil webFluxUtil = new WebFluxUtil();
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public FileSystemAccess(ConfigData conf) {
	this.configData = conf;
    }
    
    public String getAppName() { return null; }

    public String constructor(String url) {
        this.url = url;
        FileSystemConstructorParam param = new FileSystemConstructorParam();
        param.configid = "myid";
        param.configData = configData;
        //FileSystemConstructorResult result = webFluxUtil.sendMe(FileSystemConstructorResult.class, param, getAppName(), EurekaConstants.CONSTRUCTOR);
        FileSystemConstructorResult result = webFluxUtil.sendMe(FileSystemConstructorResult.class, url, param, EurekaConstants.CONSTRUCTOR);
        return result.error;
    }

    public String destructor() {
        FileSystemConstructorParam param = new FileSystemConstructorParam();
        param.configid = "myid";
        param.configData = configData;
        FileSystemConstructorResult result = webFluxUtil.sendMe(FileSystemConstructorResult.class, url, param, EurekaConstants.DESTRUCTOR);
        return result.error;
    }
    public List<FileObject> listFiles(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fo = f;
        FileSystemFileObjectResult result = webFluxUtil.sendMe(FileSystemFileObjectResult.class, url, param, EurekaConstants.LISTFILES);
        return Arrays.asList(result.getFileObject());

    }

    public List<MyFile> listFilesFull(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fo = f;
        FileSystemMyFileResult result = webFluxUtil.sendMe(FileSystemMyFileResult.class, url, param, EurekaConstants.LISTFILESFULL);
        return new ArrayList<>(result.map.values());

    }

    public boolean exists(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fo = f;
        FileSystemBooleanResult result = webFluxUtil.sendMe(FileSystemBooleanResult.class, url, param, EurekaConstants.EXIST);
        return result.bool;

    }

    public String getAbsolutePath(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fo = f;
        FileSystemPathResult result = webFluxUtil.sendMe(FileSystemPathResult.class, url, param, EurekaConstants.GETABSOLUTEPATH);
        return result.getPath();

    }

    public boolean isDirectory(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fo = f;
        FileSystemBooleanResult result = webFluxUtil.sendMe(FileSystemBooleanResult.class, url, param, EurekaConstants.ISDIRECTORY);
        return result.bool;

    }

    public InputStream getInputStream(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fo = f;
        FileSystemByteResult result = webFluxUtil.sendMe(FileSystemByteResult.class, url, param, EurekaConstants.GETINPUTSTREAM);
        return new ByteArrayInputStream(result.bytes);

    }

    public Map<String, MyFile> getWithInputStream(Set<FileObject> filenames) {
        FileSystemPathParam param = new FileSystemPathParam();
        param.configid = "myid";
        param.configData = configData;
        param.paths = filenames;
        FileSystemMyFileResult result = webFluxUtil.sendMe(FileSystemMyFileResult.class, url, param, EurekaConstants.GETWITHINPUTSTREAM);
        return result.map;

    }

    public Map<String, MyFile> getWithoutInputStream(Set<FileObject> filenames) {
        FileSystemPathParam param = new FileSystemPathParam();
        param.configid = "myid";
        param.configData = configData;
        param.paths = filenames;
        FileSystemMyFileResult result = webFluxUtil.sendMe(FileSystemMyFileResult.class, url, param, EurekaConstants.GETWITHOUTINPUTSTREAM);
        return result.map;

    }

    public FileObject getParent(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fo = f;
        FileSystemFileObjectResult result = webFluxUtil.sendMe(FileSystemFileObjectResult.class, url, param, EurekaConstants.GETPARENT);
        return result.getFileObject()[0];

    }

    public FileObject get(FileObject fo) {
        FileSystemPathParam param = new FileSystemPathParam();
        param.configid = "myid";
        param.configData = configData;
        param.path = fo;
        FileSystemFileObjectResult result = webFluxUtil.sendMe(FileSystemFileObjectResult.class, url, param, EurekaConstants.GET);
        return result.getFileObject()[0];

    }

    @Deprecated
    public String getLocalFilesystemFile(FileObject fo) {
        FileObject file = FileSystemDao.get(fo);  
        String fn = FileSystemDao.getAbsolutePath(file);
        // TODO
        if (fn.charAt(4) == ':') {
            fn = fn.substring(5);
        }
        return fn;
    }

    public Map<String, InmemoryMessage> readFile(Set<FileObject> f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fos = f;
        FileSystemMessageResult result = webFluxUtil.sendMe(FileSystemMessageResult.class, url, param, EurekaConstants.READFILE);
        return result.message;
    }

    public boolean writeFile(FileObject f, InmemoryMessage content) {
        Map<String, InmemoryMessage> map = new HashMap<>();
        map.put(f.toString(), content);
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.map = map;
        FileSystemMessageResult result = webFluxUtil.sendMe(FileSystemMessageResult.class, url, param, EurekaConstants.WRITEFILE);
        return true;
    }

    public Map<String, String> getMd5(Set<FileObject> f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.configData = configData;
        param.fos = f;
        FileSystemStringResult result = webFluxUtil.sendMe(FileSystemStringResult.class, url, param, EurekaConstants.GETMD5);
        return result.map;
    }

}

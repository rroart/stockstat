package roart.filesystem;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import roart.common.config.MyMyConfig;
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

    private MyMyConfig conf;
    
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public FileSystemAccess(MyMyConfig conf) {
	this.conf = conf;
    }
    
    public String getAppName() { return null; }

    public String constructor(String url) {
        this.url = url;
        FileSystemConstructorParam param = new FileSystemConstructorParam();
        param.configid = "myid";
        param.conf = conf;
        //FileSystemConstructorResult result = WebFluxUtil.sendMe(FileSystemConstructorResult.class, param, getAppName(), EurekaConstants.CONSTRUCTOR);
        FileSystemConstructorResult result = WebFluxUtil.sendMe(FileSystemConstructorResult.class, url, param, EurekaConstants.CONSTRUCTOR);
        return result.error;
    }

    public String destructor() {
        FileSystemConstructorParam param = new FileSystemConstructorParam();
        param.configid = "myid";
        param.conf = conf;
        FileSystemConstructorResult result = WebFluxUtil.sendMe(FileSystemConstructorResult.class, url, param, EurekaConstants.DESTRUCTOR);
        return result.error;
    }
    public List<FileObject> listFiles(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fo = f;
        FileSystemFileObjectResult result = WebFluxUtil.sendMe(FileSystemFileObjectResult.class, url, param, EurekaConstants.LISTFILES);
        return Arrays.asList(result.getFileObject());

    }

    public List<MyFile> listFilesFull(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fo = f;
        FileSystemMyFileResult result = WebFluxUtil.sendMe(FileSystemMyFileResult.class, url, param, EurekaConstants.LISTFILESFULL);
        return new ArrayList<>(result.map.values());

    }

    public boolean exists(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fo = f;
        FileSystemBooleanResult result = WebFluxUtil.sendMe(FileSystemBooleanResult.class, url, param, EurekaConstants.EXIST);
        return result.bool;

    }

    public String getAbsolutePath(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fo = f;
        FileSystemPathResult result = WebFluxUtil.sendMe(FileSystemPathResult.class, url, param, EurekaConstants.GETABSOLUTEPATH);
        return result.getPath();

    }

    public boolean isDirectory(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fo = f;
        FileSystemBooleanResult result = WebFluxUtil.sendMe(FileSystemBooleanResult.class, url, param, EurekaConstants.ISDIRECTORY);
        return result.bool;

    }

    public InputStream getInputStream(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fo = f;
        FileSystemByteResult result = WebFluxUtil.sendMe(FileSystemByteResult.class, url, param, EurekaConstants.GETINPUTSTREAM);
        return new ByteArrayInputStream(result.bytes);

    }

    public Map<String, MyFile> getWithInputStream(Set<FileObject> filenames) {
        FileSystemPathParam param = new FileSystemPathParam();
        param.configid = "myid";
        param.conf = conf;
        param.paths = filenames;
        FileSystemMyFileResult result = WebFluxUtil.sendMe(FileSystemMyFileResult.class, url, param, EurekaConstants.GETWITHINPUTSTREAM);
        return result.map;

    }

    public Map<String, MyFile> getWithoutInputStream(Set<FileObject> filenames) {
        FileSystemPathParam param = new FileSystemPathParam();
        param.configid = "myid";
        param.conf = conf;
        param.paths = filenames;
        FileSystemMyFileResult result = WebFluxUtil.sendMe(FileSystemMyFileResult.class, url, param, EurekaConstants.GETWITHOUTINPUTSTREAM);
        return result.map;

    }

    public FileObject getParent(FileObject f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fo = f;
        FileSystemFileObjectResult result = WebFluxUtil.sendMe(FileSystemFileObjectResult.class, url, param, EurekaConstants.GETPARENT);
        return result.getFileObject()[0];

    }

    public FileObject get(FileObject fo) {
        FileSystemPathParam param = new FileSystemPathParam();
        param.configid = "myid";
        param.conf = conf;
        param.path = fo;
        FileSystemFileObjectResult result = WebFluxUtil.sendMe(FileSystemFileObjectResult.class, url, param, EurekaConstants.GET);
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
        param.conf = conf;
        param.fos = f;
        FileSystemMessageResult result = WebFluxUtil.sendMe(FileSystemMessageResult.class, url, param, EurekaConstants.READFILE);
        return result.message;
    }

    public boolean writeFile(FileObject f, InmemoryMessage content) {
        Map<FileObject, InmemoryMessage> map = new HashMap<>();
        map.put(f, content);
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.map = map;
        FileSystemMessageResult result = WebFluxUtil.sendMe(FileSystemMessageResult.class, url, param, EurekaConstants.WRITEFILE);
        return true;
    }

    public Map<String, String> getMd5(Set<FileObject> f) {
        FileSystemFileObjectParam param = new FileSystemFileObjectParam();
        param.configid = "myid";
        param.conf = conf;
        param.fos = f;
        FileSystemStringResult result = WebFluxUtil.sendMe(FileSystemStringResult.class, url, param, EurekaConstants.GETMD5);
        return result.map;
    }

}

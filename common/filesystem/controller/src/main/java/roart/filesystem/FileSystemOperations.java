package roart.filesystem;

import roart.iclij.config.IclijConfig;
import roart.common.filesystem.FileSystemBooleanResult;
import roart.common.filesystem.FileSystemByteResult;
import roart.common.filesystem.FileSystemConstructorResult;
import roart.common.filesystem.FileSystemFileObjectParam;
import roart.common.filesystem.FileSystemFileObjectResult;
import roart.common.filesystem.FileSystemMessageResult;
import roart.common.filesystem.FileSystemMyFileResult;
import roart.common.filesystem.FileSystemPathParam;
import roart.common.filesystem.FileSystemPathResult;
import roart.common.filesystem.FileSystemStringResult;

public abstract class FileSystemOperations {
    
    protected String nodename;
    protected String configid;
    protected IclijConfig nodeConf;

    public FileSystemOperations(String nodename, String configid, IclijConfig nodeConf) {
        this.nodename = nodename;
        this.configid = configid;
        this.nodeConf = nodeConf;
    }

    public abstract FileSystemConstructorResult destroy() throws Exception;
	
    public abstract FileSystemFileObjectResult listFiles(FileSystemFileObjectParam param);
    
    public abstract FileSystemMyFileResult listFilesFull(FileSystemFileObjectParam param) throws Exception;
    
    public abstract FileSystemBooleanResult exists(FileSystemFileObjectParam param);
    
    public abstract FileSystemPathResult getAbsolutePath(FileSystemFileObjectParam param);

    public abstract FileSystemBooleanResult isDirectory(FileSystemFileObjectParam param);

    public abstract FileSystemByteResult getInputStream(FileSystemFileObjectParam param) throws Exception;
    
    public abstract FileSystemMyFileResult getWithInputStream(FileSystemPathParam param, boolean with) throws Exception;
    
    public abstract FileSystemFileObjectResult getParent(FileSystemFileObjectParam param);
    
    public abstract FileSystemFileObjectResult get(FileSystemPathParam param);

    public abstract FileSystemMessageResult readFile(FileSystemFileObjectParam param) throws Exception;

    public abstract FileSystemStringResult getMd5(FileSystemFileObjectParam param);

    public abstract FileSystemMessageResult writeFile(FileSystemFileObjectParam param) throws Exception;
    
}

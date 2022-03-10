package roart.common.filesystem;

import java.util.Map;
import java.util.Set;

import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.FileObject;

public class FileSystemFileObjectParam extends FileSystemParam {
    public FileObject fo;
    
    public Set<FileObject> fos;
    
    public Map<String, InmemoryMessage> map;
}

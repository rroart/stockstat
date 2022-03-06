package roart.common.filesystem;

import java.util.Map;

import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.FileObject;

public class FileSystemMessageResult extends FileSystemResult {
    public Map<String, InmemoryMessage> message;
}

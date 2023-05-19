package roart.filesystem;

import roart.common.constants.EurekaConstants;
import roart.iclij.config.IclijConfig;

public class HDFSAccess extends FileSystemAccess {

    public HDFSAccess(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.HDFS;
    }

}

package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;
import roart.iclij.config.IclijConfig;

public class HDFSAccess extends FileSystemAccess {

    public HDFSAccess(ConfigData conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.HDFS;
    }

}

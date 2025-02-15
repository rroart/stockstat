package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;

public class HDFSAccess extends FileSystemAccess {

    public HDFSAccess(ConfigData conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.HDFS;
    }

}

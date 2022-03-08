package roart.filesystem;

import roart.common.constants.EurekaConstants;
import roart.common.config.MyMyConfig;

public class HDFSAccess extends FileSystemAccess {

    public HDFSAccess(MyMyConfig conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.HDFS;
    }

}

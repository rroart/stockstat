package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;

public class HDFSDS extends FileSystemDS {

    public HDFSDS(ConfigData conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.HDFS;
    }

}

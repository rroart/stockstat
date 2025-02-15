package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;

public class S3Access extends FileSystemAccess{

    public S3Access(ConfigData conf) {
	super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.S3;
    }
}

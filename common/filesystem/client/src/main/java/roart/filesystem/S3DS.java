package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;

public class S3DS extends FileSystemDS {

    public S3DS(ConfigData conf) {
	super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.S3;
    }
}

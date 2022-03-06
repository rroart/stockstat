package roart.filesystem;

import roart.common.constants.EurekaConstants;
import roart.common.config.MyMyConfig;

public class S3Access extends FileSystemAccess{

    public S3Access(MyMyConfig conf) {
	super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.S3;
    }
}
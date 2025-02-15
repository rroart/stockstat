package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;

public class LocalFileSystemAccess extends FileSystemAccess {

    public LocalFileSystemAccess(ConfigData conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.LOCAL;
    }

}

package roart.filesystem;

import roart.common.constants.EurekaConstants;
import roart.common.config.MyMyConfig;

public class LocalFileSystemAccess extends FileSystemAccess {

    public LocalFileSystemAccess(MyMyConfig conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.LOCAL;
    }

}

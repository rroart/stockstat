package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;

public class LocalFileSystemDS extends FileSystemDS {

    public LocalFileSystemDS(ConfigData conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.LOCAL;
    }

}

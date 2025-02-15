package roart.filesystem;

import roart.common.config.ConfigData;
import roart.common.constants.EurekaConstants;

public class SwiftAccess extends FileSystemAccess {

    public SwiftAccess(ConfigData conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.SWIFT;
    }

}

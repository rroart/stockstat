package roart.filesystem;

import roart.common.constants.EurekaConstants;
import roart.common.config.MyMyConfig;

public class SwiftAccess extends FileSystemAccess {

    public SwiftAccess(MyMyConfig conf) {
        super(conf);
    }
    
    @Override
    public String getAppName() {
        return EurekaConstants.SWIFT;
    }

}

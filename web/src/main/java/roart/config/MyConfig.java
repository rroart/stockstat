package roart.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import roart.service.ControlService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MyConfig {

    protected static Logger log = LoggerFactory.getLogger(MyConfig.class);
    
    protected static MyConfig instance = null;
    
    public static MyConfig instance() {
        return instance;
    }
    
    public MyConfig() {
    }
    
    public boolean useSpark = false;
    
    public String sparkMaster = null;
    
    public abstract void config() throws Exception;

}

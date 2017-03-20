package roart.servlet.listeners;

import roart.config.MyConfig;
import roart.config.MyPropertyConfig;
import roart.util.Constants;
import roart.util.EurekaUtil;

import javax.servlet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupListener implements javax.servlet.ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(StartupListener.class);

    public void contextInitialized(ServletContextEvent context)  {
    	EurekaUtil.initEurekaClient();
    	MyConfig conf = MyPropertyConfig.instance();
        try {
            conf.config();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        System.out.println("config done");
        log.info("config done");
    }

    private Integer getInteger(String str) {
    	try {
    		return new Integer(str);
    	} catch (NumberFormatException e) {
    		log.error(Constants.EXCEPTION, e);
    	}
		return -1;
	}

	public void contextDestroyed(ServletContextEvent context) {
    }

}


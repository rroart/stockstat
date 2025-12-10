package roart.servlet.listeners;

import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.eureka.util.EurekaUtil;
import roart.service.ControlService;

import jakarta.servlet.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupListener implements ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(StartupListener.class);

    public void contextInitialized(ServletContextEvent context)  {
    	//EurekaUtil.initEurekaClient();
        try {
            ControlService maininst = new ControlService();
			//maininst.dbengine(false);
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

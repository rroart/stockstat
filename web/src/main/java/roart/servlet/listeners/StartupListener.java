package roart.servlet.listeners;

import roart.config.MyConfig;
import roart.config.MyPropertyConfig;
import roart.model.ResultItem;
import roart.service.ControlService;
import sun.rmi.rmic.newrmic.Constants;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.*;
import javax.servlet.http.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupListener implements javax.servlet.ServletContextListener {

    private static final Logger log = LoggerFactory.getLogger(StartupListener.class);

    public void contextInitialized(ServletContextEvent context)  {
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


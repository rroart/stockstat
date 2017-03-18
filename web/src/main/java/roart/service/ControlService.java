package roart.service;

import roart.model.GUISize;
import roart.model.Meta;
import roart.model.ResultItemNot;
import roart.model.ResultItem;
import roart.model.Stock;

import roart.config.MyConfig;
import roart.config.MyPropertyConfig;

import javax.servlet.http.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;
import java.util.TreeMap;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;
import java.io.*;

import roart.util.Constants;
import roart.util.EurekaConstants;
import roart.util.EurekaUtil;

import org.apache.commons.math3.util.Pair;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public MyPropertyConfig conf;
    
    public ControlService() {
    	conf = new MyPropertyConfig();
    }
    
    /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContent() {
        ServiceParam param = new ServiceParam();
        param.config = conf;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONTENT);
	/*
        for (Object o : (List)((List)result.list2)) {
			//for (Object o : (List)((List)result.list).get(0)) {
		 	log.info("obj type " + o.getClass().getName());
		 	if ("java.util.LinkedHashMap".equals(o.getClass().getName())) {
		 		java.util.LinkedHashMap l = (java.util.LinkedHashMap) o;
		 		log.info("size0 " + l.size());
		 		log.info("keyset " + l.keySet());
		 	}
		}
		*/
       return result.list;
    }

    /**
     * Create result graphs
     * @param guiSize TODO
     * 
     * @return the image list
     */

    public List getContentGraph(GUISize guiSize) {
        ServiceParam param = new ServiceParam();
        param.config = conf;
        param.guiSize = guiSize;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONTENTGRAPH);
        return result.list;
    }

    /**
     * Create result graphs for one
     * 
     * myid the id of the unit
     * @param guiSize TODO
     * @return the image list
     */

    public List getContentGraph(Set<Pair<String, String>> ids, GUISize guiSize) {
		// TODO fix quick workaround for serialization
    	Set<String> idset = new HashSet<>();
    	for (Pair pair : ids) {
    		idset.add(pair.getFirst() + "," + pair.getSecond());
    	}
    	ServiceParam param = new ServiceParam();
        param.config = conf;
        param.ids = idset;
        param.guiSize = guiSize;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONTENTGRAPH2);
        return result.list;
    }

    public String getAppName() {
    	return EurekaConstants.STOCKSTAT;
    }

    /**
     * Create stat result lists
     * 
     * @return the tabular result lists
     */

    public List getContentStat() {
        ServiceParam param = new ServiceParam();
        param.config = conf;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONTENTSTAT);
        return result.list;
    }

    private void printstock(List<Stock> stocklistPeriod4Day1, int imax) {
        int i = 0;
        for (Stock s : stocklistPeriod4Day1) {
            //log.info(s.getId() + " : " + s.getName() + " : " + s.getPeriod4());
            i++;
            if (i > imax) {
                return;
            }
        }
    }

}

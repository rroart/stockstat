package roart.service;

import roart.model.GUISize;
import roart.model.ResultItem;
import roart.config.ConfigConstants;
import roart.config.ConfigTreeMap;
import roart.config.MyConfig;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import roart.util.EurekaConstants;
import roart.util.EurekaUtil;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public MyConfig conf;
    
    public ControlService() {
    	conf = MyConfig.instance();
    	//getConfig();
    }
  
    public void getConfig() {
        ServiceParam param = new ServiceParam();
        param.config = conf;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONFIG);
        conf = result.config;
        ConfigConstants.makeTypeMap();
        ConfigConstants.makeTextMap();
        Map<String, Object> map = conf.configValueMap;
        for (String key : map.keySet()) {
            Object value = map.get(key);
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            System.out.println("k " + key + " " + value);
            if (value != null) {
                System.out.println("cls " + value.getClass().getName());
            }
        }
        ConfigTreeMap map2 = conf.configTreeMap;
        print(map2, 0);
       
    }
    
    private void print(ConfigTreeMap map2, int indent) {
        String space = "      ";
        System.out.print(space.substring(0, indent));
        System.out.println("map2 " + map2.name + " " + map2.enabled);
        Map<String, ConfigTreeMap> map3 = map2.configTreeMap;
        for (String key : map3.keySet()) {
        print(map3.get(key), indent + 1);
            //Object value = map.get(key);
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
        }
       
    }

    public List<String> getMarkets() {
        ServiceParam param = new ServiceParam();
        param.config = conf;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETMARKETS);
        return result.markets;    	
    }
    
    public Map<String, String> getStocks(String market) {
        ServiceParam param = new ServiceParam();
        param.config = conf;
        param.market = market;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETSTOCKS);
        return result.stocks;   	
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

    public void dbengine(Boolean useSpark) throws Exception {
        ServiceParam param = new ServiceParam();
        param.config = conf;
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.SETCONFIG);
        getConfig();
    }
}

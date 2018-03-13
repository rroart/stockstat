package roart.service;

import roart.model.GUISize;
import roart.model.IncDecItem;
import roart.model.ResultItem;
import roart.queue.IclijQueues;
import roart.thread.ClientRunner;
import roart.config.ConfigTreeMap;
import roart.config.MyConfig;
import roart.config.VerifyConfig;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import roart.util.EurekaConstants;
import roart.util.EurekaUtil;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.UI;

public class IclijWebControlService {
    private static Logger log = LoggerFactory.getLogger(IclijWebControlService.class);

    public MyConfig conf;
    
    private VerifyConfig verifyConfig;
    
    public VerifyConfig getVerifyConfig() {
        return verifyConfig;
    }

    public void setVerifyConfig(VerifyConfig verifyConfig) {
        this.verifyConfig = verifyConfig;
    }

    public IclijWebControlService() {
    	//conf = MyXMLConfig.configInstance();
    	//getConfig();
        verifyConfig = new VerifyConfig();
        startThreads();
        UI ui = com.vaadin.ui.UI.getCurrent();
        // temp hack :(
        ClientRunner.uiset.clear();
        ClientRunner.uiset.put(ui, "");
    }
  
    public void getConfig() {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONFIG);
        conf = result.config;
        Map<String, Object> map = conf.configValueMap;
        for (Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            System.out.println("k " + entry.getKey() + " " + value);
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
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, EurekaConstants.STOCKSTAT, EurekaConstants.GETMARKETS);
        return result.markets;    	
    }
    
    public Map<String, String> getStocks(String market) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setMarket(market);
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETSTOCKS);
        return result.stocks;   	
    }
    
    /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public void getContent() {
        IclijServiceParam param = new IclijServiceParam();
        //param.setConfig(conf);
        param.setWebpath(EurekaConstants.GETCONTENT);
        IclijQueues.clientQueue.add(param);
        //IclijServiceResult result = EurekaUtil.sendMe(IclijServiceResult.class, param, getAppName(), EurekaConstants.GETCONTENT);
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
    }

    /**
     * Create test result lists
     * 
     * @return the tabular result lists
     */

    public void getVerify() {
        IclijServiceParam param = new IclijServiceParam();
        param.setVerifyConfig(verifyConfig);
        param.setWebpath(EurekaConstants.GETVERIFY);
        IclijQueues.clientQueue.add(param);
        //IclijServiceResult result = EurekaUtil.sendMe(IclijServiceResult.class, param, getAppName(), EurekaConstants.GETVERIFY);
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
    }

    /**
     * Create result graphs
     * @param guiSize TODO
     * 
     * @return the image list
     */

    public List getContentGraph(GUISize guiSize) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setGuiSize(guiSize);
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
        param.setConfig(conf);
        param.setIds(idset);
        param.setGuiSize(guiSize);
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONTENTGRAPH2);
        return result.list;
    }

    public String getAppName() {
    	return EurekaConstants.ICLIJ;
    }

    /**
     * Create stat result lists
     * 
     * @return the tabular result lists
     */

    public List getContentStat() {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETCONTENTSTAT);
        return result.list;
    }

    public void dbengine(Boolean useSpark) throws Exception {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.SETCONFIG);
        getConfig();
    }

    public List<ResultItem> getTestRecommender(boolean doSet) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendMe(ServiceResult.class, param, getAppName(), EurekaConstants.GETTESTRECOMMENDER);
        if (doSet) {
            conf = result.config;
        }
        return result.list;
    }
    
    private static ClientRunner clientRunnable = null;
    public static Thread clientWorker = null;

    public static void startThreads() {
        if (clientRunnable == null) {
            startClientWorker();
        }
    }

    public static void startClientWorker() {
        clientRunnable = new ClientRunner();
        clientWorker = new Thread(clientRunnable);
        clientWorker.setName("ClientWorker");
        clientWorker.start();
        //log.info("starting client worker");                               
    }

}

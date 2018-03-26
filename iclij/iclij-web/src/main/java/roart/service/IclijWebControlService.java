package roart.service;

import roart.model.GUISize;
import roart.model.IncDecItem;
import roart.model.ResultItem;
import roart.queue.IclijQueues;
import roart.queue.MyExecutors;
import roart.thread.ClientRunner;
import roart.client.MyIclijUI;
import roart.config.ConfigTreeMap;
import roart.config.IclijConfig;
import roart.config.MyConfig;
import roart.config.VerifyConfig;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import roart.util.Constants;
import roart.util.EurekaConstants;
import roart.util.EurekaUtil;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

public class IclijWebControlService {
    private static Logger log = LoggerFactory.getLogger(IclijWebControlService.class);

    private MyConfig conf;

    private IclijConfig iclijConf;

    private ObjectMapper objectMapper;

    public IclijWebControlService() {
        //conf = MyXMLConfig.configInstance();
        //getConfig();
        //verifyConfig = new VerifyConfig();
        objectMapper = jsonObjectMapper();
        startThreads();
        UI ui = com.vaadin.ui.UI.getCurrent();
        // temp hack :(
        ClientRunner.uiset.clear();
        ClientRunner.uiset.put(ui, "");
    }

    /*
    private VerifyConfig verifyConfig;

    public VerifyConfig getVerifyConfig() {
        return verifyConfig;
    }

    public void setVerifyConfig(VerifyConfig verifyConfig) {
        this.verifyConfig = verifyConfig;
    }
     */

    public MyConfig getConf() {
        return conf;
    }

    public void setConf(MyConfig conf) {
        this.conf = conf;
    }

    public IclijConfig getIclijConf() {
        return iclijConf;
    }

    public void setIclijConf(IclijConfig iclijConf) {
        this.iclijConf = iclijConf;
    }

    public void getConfig() {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        IclijServiceResult result = EurekaUtil.sendMe(IclijServiceResult.class, param, getAppName(), EurekaConstants.GETCONFIG);
        iclijConf = result.getIclijConfig();
        Map<String, Object> map = iclijConf.getConfigValueMap();
        for (Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            System.out.println("k " + entry.getKey() + " " + value);
            if (value != null) {
                System.out.println("cls " + value.getClass().getName());
            }
        }
        ConfigTreeMap map2 = iclijConf.getConfigTreeMap();
        print(map2, 0);
        MyExecutors.init(iclijConf.mpClientCpuFraction());      
    }

    private void print(ConfigTreeMap map2, int indent) {
        String space = "      ";
        System.out.print(space.substring(0, indent));
        System.out.println("map2 " + map2.getName() + " " + map2.getEnabled());
        Map<String, ConfigTreeMap> map3 = map2.getConfigTreeMap();
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

    public void getContent(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        //param.setConfig(conf);
        param.setWebpath(EurekaConstants.GETCONTENT);
        new IclijThread(ui, param).start();
        //IclijQueues.clientQueue.add(param);
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

    public void getVerify(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        //param.setVerifyConfig(verifyConfig);
        param.setIclijConfig(getIclijConf());
        param.setWebpath(EurekaConstants.GETVERIFY);
        new IclijThread(ui, param).start();
        //IclijQueues.clientQueue.add(param);
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

    public void getVerifyLoop(MyIclijUI ui) {
        for (int i = 0; i < iclijConf.verificationLoops(); i++) {
            IclijServiceParam param = new IclijServiceParam();
            param.setIclijConfig(getIclijConf());
            param.setWebpath(EurekaConstants.GETVERIFY);
            param.setOffset(i * getIclijConf().verificationLoopInterval());
            IclijThread thread = new IclijThread(ui, param);
            MyExecutors.run(thread);
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                log.info(Constants.EXCEPTION, "Sleep interrupted");
            }
        }
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

    class IclijThread extends Thread {
        private IclijServiceParam param;
        private MyIclijUI ui;

        public IclijThread(MyIclijUI ui, IclijServiceParam param) {
            this.ui = ui;
            this.param = param;
        }

        @Override
        public void run() {
            IclijServiceResult result = EurekaUtil.sendMe(IclijServiceResult.class, param, getAppName(), param.getWebpath(), objectMapper);
            ui.access(() -> {
                VerticalLayout layout = new VerticalLayout();
                layout.setCaption("Results");
                ui.displayResultListsTab(layout, result.getLists());
                ui.notify("New results");
            });
        }
    }

    private ObjectMapper jsonObjectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(new JavaTimeModule())
                .build();
    }
}

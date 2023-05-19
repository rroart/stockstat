package roart.service;

import roart.queue.Queues;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.thread.ClientRunner;
import roart.client.MyVaadinUI;
import roart.common.config.ConfigConstants;
import roart.common.config.ConfigTreeMap;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.constants.EurekaConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.eureka.util.EurekaUtil;
import roart.executor.MyExecutors;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vaadin.ui.VerticalLayout;

public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public MyConfig conf;

    public ControlService() {
        //conf = MyXMLConfig.configInstance();
        getConfig();
        startThreads();
    }

    public void getConfig() {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG);
        conf = result.getConfig();
        Map<String, Object> map = conf.getConfigData().getConfigValueMap();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            System.out.println("k " + key + " " + value);
            if (value != null) {
                System.out.println("cls " + value.getClass().getName());
            }
        }
        ConfigTreeMap map2 = conf.getConfigData().getConfigTreeMap();
        print(map2, 0);
        MyMyConfig aConf = new MyMyConfig(conf);
        MyExecutors.init(new double[] { (aConf).getMLMPCpu() } );
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
        ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETMARKETS);
        return result.getMarkets();    	
    }

    public Map<String, String> getStocks(String market) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setMarket(market);
        ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETSTOCKS);
        return result.getStocks();   	
    }

    /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public void getContent(MyVaadinUI ui) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setWebpath(EurekaConstants.GETCONTENT);
        MyMyConfig aConf = new MyMyConfig(conf);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(false);
        neuralnetcommand.setMlclassify(true);
        neuralnetcommand.setMldynamic(false);
        param.setNeuralnetcommand(neuralnetcommand);
        new CoreThread(ui, param).start();
        //Queues.clientQueue.add(param);
        //ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENT);
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
        //return result.list;
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
        ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH);
        return result.getList();
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
        ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH2);
        return result.getList();
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
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENTSTAT);
        return result.getList();
    }

    public void dbengine(Boolean useSpark) throws Exception {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = EurekaUtil.sendCMe(ServiceResult.class, param, EurekaConstants.SETCONFIG);
        getConfig();
    }

    public void getEvolveRecommender(MyVaadinUI ui, boolean doSet) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setWebpath(EurekaConstants.GETEVOLVERECOMMENDER);
        new EvolveCoreThread(ui, param, doSet).start();
    }

    public void getEvolveML(MyVaadinUI ui, boolean doSet, String ml) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        Set<String> ids = new HashSet<>();
        ids.add(ml);
        param.setIds(ids);
        param.setWebpath(EurekaConstants.GETEVOLVENN);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(true);
        neuralnetcommand.setMlclassify(true);
        neuralnetcommand.setMldynamic(false);
        param.setNeuralnetcommand(neuralnetcommand);
        new EvolveCoreThread(ui, param, doSet).start();
    }

    public void resetML(String ml) {
        if (ml.equals(PipelineConstants.PREDICTOR)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG, null);
        }
        if (ml.equals(PipelineConstants.MLINDICATOR)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORMLCONFIG, null);
        }
        if (ml.equals(PipelineConstants.MLMACD)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACDMLCONFIG, null);
        }
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

    class CoreThread extends Thread {
        private ServiceParam param;

        private MyVaadinUI ui;

        private ServiceResult result;

        public CoreThread(MyVaadinUI ui, ServiceParam param) {
            this.ui = ui;
            this.param = param;
        }

        public MyVaadinUI getUi() {
            return ui;
        }

        public void setUi(MyVaadinUI ui) {
            this.ui = ui;
        }

        public ServiceResult getResult() {
            return result;
        }

        public void setResult(ServiceResult result) {
            this.result = result;
        }

        @Override
        public void run() {
            result = EurekaUtil.sendCMe(ServiceResult.class, param, param.getWebpath());
            ui.access(() -> {
                VerticalLayout layout = new VerticalLayout();
                layout.setCaption("Results");
                ui.displayResultListsTab(layout, result.getList());
                ui.notify("New results");
            });
        }
    }

    class EvolveCoreThread extends CoreThread {

        boolean doSet;

        public EvolveCoreThread(MyVaadinUI ui, ServiceParam param, boolean doSet) {
            super(ui, param);
            this.doSet = doSet;
        }

        @Override
        public void run() {
            super.run();
            if (doSet) {
                Map<String, Object> updateMap = getResult().getMaps().get("update");
                conf.getConfigData().getConfigValueMap().putAll(updateMap);
            }
            if (doSet) {
                getUi().access(() -> 
                    getUi().replaceControlPanelTab()
                );
            }
        }
    }

}

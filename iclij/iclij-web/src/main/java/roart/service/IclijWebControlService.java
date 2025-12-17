package roart.service;

import roart.executor.MyExecutors;
import roart.result.model.GUISize;
import roart.result.model.ResultItem;
import roart.thread.ClientRunner;
import roart.client.MyIclijUI;
import roart.common.config.ConfigTreeMap;
import roart.common.config.MyConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.math3.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
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
        param.setConfigData(conf.getConfigData());
        IclijServiceResult result = WebFluxUtil.sendIMe(IclijServiceResult.class, param, WebFluxConstants.GETCONFIG);
        iclijConf = null; //result.getConfig();
        Map<String, Object> map = iclijConf.getConfigData().getConfigValueMap();
        for (Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            System.out.println("k " + entry.getKey() + " " + value);
            if (value != null) {
                System.out.println("cls " + value.getClass().getName());
            }
        }
        ConfigTreeMap map2 = iclijConf.getConfigData().getConfigTreeMap();
        print(map2, 0);
        MyExecutors.init(new double[] { iclijConf.mpClientCpu() } );      
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
        param.setConfigData(conf.getConfigData());
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, WebFluxConstants.GETMARKETS);
        return result.getMarkets();    	
    }

    public Map<String, String> getStocks(String market) {
        ServiceParam param = new ServiceParam();
        param.setConfigData(conf.getConfigData());
        param.setMarket(market);
        ServiceResult result = WebFluxUtil.sendIMe(ServiceResult.class, param, WebFluxConstants.GETSTOCKS);
        return result.getStocks();   	
    }

    /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public void getContent(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENT);
        new IclijThread(ui, param).start();
    }

    public void getContentImprove(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENTIMPROVE);
        new IclijThread(ui, param).start();
    }

    public void getContentEvolve(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENTEVOLVE);
        new IclijThread(ui, param).start();
    }

    public void getContentMachineLearning(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENTMACHINELEARNING);
        new IclijThread(ui, param).start();
    }

    public void getContentDataset(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENTDATASET);
        new IclijThread(ui, param).start();
    }

    public void getContentFilter(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENTFILTER);
        new IclijThread(ui, param).start();
    }

    public void getContentAboveBelow(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENTABOVEBELOW);
        new IclijThread(ui, param).start();
    }

    public void getContentCrosstest(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETCONTENTCROSSTEST);
        new IclijThread(ui, param).start();
    }

    public void getImproveAboveBelowMarket(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETIMPROVEABOVEBELOW);
        new IclijThread(ui, param).start();
    }

    public void getSingleMarket(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETSINGLEMARKET);
        new IclijThread(ui, param).start();
    }

    public void getSingleMarketLoop(MyIclijUI ui) {
        for (int i = 0; i < iclijConf.singlemarketLoops(); i++) {
            IclijServiceParam param = new IclijServiceParam();
            param.setConfig(getIclijConf());
            param.setWebpath(WebFluxConstants.GETSINGLEMARKET);
            param.setOffset(i * getIclijConf().singlemarketLoopInterval());
            IclijThread thread = new IclijThread(ui, param);
            MyExecutors.run(thread, 0);
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                log.info(Constants.EXCEPTION, "Sleep interrupted");
            }
        }
    }

    public void getImproveProfit(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETIMPROVEPROFIT);
        new IclijThread(ui, param).start();
    }

    /**
     * Create test result lists
     * 
     * @return the tabular result lists
     */

    public void getVerify(MyIclijUI ui) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfig(getIclijConf());
        param.setWebpath(WebFluxConstants.GETVERIFY);
        IclijThread thread = new IclijThread(ui, param);
        MyExecutors.run(thread, 0);
    }

    public void getVerifyLoop(MyIclijUI ui) {
        for (int i = 0; i < iclijConf.verificationLoops(); i++) {
            IclijServiceParam param = new IclijServiceParam();
            param.setConfig(getIclijConf());
            param.setWebpath(WebFluxConstants.GETVERIFY);
            param.setOffset(i * getIclijConf().verificationLoopInterval());
            IclijThread thread = new IclijThread(ui, param);
            MyExecutors.run(thread, 0);
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
        param.setConfigData(conf.getConfigData());
        param.setGuiSize(guiSize);
        ServiceResult result = WebFluxUtil.sendIMe(ServiceResult.class, param, WebFluxConstants.GETCONTENTGRAPH);
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
        param.setConfigData(conf.getConfigData());
        param.setIds(idset);
        param.setGuiSize(guiSize);
        ServiceResult result = WebFluxUtil.sendIMe(ServiceResult.class, param, WebFluxConstants.GETCONTENTGRAPH2);
        return result.getList();
    }

    public String getAppName() {
        return WebFluxConstants.ICLIJ;
    }

    /**
     * Create stat result lists
     * 
     * @return the tabular result lists
     */

    public List getContentStat() {
        ServiceParam param = new ServiceParam();
        param.setConfigData(conf.getConfigData());
        ServiceResult result = WebFluxUtil.sendIMe(ServiceResult.class, param, WebFluxConstants.GETCONTENTSTAT);
        return result.getList();
    }

    public void dbengine(Boolean useSpark) throws Exception {
        ServiceParam param = new ServiceParam();
        param.setConfigData(conf.getConfigData());
        ServiceResult result = WebFluxUtil.sendIMe(ServiceResult.class, param, WebFluxConstants.SETCONFIG);
        getConfig();
    }

    public List<ResultItem> getTestRecommender(boolean doSet) {
        ServiceParam param = new ServiceParam();
        param.setConfigData(conf.getConfigData());
        ServiceResult result = WebFluxUtil.sendIMe(ServiceResult.class, param, WebFluxConstants.GETEVOLVERECOMMENDER);
        if (doSet) {
            conf = result.getConfig();
        }
        return result.getList();
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
            IclijServiceResult result = WebFluxUtil.sendIMe(IclijServiceResult.class, param, param.getWebpath(), objectMapper);
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

package roart.iclij.service;

import roart.common.config.CacheConstants;
import roart.common.config.ConfigTreeMap;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.webflux.WebFluxUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.model.WebData;
import roart.iclij.model.WebDataJson;
import roart.iclij.model.component.ComponentInput;
import roart.result.model.ResultItem;
import roart.common.service.ServiceParam;
import roart.common.service.ServiceResult;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.cache.MyCache;
import roart.common.webflux.WebFluxUtil;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public MyMyConfig conf;
    ObjectMapper objectMapper;
    
    public static CuratorFramework curatorClient;

    public ControlService() {
    	//conf = MyConfig.instance();
    	//getConfig();
        objectMapper = jsonObjectMapper();
    }
  
    public void getConfig() {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG);
        /*
        IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(EurekaConstants.GETCONFIG, iclijConfig.getServices(), iclijConfig.getCommunications());
        ServiceResult result;// = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG);
        Communication c = CommunicationFactory.get(sc.getLeft(), ServiceResult.class, EurekaConstants.GETCONFIG, objectMapper, true, true, true, sc.getRight());
        param.setWebpath(c.getReturnService());
        result = (ServiceResult) c.sendReceive(param);
        */
        //ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, "http://localhost:12345/" + EurekaConstants.GETCONFIG);
        conf = new MyMyConfig(result.getConfig());
        Map<String, Object> map = conf.getConfigValueMap();
        for (String key : map.keySet()) {
            Object value = map.get(key);
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            //System.out.println("k " + key + " " + value);
            if (value != null) {
                //System.out.println("cls " + value.getClass().getName());
            }
        }
        ConfigTreeMap map2 = conf.getConfigTreeMap();
        print(map2, 0);
       
    }
    
    private <T> T sendCMe(Class<T> myclass, ServiceParam param, String service) {
        IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        T[] result;// = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG        
        Communication c = CommunicationFactory.get(sc.getLeft(), myclass, service, objectMapper, true, true, true, sc.getRight());
        param.setWebpath(c.getReturnService());
        result = c.sendReceive(param);
        return result[0];
    }

    private <T> T sendAMe(Class<T> myclass, IclijServiceParam param, String service, ObjectMapper objectMapper) {
        IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        T[] result;// = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG        
        Communication c = CommunicationFactory.get(sc.getLeft(), myclass, service, objectMapper, true, true, true, sc.getRight());
        param.setWebpath(c.getReturnService());
        result = c.sendReceive(param);
        return result[0];
    }

    public void send(String service, Object object, ObjectMapper objectMapper) {
        IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        String appid = System.getenv(Constants.APPID);
        if (appid != null) {
            service = service + appid; // can not handle domain, only eureka
        }
        Communication c = CommunicationFactory.get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight());
        c.send(object);
    }

    public void send(String service, Object object, IclijConfig config) {
        Inmemory inmemory = InmemoryFactory.get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
        String id = service + System.currentTimeMillis() + UUID.randomUUID();
        InmemoryMessage message = inmemory.send(id, object);
        send(service, message);
    }

    public void send(String service, Object object) {
        if (object == null) {
            log.error("Empty msg for {}", service);
            return;
        }
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        send(service, object, mapper);
    }

    private void print(ConfigTreeMap map2, int indent) {
        String space = "      ";
        //System.out.print(space.substring(0, indent));
        //System.out.println("map2 " + map2.name + " " + map2.enabled);
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
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETMARKETS);
        return result.getMarkets();    	
    }
    
    public List<MetaItem> getMetas() {
        String key = CacheConstants.METAS;
        List<MetaItem> list = (List<MetaItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETMETAS);
        list = result.getMetas();
        MyCache.getInstance().put(key, list);
        return list;
    }
    
    public Map<String, String> getStocks(String market) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setMarket(market);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETSTOCKS);
        return result.getStocks();   	
    }
    
    public List<String> getDates(String market) {
        String key = CacheConstants.DATES + conf.getMarket() + conf.getdate();
        List<String> list =  (List<String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setWantMaps(true);
        param.setMarket(market);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETDATES);
        list = (List<String>) result.getMaps().get(PipelineConstants.DATELIST).get(PipelineConstants.DATELIST);      
        MyCache.getInstance().put(key, list);
        return list;
    }
   /**
     * Create result lists
     * 
     * @return the tabular result lists
     */

    public Map<String, Map<String, Object>> getContent() {
        return getContent(new ArrayList<>());
    }
    
    public Map<String, Map<String, Object>> getContent(List<String> disableList) {
        {
            long heapSize = Runtime.getRuntime().totalMemory(); 

         // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
         long heapMaxSize = Runtime.getRuntime().maxMemory();

          // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
         long heapFreeSize = Runtime.getRuntime().freeMemory(); 
         log.info("MEM0 " + heapSize + " " + heapMaxSize + " " + heapFreeSize);
        }
        String key = CacheConstants.CONTENT + conf.getMarket() + conf.getMLmarket() + conf.getdate() + conf.getConfigValueMap();
        Map<String, Map<String, Object>> list = (Map<String, Map<String, Object>>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setWantMaps(true);
        param.setConfList(disableList);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(conf.wantMLLearn());
        neuralnetcommand.setMlclassify(conf.wantMLClassify());
        neuralnetcommand.setMldynamic(conf.wantMLDynamic());
        neuralnetcommand.setMlcross(conf.wantMLCross());
        param.setNeuralnetcommand(neuralnetcommand);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENT);
        //log.info("blblbl" + JsonUtil.convert(result).length());
        list = result.getMaps();
        MyCache.getInstance().put(key, list);
        {
            long heapSize = Runtime.getRuntime().totalMemory(); 

         // Get maximum size of heap in bytes. The heap cannot grow beyond this size.// Any attempt will result in an OutOfMemoryException.
         long heapMaxSize = Runtime.getRuntime().maxMemory();

          // Get amount of free memory within the heap in bytes. This size will increase // after garbage collection and decrease as new objects are created.
         long heapFreeSize = Runtime.getRuntime().freeMemory(); 
         log.info("MEM1 " + heapSize + " " + heapMaxSize + " " + heapFreeSize);
        }
        return list;
        //return result.getMaps();
        //ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, "http://localhost:12345/" + EurekaConstants.GETCONTENT);
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
     * @param guiSize gui size
     * 
     * @return the image list
     */

    public List getContentGraph() {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH);
        return result.getList();
    }

    /**
     * Create result graphs for one
     * 
     * myid the id of the unit
     * @param guiSize gui size
     * @return the image list
     */

    public List getContentGraph(Set<Pair<String, String>> ids) {
    	Set<String> idset = new HashSet<>();
    	for (Pair pair : ids) {
    		idset.add(pair.getLeft() + "," + pair.getRight());
    	}
    	ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setIds(idset);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH2);
        return result.getList();
    }

    public Map<String, Map<String, Object>> getRerun(List<String> disableList) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setWantMaps(true);
        param.setConfList(disableList);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(conf.wantMLLearn());
        neuralnetcommand.setMlclassify(conf.wantMLClassify());
        neuralnetcommand.setMldynamic(conf.wantMLDynamic());
        neuralnetcommand.setMlcross(conf.wantMLCross());
        param.setNeuralnetcommand(neuralnetcommand);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, "/findprofit");
        return result.getMaps();
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
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONTENTSTAT);
        return result.getList();
    }

    public void dbengine(Boolean useSpark) throws Exception {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.SETCONFIG);
        getConfig();
    }

    public List<ResultItem> getEvolveRecommender(boolean doSet, List<String> disableList, Map<String, Object> updateMap, Map<String, Object> scoreMap, Map<String, Object> resultMap) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        param.setConfList(disableList);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETEVOLVERECOMMENDER);
        if (doSet) {
            //conf = new MyMyConfig(result.getConfig());
            updateMap.putAll(result.getMaps().get("update"));
            scoreMap.putAll(result.getMaps().get("score"));
            resultMap.putAll(result.getMaps().get("result"));
        }
        return result.getList();
        //return result.getMaps().get("update");
    }

    public List<ResultItem> getEvolveML(boolean doSet, List<String> disableList, String ml, MyMyConfig conf, Map<String, Object> updateMap, Map<String, Object> scoreMap, Map<String, Object> resultMap) {
        ServiceParam param = new ServiceParam();
        param.setConfig(conf);
        Set<String> ids = new HashSet<>();
        ids.add(ml);
        param.setIds(ids);
        param.setConfList(disableList);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(true);
        neuralnetcommand.setMlclassify(true);
        // where is this reset?
        neuralnetcommand.setMldynamic(true);
        param.setNeuralnetcommand(neuralnetcommand);
        ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETEVOLVENN);
        if (doSet) {
            updateMap.putAll(result.getMaps().get("update"));
            scoreMap.putAll(result.getMaps().get("score"));
            resultMap.putAll(result.getMaps().get("result"));
            //Map<String, Object> updateMap = result.getMaps().get("update");
            //conf.getConfigValueMap().putAll(updateMap);
            //return updateMap;
        }
        return result.getList();
    }

    public WebData getRun(String action, ComponentInput componentInput) {
        // TODO Auto-generated method stub
        IclijServiceParam param = new IclijServiceParam();
        param.setIclijConfig(componentInput.getConfig());
        param.setWebpath(EurekaConstants.ACTION + "/" + action);
        param.setOffset(componentInput.getLoopoffset());
        IclijServiceResult result = WebFluxUtil.sendAMe(IclijServiceResult.class, param, param.getWebpath(), objectMapper);

        WebDataJson dataJson = result.getWebdatajson();
        WebData data = convert(dataJson);
        return data;
    }

    public <T> T sendReceive(Communication c, IclijServiceParam param) {
        param.setWebpath(c.getReturnService());
        T r = (T) c.sendReceive(param);
        return r;
    }

    public WebData getVerify(String findprofit, ComponentInput componentInput) {
        // TODO Auto-generated method stub
        IclijServiceParam param = new IclijServiceParam();
        param.setIclijConfig(componentInput.getConfig());
        param.setWebpath(EurekaConstants.GETVERIFY);
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(EurekaConstants.GETVERIFY, componentInput.getConfig().getServices(), componentInput.getConfig().getCommunications());
        param.setOffset(componentInput.getLoopoffset());
        IclijServiceResult result = sendAMe(IclijServiceResult.class, param, param.getWebpath(), objectMapper);
        /*
        Communication c = CommunicationFactory.get(sc.getLeft(), IclijServiceResult.class, param.getWebpath(), objectMapper, true, true, true, sc.getRight());
        param.setWebpath(c.getReturnService());
        */
        //result = (IclijServiceResult[]) c.sendReceive(param);
        
        WebDataJson dataJson = result.getWebdatajson();
        WebData data = convert(dataJson);
        return data;
    }

    private WebData convert(WebDataJson dataJson) {
        WebData data = new WebData();
        data.setDecs(dataJson.getDecs());
        data.setIncs(dataJson.getIncs());
        data.setMemoryItems(dataJson.getMemoryItems());
        data.setTimingMap(dataJson.getTimingMap());
        data.setUpdateMap(dataJson.getUpdateMap());
        return data;
    }
    
    private ObjectMapper jsonObjectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(new JavaTimeModule())
                .build();
    }
    
    public static void configCurator(MyMyConfig conf) {
        if (true) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);        
            String zookeeperConnectionString = conf.getZookeeper();
            if (curatorClient == null) {
                curatorClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
                curatorClient.start();
            }
        }
    }
}

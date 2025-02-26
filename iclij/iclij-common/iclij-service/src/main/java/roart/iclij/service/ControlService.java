package roart.iclij.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigData;
import roart.common.config.ConfigTreeMap;
import roart.common.config.MarketStock;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.queue.QueueElement;
import roart.common.util.ImmutabilityUtil;
import roart.common.util.MemUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.webflux.WebFluxUtil;
import roart.db.dao.IclijDbDao;
import roart.db.spring.DbSpringAccess;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.WebData;
import roart.iclij.model.WebDataJson;
import roart.iclij.model.component.ComponentInput;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.result.model.ResultItem;
import roart.common.queueutil.QueueUtils;
import roart.model.io.IO;

// TODO not a component, many 
public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public static String id = UUID.randomUUID().toString();
    
    // Config for the core, not iclij
    public IclijConfig coremlconf;

    ObjectMapper objectMapper;

    private IclijConfig iclijConfig;

    private Function<String, Boolean> zkRegister;
    
    private IO io;
    
    private static final ObjectMapper mapper = new JsonMapper().builder().addModule(new JavaTimeModule()).build();

    public ControlService(IclijConfig iclijConfig, IO io) {
        this.iclijConfig = iclijConfig;
        this.io = io;
        this.objectMapper = jsonObjectMapper();
    }

    public IclijConfig getIclijConfig() {
        return iclijConfig;
    }

    public void setIclijConfig(IclijConfig iclijConfig) {
        this.iclijConfig = iclijConfig;
    }

    public List<String> getTasks() {
        try {
            return sendAMe(List.class, null, EurekaConstants.GETTASKS, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    public void getAndSetCoreConfig() {
        ConfigData list = getCoreConfig();
        /*
        IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(EurekaConstants.GETCONFIG, iclijConfig.getServices(), iclijConfig.getCommunications());
        ServiceResult result;// = WebFluxUtil.sendCMe(ServiceResult.class, param, EurekaConstants.GETCONFIG);
        Communication c = CommunicationFactory.get(sc.getLeft(), ServiceResult.class, EurekaConstants.GETCONFIG, objectMapper, true, true, true, sc.getRight());
        param.setWebpath(c.getReturnService());
        result = (ServiceResult) c.sendReceive(param);
         */
        //ServiceResult result = WebFluxUtil.sendCMe(ServiceResult.class, param, "http://localhost:12345/" + EurekaConstants.GETCONFIG);
        this.coremlconf = new IclijConfig(list.copy());
        Map<String, Object> map = this.coremlconf.getConfigData().getConfigValueMap();
        for (String akey : map.keySet()) {
            Object value = map.get(akey);
            //System.out.println("k " + key + " " + value + " " + value.getClass().getName());
            //System.out.println("k " + key + " " + value);
            if (value != null) {
                //System.out.println("cls " + value.getClass().getName());
            }
        }
        ConfigTreeMap map2 = coremlconf.getConfigData().getConfigTreeMap();
        print(map2, 0);

    }

    public ConfigData getCoreConfig() {
        String key = CacheConstants.CORECONFIG;
        ConfigData list = (ConfigData) MyCache.getInstance().get(key);
        if (list == null) {
            IclijServiceParam param = new IclijServiceParam();
            param.setConfigData(iclijConfig.getConfigData());
            IclijServiceResult result = sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONFIG);
            list = result.getConfigData();
            MyCache.getInstance().put(key, list);
        }
        return list;
    }

    public IclijServiceResult getCoreContent(IclijServiceParam param) {
        return sendCMe(IclijServiceResult.class, param, "core/" + EurekaConstants.GETCONTENT);
    }

    public IclijServiceResult getCoreContentGraph(IclijServiceParam param) {
        return sendCMe(IclijServiceResult.class, param, "core/" + EurekaConstants.GETCONTENTGRAPH);
    }

    public ConfigData getConfig() {
        String key = CacheConstants.CONFIG;
        ConfigData list = (ConfigData) MyCache.getInstance().get(key);
        if (list == null) {
            IclijServiceParam param = new IclijServiceParam();
            param.setConfigData(iclijConfig.getConfigData());
            IclijServiceResult result = sendAMe(IclijServiceResult.class, param, "i" + EurekaConstants.GETCONFIG, objectMapper);
            list = result.getConfigData();
            MyCache.getInstance().put(key, list);
        }
        return list;
    }

    private <T> T sendCMe(Class<T> myclass, IclijServiceParam param, String service) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        T[] result;// = WebFluxUtil.sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONFIG        
        Communication c = new CommunicationFactory().get(sc.getLeft(), myclass, service, objectMapper, true, true, true, sc.getRight(), zkRegister, io.getWebFluxUtil());
        param.setWebpath(c.getReturnService());
        result = c.sendReceive(param);
        return result[0];
    }

    private <T> T sendAMe(Class<T> myclass, IclijServiceParam param, String service, ObjectMapper objectMapper) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        T[] result;// = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONFIG        
        zkRegister = (new QueueUtils(io.getCuratorClient()))::zkRegister;
        Communication c = io.getCommunicationFactory().get(sc.getLeft(), myclass, service, objectMapper, true, true, true, sc.getRight(), zkRegister, io.getWebFluxUtil());
        param.setWebpath(c.getReturnService());
        result = c.sendReceive(param);
        return result[0];
    }

    public void send(String service, Object object, ObjectMapper objectMapper) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        String appid = System.getenv(Constants.APPID);
        if (appid != null) {
            service = service + appid; // can not handle domain, only eureka
        }
        zkRegister = (new QueueUtils(io.getCuratorClient()))::zkRegister;
        Communication c = io.getCommunicationFactory().get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight(), zkRegister, io.getWebFluxUtil());
        c.send(object);
    }

    public void send(String service, QueueElement element, IclijConfig config) {
        element.setQueue(service);
        send(service, element);
    }

    public void send(String service, Object object, IclijConfig config) {
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        String id = service + System.currentTimeMillis() + UUID.randomUUID();
        InmemoryMessage message = inmemory.send(id, object);
        send(service, message);
    }

    public void send(String service, Object object) {
        if (object == null) {
            log.error("Empty msg for {}", service);
            return;
        }
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
        IclijServiceParam param = new IclijServiceParam();
        //param.setConfigData(conf.getConfigData());
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETMARKETS);
        return result.getMarkets();    	
    }
    
    public List<MetaItem> getMetas() {
        String key = CacheConstants.METAS;
        List<MetaItem> list = (List<MetaItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETMETAS);
        list = result.getMetas();
        MyCache.getInstance().put(key, list);
        return list;
    }
    
    // (Un)used
    public Map<String, String> getStocks(String market) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        param.setMarket(market);
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETSTOCKS);
        return result.getStocks();   	
    }
    
    public List<String> getDates(String market) {
        String key = CacheConstants.DATES + coremlconf.getConfigData().getMarket() + coremlconf.getConfigData().getDate();
        List<String> list =  (List<String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
             
        Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig);
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        param.setWantMaps(true);
        param.setMarket(market);
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETDATES);
        list = PipelineUtils.getDatelist(PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.DATELIST, inmemory));      
        MyCache.getInstance().put(key, list);
        return list;
    }
   /**
     * Create result lists
 * @param uuid TODO
 * @param useMl TODO
 * @return the tabular result lists
     */

    public PipelineData[] getContent(String uuid, boolean useMl) {
        return getContent(uuid, useMl, new ArrayList<>());
    }
    
    public PipelineData[] getContent(String uuid, boolean useMl, List<String> disableList) {
        
        long[] mem0 = MemUtil.mem();
        log.info("MEM {}", MemUtil.print(mem0));

        String key = CacheConstants.CONTENT + coremlconf.getConfigData().getMarket() + coremlconf.getConfigData().getMlmarket() + coremlconf.getConfigData().getDate() + coremlconf.getConfigData().getConfigValueMap();
        PipelineData[] list = (PipelineData[]) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        IclijServiceParam param = new IclijServiceParam();
        log.info("Wants {}", iclijConfig.wantsInmemoryPipeline());
        if (iclijConfig.wantsInmemoryPipeline()) {
            log.info("InmemoryPipeline");
            param.setId(id + "/" + uuid);
            String path = "/" + Constants.STOCKSTAT + "/" + "pipeline" + "/" + "live" + "/" + id;
            try {
                io.getCuratorClient().setData().forPath(path + "/" + uuid, new byte[0]);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            String path2 = "/" + Constants.STOCKSTAT + "/" + "pipeline";
            //QueueThread.queue.add(param.getId());
        }
        param.setConfigData(coremlconf.getConfigData());
        param.setWantMaps(true);
        param.setConfList(disableList);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(coremlconf.wantMLLearn());
        neuralnetcommand.setMlclassify(coremlconf.wantMLClassify());
        neuralnetcommand.setMldynamic(coremlconf.wantMLDynamic());
        neuralnetcommand.setMlcross(coremlconf.wantMLCross());
        param.setNeuralnetcommand(neuralnetcommand);
        IclijServiceResult result;
        
        // TODO retry or queue
        if (useMl) {
            // todo send queue ml
            result = io.getWebFluxUtil().sendMMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENT);
        } else {
            // todo send queue core
            result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENT);
        }
        // todo icore queue listen
        //log.info("blblbl" + JsonUtil.convert(result).length());
        list = result.getPipelineData();
        PipelineData[] list2 = list;
        // TODO list = ImmutabilityUtil.immute(list2);
        MyCache.getInstance().put(key, list);

        long[] mem1 = MemUtil.mem();
        long[] memdiff = MemUtil.diff(mem1, mem0);
        log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
        PipelineUtils.fixPipeline(result.getPipelineData(), MarketStock.class, StockData.class);
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
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH);
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
    	IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        param.setIds(idset);
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH2);
        return result.getList();
    }

    @Deprecated
    public PipelineData[] getRerun(List<String> disableList) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        param.setWantMaps(true);
        param.setConfList(disableList);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(coremlconf.wantMLLearn());
        neuralnetcommand.setMlclassify(coremlconf.wantMLClassify());
        neuralnetcommand.setMldynamic(coremlconf.wantMLDynamic());
        neuralnetcommand.setMlcross(coremlconf.wantMLCross());
        param.setNeuralnetcommand(neuralnetcommand);
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, "/findprofit");
        return result.getPipelineData();
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
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENTSTAT);
        return result.getList();
    }

    public void dbengine(Boolean useSpark) throws Exception {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.SETCONFIG);
        getAndSetCoreConfig();
    }

    public List<ResultItem> getEvolveRecommender(boolean doSet, List<String> disableList, Map<String, Object> updateMap, Map<String, Object> scoreMap, PipelineData resultMap, Inmemory inmemory) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        param.setConfList(disableList);
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETEVOLVERECOMMENDER);
        if (doSet) {
            //conf = new MyMyConfig(result.getConfig());
            PipelineData datum = PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE, inmemory);  
            updateMap.putAll(datum.getMap(PipelineConstants.UPDATE));
            scoreMap.putAll(datum.getMap(PipelineConstants.SCORE));
            // rec with own result
            // TODO cast seriallistmap
            resultMap.putAll(datum.getMap(PipelineConstants.RESULT));
        }
        return result.getList();
        //return result.getMaps().get("update");
    }

    public List<ResultItem> getEvolveML(boolean doSet, List<String> disableList, String ml,  IclijConfig conf, Map<String, Object> updateMap, Map<String, Object> scoreMap, PipelineData resultMap) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(conf.getConfigData());
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
        // TODO retry or queue
        IclijServiceResult result = io.getWebFluxUtil().sendMMe(IclijServiceResult.class, param, EurekaConstants.GETEVOLVENN);
        if (doSet) {
            Inmemory inmemory = io.getInmemoryFactory().get(conf);
            PipelineData datum = PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE, inmemory);  
            updateMap.putAll(datum.getMap(PipelineConstants.UPDATE));
            scoreMap.putAll(datum.getMap(PipelineConstants.SCORE));
            // TODO?
            resultMap.putAll(datum.getListMap(PipelineConstants.RESULT));
            //Map<String, Object> updateMap = result.getMaps().get("update");
            //conf.getConfigValueMap().putAll(updateMap);
            //return updateMap;
        }
        return result.getList();
    }

    public List<ResultItem> getEvolveMLAsync(boolean doSet, List<String> disableList, String ml,  IclijConfig conf, Map<String, Object> updateMap, Map<String, Object> scoreMap, PipelineData resultMap) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(conf.getConfigData());
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
        // TODO retry or queue
        IclijServiceResult result = io.getWebFluxUtil().sendMMe(IclijServiceResult.class, param, EurekaConstants.GETEVOLVENN);
        return null;
    }

    public List<ResultItem> getEvolveMLAsync2(QueueElement el) {
        boolean doSet = true; List<String> disableList = null; String ml = "";  IclijConfig conf = null; Map<String, Object> updateMap = null; Map<String, Object> scoreMap = null; PipelineData resultMap = null;
        IclijServiceResult result = null;
        
        if (doSet) {
            PipelineData datum = PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE);  
            updateMap.putAll(datum.getMap(PipelineConstants.UPDATE));
            scoreMap.putAll(datum.getMap(PipelineConstants.SCORE));
            resultMap.getMap().putAll(datum.getMap(PipelineConstants.RESULT));
            //Map<String, Object> updateMap = result.getMaps().get("update");
            //conf.getConfigValueMap().putAll(updateMap);
            //return updateMap;
        }
        return result.getList();
    }

    public WebData getRun(String action, ComponentInput componentInput) {
        // TODO Auto-generated method stub
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(componentInput.getConfigData());
        param.setWebpath(EurekaConstants.ACTION + "/" + action);
        param.setOffset(componentInput.getLoopoffset());
        IclijServiceResult result = io.getWebFluxUtil().sendAMe(IclijServiceResult.class, param, param.getWebpath(), objectMapper);

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
        param.setConfigData(componentInput.getConfigData());
        param.setWebpath(EurekaConstants.GETVERIFY);
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
    
    public IO getIo() {
        return io;
    }
    
    
}

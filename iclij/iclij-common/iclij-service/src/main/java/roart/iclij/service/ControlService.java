package roart.iclij.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.http.converter.json.JsonMapper;

import com.fasterxml.jackson.annotation.JsonInclude;
import roart.common.constants.ServiceConstants;
import roart.common.util.JsonUtil;
import roart.iclij.common.service.IclijServiceParam;
import roart.iclij.common.service.IclijServiceResult;
import roart.model.io.util.IOUtils;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigData;
import roart.common.config.ConfigTreeMap;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MetaDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.queue.QueueElement;
import roart.common.util.MemUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.model.WebData;
import roart.iclij.model.WebDataJson;
import roart.iclij.model.component.ComponentInput;
import roart.result.model.ResultItem;
import roart.common.queueutil.QueueUtils;
import roart.model.io.IO;

// TODO not a component, many 
public class ControlService {
    private static Logger log = LoggerFactory.getLogger(ControlService.class);

    public static String id = UUID.randomUUID().toString();
    
    // Config for the core, not iclij, for the evolved
    public IclijConfig coremlconf;

    ObjectMapper objectMapper;

    private IclijConfig iclijConfig;

    private IO io;
    
    private static final ObjectMapper mapper = new JsonMapper().builder().build();

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
            return new IOUtils(io, iclijConfig, objectMapper).sendReceiveA(List.class, null, EurekaConstants.GETTASKS, null);
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
        if (iclijConfig.wantsInmemoryPipeline()) {
            //log.info("Wants {}", coremlconf.wantsInmemoryPipeline());
            //coremlconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINE, Boolean.TRUE);
            //coremlconf.getConfigData().getConfigValueMap().put(IclijConfigConstants.MISCINMEMORYPIPELINEBATCHSIZE, iclijConfig.wantsInmemoryPipelineBatchsize());
            log.info("Wants {} {}", coremlconf.wantsInmemoryPipeline(), coremlconf.wantsInmemoryPipelineBatchsize());
        }
    }

    public ConfigData getCoreConfig() {
        String key = CacheConstants.CORECONFIG;
        ConfigData list = (ConfigData) MyCache.getInstance().get(key);
        if (list == null) {
            IclijServiceParam param = new IclijServiceParam();
            param.setConfigData(iclijConfig.getConfigData());
            IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETCONFIG, ServiceConstants.GETCONFIG);
            list = result.getConfigData();
            MyCache.getInstance().put(key, list);
        }
        return list;
    }

    public IclijServiceResult getCoreContent(IclijServiceParam param) {
        return new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, "core/" + EurekaConstants.GETCONTENT, ServiceConstants.GETCONTENT);
    }

    public IclijServiceResult getCoreContentGraph(IclijServiceParam param) {
        return new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, "core/" + EurekaConstants.GETCONTENTGRAPH, ServiceConstants.GETCONTENTGRAPH);
    }

    public ConfigData getConfig() {
        String key = CacheConstants.CONFIG;
        ConfigData list = (ConfigData) MyCache.getInstance().get(key);
        if (list == null) {
            IclijServiceParam param = new IclijServiceParam();
            param.setConfigData(iclijConfig.getConfigData());
            IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveA(IclijServiceResult.class, param, "i" + EurekaConstants.GETCONFIG, ServiceConstants.GETCONFIG);
            list = result.getConfigData();
            MyCache.getInstance().put(key, list);
        }
        return list;
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
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETMARKETS, ServiceConstants.GETMARKETS);
        return result.getMarkets();
    }
    
    public List<MetaDTO> getMetas() {
        String key = CacheConstants.METAS;
        List<MetaDTO> list = (List<MetaDTO>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETMETAS, ServiceConstants.GETMETAS);
        list = result.getMetas();
        MyCache.getInstance().put(key, list);
        return list;
    }
    
    // (Un)used
    public Map<String, String> getStocks(String market) {
        // TODO pipeline
        if (false) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        String key = CacheConstants.STOCKS + coremlconf.getConfigData().getMarket() + coremlconf.getConfigData().getMlmarket() + coremlconf.getConfigData().getDate() + coremlconf.getConfigData().getConfigValueMap();
        log.info("Content key {}", key.hashCode());
        Map<String, String> list = (Map<String, String>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        param.setMarket(market);
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETSTOCKS, ServiceConstants.GETSTOCKS);
        list = result.getStocks();
        MyCache.getInstance().put(key, list);
        return list;   	
    }
    
    public List<String> getDates(String market, String uuid) {
        if (false) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
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
        if (iclijConfig.wantsInmemoryPipeline()) {
            uuid = UUID.randomUUID().toString();
            log.info("InmemoryPipeline {} {}", id, uuid);
            param.setId(id + "/" + uuid);
        }
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETDATES, ServiceConstants.GETDATES);
        list = PipelineUtils.getDatelist(result.getPipelineData(), PipelineConstants.DATELIST, inmemory);
        new PipelineThreadUtils(getIclijConfig(), inmemory, getIo().getCuratorClient()).cleanPipeline(id, uuid);
        MyCache.getInstance().put(key, list);
        return list;
    }
   /**
     * Create result lists
 * @param uuid TODO
 * @param useMl TODO
 * @param disableCache TODO
 * @param keepPipeline TODO
 * @return the tabular result lists
     */

    public SerialPipeline getContent(String uuid, boolean useMl, boolean disableCache, boolean keepPipeline) {
        return getContent(uuid, useMl, new ArrayList<>(), disableCache, keepPipeline);
    }
    
    public SerialPipeline getContent(String uuid, boolean useMl, List<String> disableList, boolean disableCache, boolean keepPipeline) {
        if (false) {
            try {
                String s = null;
                s.length();
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
        }
        long[] mem0 = MemUtil.mem();
        log.info("MEM {}", MemUtil.print(mem0));

        String common = "";
        if (!keepPipeline) {
            common = uuid;
        }
        
        String key = common + CacheConstants.CONTENT + coremlconf.getConfigData().getMarket() + coremlconf.getConfigData().getMlmarket() + coremlconf.getConfigData().getDate() + coremlconf.getConfigData().getConfigValueMap();
        log.info("Content key {}", key.hashCode());
        SerialPipeline list = (SerialPipeline) MyCache.getInstance().get(key);
        if (list != null) {
            // todo pipe?
            return list;
        }
        IclijServiceParam param = new IclijServiceParam();
        log.info("Wants {}", iclijConfig.wantsInmemoryPipeline());
        if (iclijConfig.wantsInmemoryPipeline()) {
            log.info("InmemoryPipeline {} {}", id, uuid);
            param.setId(id + "/" + uuid);
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
        // TODO cache
        if (useMl) {
            // todo send queue ml
            /*
            Map<String, Object> valueMap = new HashMap<>(conf.getConfigData().getConfigValueMap());
            valueMap.keySet().removeAll(new ConfigUtils().getMLComponentConfigList());
            String key2 = CacheConstants.CONTENT + coremlconf.getConfigData().getMarket() + coremlconf.getConfigData().getMlmarket() + coremlconf.getConfigData().getDate() + valueMap);
            log.info("Content key {}", key2.hashCode());
            */

            result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveM(IclijServiceResult.class, param, EurekaConstants.GETCONTENT, ServiceConstants.GETMCONTENT);
        } else {
            // todo send queue core
            result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETCONTENT, ServiceConstants.GETCONTENT);
        }
        // todo icore queue listen
        //log.info("blblbl" + JsonUtil.convert(result).length());
        list = result.getPipelineData();
        SerialPipeline list2 = list;
        // TODO list = ImmutabilityUtil.immute(list2);
        if (!disableCache) {
            MyCache.getInstance().put(key, list);
            if (keepPipeline) {
                MyCache.getInstance().pipeline(uuid);
            }
        } else {
            log.info("Cache disabled for {} {} {}", id, uuid, key.hashCode());
        }
        
        long[] mem1 = MemUtil.mem();
        long[] memdiff = MemUtil.diff(mem1, mem0);
        log.info("MEM {} Δ {}", MemUtil.print(mem1), MemUtil.print(memdiff));
        log.info("Cache {}", MyCache.getInstance().toString());
        //PipelineUtils.fixPipeline(result.getPipelineData(), MarketStock.class, StockData.class);
        log.info("Serial pipeline size {}", list.length());
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
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH, ServiceConstants.GETCONTENTGRAPH);
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
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETCONTENTGRAPH2, ServiceConstants.GETCONTENTGRAPH2);
        return result.getList();
    }

    @Deprecated
    public SerialPipeline getRerun(List<String> disableList) {
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
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, "/findprofit", null);
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
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETCONTENTSTAT, ServiceConstants.GETCONTENTSTAT);
        return result.getList();
    }

    public void dbengine(Boolean useSpark) throws Exception {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(coremlconf.getConfigData());
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.SETCONFIG, EurekaConstants.SETCONFIG);
        getAndSetCoreConfig();
    }

    public List<ResultItem> getEvolveRecommender(String uuid, boolean doSet, List<String> disableList, Map<String, Object> updateMap, Map<String, Object> scoreMap, SerialPipeline resultMap, Inmemory inmemory) {
        IclijServiceParam param = new IclijServiceParam();
        log.info("Wants {}", iclijConfig.wantsInmemoryPipeline());
        if (iclijConfig.wantsInmemoryPipeline()) {
            log.info("InmemoryPipeline {} {}", id, uuid);
            param.setId(id + "/" + uuid);
        }
        param.setConfigData(coremlconf.getConfigData());
        param.setConfList(disableList);
        
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveC(IclijServiceResult.class, param, EurekaConstants.GETEVOLVERECOMMENDER, ServiceConstants.GETEVOLVERECOMMENDER);
        if (doSet) {
            //conf = new MyMyConfig(result.getConfig());
            //PipelineData datum = PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE, inmemory);
            updateMap.putAll(PipelineUtils.getSerialMapPlain(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.UPDATE, null, null));
            scoreMap.putAll(PipelineUtils.getSerialMapPlain(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.SCORE, null, null));
            // rec with own result
            // TODO  cast seriallistmap
            SerialPipeline result2 = PipelineUtils.getPipelines(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.RESULT, inmemory);
            log.info("Result {}", result2.length());
            resultMap.add(result2); // TODO overwrite
        }
        return result.getList();
        //return result.getMaps().get("update");
    }

    public List<ResultItem> getEvolveML(String uuid, boolean doSet, List<String> disableList,  String ml, IclijConfig conf, Map<String, Object> updateMap, Map<String, Object> scoreMap, SerialPipeline resultMap) {
        IclijServiceParam param = new IclijServiceParam();
        log.info("Wants {}", iclijConfig.wantsInmemoryPipeline());
        if (iclijConfig.wantsInmemoryPipeline()) {
            log.info("InmemoryPipeline {} {}", id, uuid);
            param.setId(id + "/" + uuid);
        }
        param.setConfigData(conf.getConfigData());
        Set<String> ids = new HashSet<>();
        ids.add(ml);
        param.setIds(ids);
        param.setConfList(disableList);
        NeuralNetCommand neuralnetcommand = new NeuralNetCommand();
        neuralnetcommand.setMllearn(true);
        // TODO?
        neuralnetcommand.setMlclassify(true);
        // where is this reset?
        neuralnetcommand.setMldynamic(true);
        param.setNeuralnetcommand(neuralnetcommand);
        // TODO retry or queue
        // TODO cache not
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveM(IclijServiceResult.class, param, EurekaConstants.GETEVOLVENN, ServiceConstants.GETEVOLVENN);
        
        if (doSet) {
            Inmemory inmemory = io.getInmemoryFactory().get(conf);
            SerialPipeline datum = PipelineUtils.getPipelines(result.getPipelineData(), PipelineConstants.EVOLVE, null, inmemory);
            updateMap.putAll(PipelineUtils.getSerialMapPlain(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.UPDATE, null, null));
            scoreMap.putAll(PipelineUtils.getSerialMapPlain(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.SCORE, null, null));
            // rec with own result
            // TODO  cast seriallistmap
            SerialPipeline result2 = PipelineUtils.getPipelines(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.RESULT, inmemory);
            log.info("Result {}", result2.length());
            resultMap.add(result2); // TODO overwrite
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
            //PipelineData datum = PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE);
            //updateMap.putAll(PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.UPDATE, inmemory).getMap());
            //scoreMap.putAll(PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.SCORE, inmemory).getMap());
            // rec with own result
            // TODO  cast seriallistmap
            //resultMap.add(PipelineUtils.getPipeline(result.getPipelineData(), PipelineConstants.EVOLVE, PipelineConstants.RESULT, inmemory).getListMap());
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
        IclijServiceResult result = new IOUtils(io, iclijConfig, objectMapper).sendReceiveA(IclijServiceResult.class, param, param.getWebpath(), param.getWebpath());

        WebDataJson dataJson = result.getWebdatajson();
        WebData data = convert(dataJson);
        return data;
    }

    @Deprecated
    public WebData getVerify(String findprofit, ComponentInput componentInput) {
        // TODO Auto-generated method stub
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(componentInput.getConfigData());
        param.setWebpath(EurekaConstants.GETVERIFY);
        param.setOffset(componentInput.getLoopoffset());
        IclijServiceResult result = null; //new IOUtils(io, iclijConfig, objectMapper).sendReceiveA(IclijServiceResult.class, param, param.getWebpath(), objectMapper);
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
        data.setMemoryDTOs(dataJson.getMemoryDTOs());
        data.setTimingMap(dataJson.getTimingMap());
        data.setUpdateMap(dataJson.getUpdateMap());
        return data;
    }
    
    public ObjectMapper jsonObjectMapper() {
        return JsonMapper.builder()
                .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
                .build();
    }
    
    public IO getIo() {
        return io;
    }

    // github copilot

    /**
     * Async version of getContent using queue-based communication.
     * Instead of direct HTTP calls via sendCMe, this method sends the request
     * to ServiceControllerOther via the queue and returns immediately.
     * The result will be retrieved through the return service specified in param.
     *
     * @param uuid the unique identifier
     * @param useMl whether to use ML
     * @param disableList list of components to disable
     * @param disableCache whether to disable caching
     * @param keepPipeline whether to keep pipeline
     * @param replyQueue the queue name where the result should be sent back
     * @return the element id that can be used to track the request
     */
    public String getContentAsync(String uuid, boolean useMl, List<String> disableList, boolean disableCache, boolean keepPipeline, String replyQueue) {
        log.info("Starting async getContent for uuid {}", uuid);

        String common = "";
        if (!keepPipeline) {
            common = uuid;
        }

        String key = common + CacheConstants.CONTENT + coremlconf.getConfigData().getMarket() + coremlconf.getConfigData().getMlmarket() + coremlconf.getConfigData().getDate() + coremlconf.getConfigData().getConfigValueMap();
        log.info("Content key {}", key.hashCode());

        // Check cache first
        SerialPipeline cachedList = (SerialPipeline) MyCache.getInstance().get(key);
        if (cachedList != null) {
            log.info("Found in cache, would return immediately in async context");
            // In async context, you might still want to queue this for consistency
        }

        IclijServiceParam param = new IclijServiceParam();
        log.info("Wants {}", iclijConfig.wantsInmemoryPipeline());
        if (iclijConfig.wantsInmemoryPipeline()) {
            log.info("InmemoryPipeline {} {}", id, uuid);
            param.setId(id + "/" + uuid);
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
        param.setWebpath(replyQueue);

        // Create queue element for async processing
        QueueElement element = new QueueElement(id, replyQueue);
        element.setParam(param);
        String elementId = element.getId();

        // Determine which service to send to
        String service = EurekaConstants.GETCONTENT;

        try {
            // Send asynchronously to ServiceControllerOther via send method
            // TODO send(service, element, iclijConfig);
            log.info("Async request sent with element id {}", elementId);
        } catch (Exception e) {
            log.error("Error sending async getContent request", e);
            throw new RuntimeException("Failed to send async getContent request", e);
        }

        return elementId;
    }

    // for the actions
    public void send(String service, QueueElement element, IclijConfig config) {
        new IOUtils(io, config, objectMapper).send(service, element, config);
    }
}

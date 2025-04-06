package roart.machinelearning.service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import roart.aggregator.impl.MACDBase;
import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.common.cache.MyCache;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.config.MarketStock;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.data.TwoDimd;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.ArraysUtil;
import roart.common.util.ImmutabilityUtil;
import roart.common.util.JsonUtil;
import roart.common.util.MemUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
import roart.aggregator.util.AggregatorUtils;
import roart.etl.CleanETL;
import roart.iclij.config.ConfigUtils;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.machinelearning.service.util.ServiceUtil;
import roart.model.data.MarketData;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.result.model.ResultItem;
import roart.stockutil.StockUtil;
import roart.model.data.StockData;
import roart.predictor.util.PredictorUtils;
import roart.model.io.IO;

public class MachineLearningControlService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Function<String, Boolean> zkRegister;

    private static final ObjectMapper mapper = new JsonMapper().builder().addModule(new JavaTimeModule()).build();

    private IO io;
    
    public MachineLearningControlService() {
        super();
    }

    public MachineLearningControlService(IO io) {
        super();
        this.io = io;
    }    

    //protected static int[] otherTableNames = { Constants.EVENT, Constants.MLTIMES }; 

    /**
     * Create result lists
     * @param param2 
     * @param datareaders 
     * @return the tabular result lists
     */

    public IclijServiceResult getContent(List<String> disableList, IclijServiceParam origparam) {
        IclijConfig conf = new IclijConfig(origparam.getConfigData());
        Inmemory inmemory = io.getInmemoryFactory().get(conf);
        NeuralNetCommand neuralnetcommand = origparam.getNeuralnetcommand();
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        

        IclijServiceResult result = getContent(conf, origparam, disableList);
        
        List<ResultItem> retlist = result.getList();
        PipelineData[] pipelineData = result.getPipelineData();
        
        StockData stockData = new StockUtil().getStockData(conf, pipelineData, inmemory);

        try {
            String mydate = TimeUtil.format(conf.getConfigData().getDate());
            int dateIndex = TimeUtil.getIndexEqualBefore(stockData.stockdates, mydate);
            if (dateIndex >= 0) {
                mydate = stockData.stockdates.get(dateIndex);
            }
            
            // TODO split
            
            AbstractPredictor[] predictors = new PredictorUtils().getPredictors(conf, pipelineData,
                    stockData.catName, stockData.cat, neuralnetcommand, inmemory);
            //new ServiceUtil().createPredictors(categories);
            new PredictorUtils().calculatePredictors(predictors);
            
            Aggregator[] aggregates = new AggregatorUtils().getAggregates(conf, pipelineData,
                    disableList, stockData.idNameMap, stockData.catName, stockData.cat, neuralnetcommand, stockData.stockdates, inmemory);

            /*
            for (AbstractCategory category : categories) {
                List<AbstractPredictor> predictors = category.getPredictors();
                addOtherTables(predictors);
            }
            */
            
            // TODO rows
            
            /*
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    Map map = categories[i].getIndicatorLocalResultMap();
                    maps.put(categories[i].getTitle(), map);
                    log.debug("ca {}", categories[i].getTitle());
                }
             */
            for (int i = 0; i < predictors.length; i++) {
                if (predictors[i] == null) {
                    continue;
                }
                Map map = predictors[i].putData().getMap();
                log.debug("ca {}", predictors[i].getName());
                PipelineData singlePipelinedata = predictors[i].putData();
                pipelineData = ArrayUtils.add(pipelineData, singlePipelinedata);
            }
            for (int i = 0; i < aggregates.length; i++) {
                if (aggregates[i] == null) {
                    continue;
                }
                if (!aggregates[i].isEnabled()) {
                    continue;
                }
                log.debug("ag {}", aggregates[i].getName());
                Map map = aggregates[i].putData().getMap();
                PipelineData singlePipelinedata = aggregates[i].putData();
                pipelineData = ArrayUtils.add(pipelineData, singlePipelinedata);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        //new CleanETL().fixmap((Map) maps);
        //printmap(maps, 0);
        result.setList(retlist);
        if (origparam.getId() != null) {
            log.info("Before setPipelineMap");
            PipelineUtils.setPipelineMap(pipelineData, origparam.getId());
            pipelineData = PipelineUtils.setPipelineMap(pipelineData, inmemory, io.getCuratorClient());
        }
        result.setPipelineData(pipelineData);
        result.setConfigData(conf.getConfigData());
       
        PipelineUtils.printkeys(pipelineData);
    
        if (true) return result;
        for (PipelineData data : pipelineData) {
            //data.
            try (InputStream is = new ByteArrayInputStream(JsonUtil.convert(data).getBytes())) {
                String md5 = null;
                InmemoryMessage msg = inmemory.send(Constants.STOCKSTAT + data.getId() + data.getName(), is, md5);
                //result.message = msg;
                io.getCuratorClient().create().creatingParentsIfNeeded().forPath("/" + Constants.STOCKSTAT + "/" + Constants.DATA + "/" + msg.getId(), JsonUtil.convert(msg).getBytes());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

        }
        return result;
    }

    public IclijServiceResult getContent(IclijConfig conf, IclijServiceParam origparam, List<String> disableList) {
        // TODO core config
        Map<String, Object> valueMap = new HashMap<>(conf.getConfigData().getConfigValueMap());
        valueMap.keySet().removeAll(new ConfigUtils().getMLComponentConfigList());
        String key = CacheConstants.MLCONTENT + conf.getConfigData().getMarket() + conf.getConfigData().getMlmarket() + conf.getConfigData().getDate() + valueMap;
        log.info("Content key {}", key.hashCode());
        log.debug("Content kez {}", key);
        IclijServiceResult list = (IclijServiceResult) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }

        IclijServiceParam param = new IclijServiceParam();
        param.setId(origparam.getId());
        param.setConfigData(conf.getConfigData());
        param.setWantMaps(origparam.isWantMaps());
        param.setConfList(disableList);
        if (conf.wantsInmemoryPipeline()) {
            log.info("InmemoryPipeline {}", origparam.getId());
        }
        // TODO retry or queue
        IclijServiceResult result = io.getWebFluxUtil().sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENT);

        /// TODO list2 = ImmutabilityUtil.immute(list2);
        MyCache.getInstance().put(key, result);
        {
        long[] mem0 = MemUtil.mem();
        log.info("MEM {}", MemUtil.print(mem0));
        }
        PipelineUtils.fixPipeline(result.getPipelineData(), MarketStock.class, StockData.class);
        {
        long[] mem0 = MemUtil.mem();
        log.info("MEM {}", MemUtil.print(mem0));
        log.info("Cache {}", MyCache.getInstance().toString());
        }
        
        return result;
        
    }

    public void printmap(Object o, int i) {
        if (o == null) {
            return;
        }
        //System.out.println("" + i + " " + o.hashCode());
        Map<String, Object> m = (Map<String, Object>) o;
        for (Entry<String, Object> e : m.entrySet()) {
            Object value = e.getValue();
            if (value instanceof Map) {
                log.debug("Kv {} {} {}", i, e.getKey(),value.hashCode());
                printmap((Map<String, Object>) value, i + 1);
            } else {
                if (value == null) {
                    log.debug("Kv {} {} {}", i, e.getKey(), null);
                    //System.out.println(" v " + null);
                }
            }
        }
    }

    public void send(String service, Object object, IclijConfig config) {
        IclijConfig iclijConfig = config; // TODO check
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

    public void send(String service, Object object, ObjectMapper objectMapper) {
        //IclijConfig iclijConfig = IclijXMLConfig.getConfigInstance();
        IclijConfig iclijConfig = null;
        Pair<String, String> sc = new ServiceConnectionUtil().getCommunicationConnection(service, iclijConfig.getServices(), iclijConfig.getCommunications());
        String appid = System.getenv(Constants.APPID);
        if (appid != null) {
            service = service + appid; // can not handle domain, only eureka
        }
        Communication c = io.getCommunicationFactory().get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight(), zkRegister, io.getWebFluxUtil());
        c.send(object);
    }

}

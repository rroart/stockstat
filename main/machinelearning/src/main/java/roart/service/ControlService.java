package roart.service;

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
import roart.etl.CleanETL;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.model.data.MarketData;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.result.model.ResultItem;
import roart.service.util.ServiceUtil;
import roart.model.data.StockData;

public class ControlService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private Function<String, Boolean> zkRegister;

    private static final ObjectMapper mapper = new JsonMapper().builder().addModule(new JavaTimeModule()).build();

    public static CuratorFramework curatorClient;

    public ControlService() {
        super();
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
        NeuralNetCommand neuralnetcommand = origparam.getNeuralnetcommand();
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        

        IclijServiceResult result = getContent(conf, origparam, disableList);
        
        List<ResultItem> retlist = result.getList();
        PipelineData[] pipelineData = result.getPipelineData();
        
        StockData stockData = getStockData(conf, pipelineData);

        try {
            String mydate = TimeUtil.format(conf.getConfigData().getDate());
            int dateIndex = TimeUtil.getIndexEqualBefore(stockData.stockdates, mydate);
            if (dateIndex >= 0) {
                mydate = stockData.stockdates.get(dateIndex);
            }
            
            AbstractPredictor[] predictors = new ServiceUtil().getPredictors(conf, pipelineData,
                    stockData.catName, stockData.cat, neuralnetcommand);
            //new ServiceUtil().createPredictors(categories);
            new ServiceUtil().calculatePredictors(predictors);
            
            Aggregator[] aggregates = getAggregates(conf, pipelineData,
                    disableList, stockData.idNameMap, stockData.catName, stockData.cat, neuralnetcommand, stockData.stockdates);

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
        result.setPipelineData(pipelineData);
        result.setConfigData(conf.getConfigData());
        
        Inmemory inmemory = InmemoryFactory.get(conf.getInmemoryServer(), conf.getInmemoryHazelcast(), conf.getInmemoryRedis());

        if (true) return result;
        for (PipelineData data : pipelineData) {
            //data.
            try (InputStream is = new ByteArrayInputStream(JsonUtil.convert(data).getBytes())) {
                String md5 = null;
                InmemoryMessage msg = inmemory.send(Constants.STOCKSTAT + data.getId() + data.getName(), is, md5);
                //result.message = msg;
                curatorClient.create().creatingParentsIfNeeded().forPath("/" + Constants.STOCKSTAT + "/" + Constants.DATA + "/" + msg.getId(), JsonUtil.convert(msg).getBytes());
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }

        }
        return result;
    }

    public IclijServiceResult getContent(IclijConfig conf, IclijServiceParam origparam, List<String> disableList) {
        String key = CacheConstants.CONTENT + conf.getConfigData().getMarket() + conf.getConfigData().getMlmarket() + conf.getConfigData().getDate() + conf.getConfigData().getConfigValueMap();
        IclijServiceResult list = (IclijServiceResult) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }

        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(conf.getConfigData());
        param.setWantMaps(origparam.isWantMaps());
        param.setConfList(disableList);
        // TODO retry or queue
        IclijServiceResult result = WebFluxUtil.sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENT);

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

    private Aggregator[] getAggregates(IclijConfig conf, PipelineData[] pipelineData,
            List<String> disableList,
            Map<String, String> idNameMap,
            String catName, Integer cat, NeuralNetCommand neuralnetcommand, List<String> stockDates) throws Exception {
        Aggregator[] aggregates = new Aggregator[10];
        aggregates[0] = new MACDBase(conf, catName, catName, cat, pipelineData, stockDates);
        //aggregates[1] = new AggregatorRecommenderIndicator(conf, catName, marketdatamap, categories, pipelineData, disableList);
        //aggregates[2] = new RecommenderRSI(conf, catName, marketdatamap, categories);
        aggregates[3] = new MLMACD(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[4] = new MLRSI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[5] = new MLATR(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[6] = new MLCCI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[7] = new MLSTOCH(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[8] = new MLMulti(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand, stockDates);
        aggregates[9] = new MLIndicator(conf, catName, catName, cat, pipelineData, neuralnetcommand, stockDates);
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.MACHINELEARNING));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.AGGREGATORSMLMACD));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORSMACD));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORSRSI));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORS));
        return aggregates;
    }

    public static void configCurator(IclijConfig conf) {
        if (true) {
            RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);        
            String zookeeperConnectionString = conf.getZookeeper();
            if (curatorClient == null) {
                curatorClient = CuratorFrameworkFactory.newClient(zookeeperConnectionString, retryPolicy);
                curatorClient.start();
            }
        }
    }
    
    public StockData getStockData(IclijConfig conf, PipelineData[] pipelineData) {
        StockData stockData = new StockData();
        PipelineData pipelineDatum = PipelineUtils.getPipeline(pipelineData, PipelineConstants.META);
        stockData.cat = PipelineUtils.getWantedcat(pipelineDatum);
        stockData.catName = PipelineUtils.getMetaCat(pipelineDatum);
        stockData.idNameMap = PipelineUtils.getNamemap(pipelineDatum);
        stockData.stockdates = PipelineUtils.getDatelist(pipelineDatum);
        return stockData;
    }

    public void send(String service, Object object, IclijConfig config) {
        IclijConfig iclijConfig = config; // TODO check
        Inmemory inmemory = InmemoryFactory.get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
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
        Communication c = CommunicationFactory.get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight(), zkRegister);
        c.send(object);
    }

}

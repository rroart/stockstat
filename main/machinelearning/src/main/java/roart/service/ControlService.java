package roart.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregator.impl.MACDBase;
import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.ArraysUtil;
import roart.common.util.ImmutabilityUtil;
import roart.common.util.PipelineUtils;
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
                    disableList, stockData.idNameMap, stockData.catName, stockData.cat, neuralnetcommand);

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
        return result;
    }

    public static IclijServiceResult getContent(IclijConfig conf, IclijServiceParam origparam, List<String> disableList) {
        String key = CacheConstants.CONTENT + conf.getConfigData().getMarket() + conf.getConfigData().getMlmarket() + conf.getConfigData().getDate() + conf.getConfigData().getConfigValueMap();
        IclijServiceResult list = (IclijServiceResult) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }

        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(conf.getConfigData());
        param.setWantMaps(origparam.isWantMaps());
        param.setConfList(disableList);
        IclijServiceResult result = WebFluxUtil.sendCMe(IclijServiceResult.class, param, EurekaConstants.GETCONTENT);

        /// TODO list2 = ImmutabilityUtil.immute(list2);
        MyCache.getInstance().put(key, result);

        fixPipeline(result.getPipelineData());
        
        return result;
        
    }
    
    private static void fixPipeline(PipelineData[] pipelineData) {
        for (PipelineData data : pipelineData) {
            for (Entry<String, Object> entry : data.getMap().entrySet()) {
                Object newData = transformListList(entry.getValue());
                data.getMap().put(entry.getKey(), newData);
            }
        }
    }



    private static Object transform(Object data) {
        if (data instanceof List list) {
            return list.stream().map(e -> transform(e)).toArray();
        }
        return data;
    }

    private static Object transformListList(Object data) {
        if (data instanceof List list) {
            if (!list.isEmpty() && list.get(0) instanceof List) {
                return ArraysUtil.convert((List<List<Double>>) data);
            }
        }
        return data;
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
                System.out.println("" + i + " " + e.getKey() + " " + value.hashCode());
                printmap((Map<String, Object>) value, i + 1);
            } else {
                if (value == null) {
                    System.out.println("" + i + " " + e.getKey() + " " + null);
                    //System.out.println(" v " + null);
                }
            }
        }
    }

    private Aggregator[] getAggregates(IclijConfig conf, PipelineData[] pipelineData,
            List<String> disableList,
            Map<String, String> idNameMap,
            String catName, Integer cat, NeuralNetCommand neuralnetcommand) throws Exception {
        Aggregator[] aggregates = new Aggregator[10];
        aggregates[0] = new MACDBase(conf, catName, catName, cat, pipelineData);
        //aggregates[1] = new AggregatorRecommenderIndicator(conf, catName, marketdatamap, categories, pipelineData, disableList);
        //aggregates[2] = new RecommenderRSI(conf, catName, marketdatamap, categories);
        aggregates[3] = new MLMACD(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[4] = new MLRSI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[5] = new MLATR(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[6] = new MLCCI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[7] = new MLSTOCH(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[8] = new MLMulti(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[9] = new MLIndicator(conf, catName, catName, cat, pipelineData, neuralnetcommand);
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
        stockData.cat = (Integer) pipelineDatum.get(PipelineConstants.WANTEDCAT);
        stockData.catName = (String) pipelineDatum.get(PipelineConstants.CATEGORY);
        stockData.idNameMap = (Map<String, String>) pipelineDatum.get(PipelineConstants.NAME);
        stockData.stockdates = (List<String>) pipelineDatum.get(PipelineConstants.DATELIST);
         return stockData;
    }

}

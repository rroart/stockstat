package roart.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregator.impl.AggregatorRecommenderIndicator;
import roart.aggregator.impl.MACDBase;
import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.aggregator.impl.RecommenderRSI;
import roart.category.AbstractCategory;
import roart.category.impl.CategoryIndex;
import roart.category.impl.CategoryPeriod;
import roart.category.impl.CategoryPrice;
import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.PipelineUtils;
import roart.common.util.TimeUtil;
import roart.db.dao.DbDao;
import roart.etl.CleanETL;
import roart.etl.PeriodDataETL;
import roart.etl.db.Extract;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.pipeline.impl.DataReader;
import roart.pipeline.impl.ExtraReader;
import roart.result.model.GUISize;
import roart.service.util.ServiceUtil;
import roart.stockutil.StockUtil;

public class ControlService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public static CuratorFramework curatorClient;

    public List<String> getMarkets() {
        try {
            return dbDao.getMarkets();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    public List<MetaItem> getMetas() {
        try {
            return dbDao.getMetas();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    private DbDao dbDao;

    public ControlService(DbDao dbDao) {
        super();
        this.dbDao = dbDao;
    }

    public Map<String, String> getStocks(String market, IclijConfig conf) {
        try {
            Map<String, String> stockMap = new HashMap<>();
            List<StockItem> stocks = dbDao.getAll(market, conf);
            stocks.remove(null);
            for (StockItem stock : stocks) {
                String name = stock.getName();
                if (name != null && !name.isEmpty() && !name.isBlank()) {
                    stockMap.put(stock.getId(), stock.getName());
                }
            }
            return stockMap;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return null;
        }
    }

    //protected static int[] otherTableNames = { Constants.EVENT, Constants.MLTIMES }; 

    /**
     * Create result lists
     * @param maps 
     * @param neuralnetcommand TODO
     * @param datareaders 
     * @return the tabular result lists
     */

    public List getContent(IclijConfig conf, Map<String, Map<String, Object>> maps, List<String> disableList, NeuralNetCommand neuralnetcommand, PipelineData[] pipelineData) {
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        /*
        StockData stockData = new Extract(dbDao).getStockData(conf);
        if (stockData == null) {
            return new ArrayList<>();
        }
        */
        PipelineData pipelineDatum = PipelineUtils.getPipeline(pipelineData, PipelineConstants.META);
        MyStockData stockData = getStockData(conf, "market", pipelineData);

        stockData.cat = (Integer) pipelineDatum.get(PipelineConstants.WANTEDCAT);
        try {
            /*
            Pipeline[] datareaders = new ServiceUtil().getDataReaders(conf, stockData.periodText,
                    stockData.marketdatamap, stockData, dbDao);
*/
            
            String mydate = TimeUtil.format(conf.getConfigData().getDate());
            int dateIndex = TimeUtil.getIndexEqualBefore(stockData.stockdates, mydate);
            if (dateIndex >= 0) {
                mydate = stockData.stockdates.get(dateIndex);
            }
            List<StockItem> dayStocks = stockData.stockdatemap.get(mydate);
            
            AbstractCategory[] categories = new ServiceUtil().getCategories(conf, dayStocks,
                    stockData.periodText, pipelineData);
            AbstractPredictor[] predictors = new ServiceUtil().getPredictors(conf, stockData.marketdatamap,
                    pipelineData, categories, neuralnetcommand);
            //new ServiceUtil().createPredictors(categories);
            new ServiceUtil().calculatePredictors(predictors);
            
            Aggregator[] aggregates = getAggregates(conf, categories,
                    pipelineData, disableList, stockData.idNameMap, stockData.catName, stockData.cat, neuralnetcommand);

            /*
            for (AbstractCategory category : categories) {
                List<AbstractPredictor> predictors = category.getPredictors();
                addOtherTables(predictors);
            }
            */
            if (maps != null) {
                Map<String, Object> aMap = new HashMap<>();
                aMap.put(PipelineConstants.WANTEDCAT, stockData.cat);
                maps.put(PipelineConstants.META, aMap);
                /*
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    Map map = categories[i].getIndicatorLocalResultMap();
                    maps.put(categories[i].getTitle(), map);
                    log.debug("ca {}", categories[i].getTitle());
                }
                */
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    if (predictors[i] == null) {
                        continue;
                    }
                    Map map = predictors[i].putData().getMap();
                    maps.put(predictors[i].getName(), map);
                    log.debug("ca {}", predictors[i].getName());
                    PipelineData singlePipelinedata = predictors[i].putData();
                    pipelineData = ArrayUtils.add(pipelineData, singlePipelinedata);
                }
                for (int i = 0; i < aggregates.length; i++) {
                    if (!aggregates[i].isEnabled()) {
                        continue;
                    }
                    log.debug("ag {}", aggregates[i].getName());
                    Map map = aggregates[i].putData().getMap();
                    maps.put(aggregates[i].getName(), map);
                    PipelineData singlePipelinedata = aggregates[i].putData();
                    pipelineData = ArrayUtils.add(pipelineData, singlePipelinedata);
                }
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        new CleanETL().fixmap((Map) maps);
        printmap(maps, 0);
        return new ArrayList<>();
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

    private Aggregator[] getAggregates(IclijConfig conf, AbstractCategory[] categories,
            PipelineData[] pipelineData,
            List<String> disableList,
            Map<String, String> idNameMap, String catName, Integer cat, NeuralNetCommand neuralnetcommand) throws Exception {
        Aggregator[] aggregates = new Aggregator[10];
        aggregates[0] = new MACDBase(conf, catName, catName, cat, categories, idNameMap, pipelineData);
        //aggregates[1] = new AggregatorRecommenderIndicator(conf, catName, marketdatamap, categories, pipelineData, disableList);
        //aggregates[2] = new RecommenderRSI(conf, catName, marketdatamap, categories);
        aggregates[3] = new MLMACD(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[4] = new MLRSI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[5] = new MLATR(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[6] = new MLCCI(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[7] = new MLSTOCH(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[8] = new MLMulti(conf, catName, catName, cat, idNameMap, pipelineData, neuralnetcommand);
        aggregates[9] = new MLIndicator(conf, catName, catName, cat, categories, pipelineData, neuralnetcommand);
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
    
    class MyStockData {

        public String[] periodText;
        public Map<String, MarketData> marketdatamap;
        public List<StockItem>[] datedstocklists;
        public List<String> stockdates;
        public Map<String, List<StockItem>> stockdatemap;
        public Map<String, String> idNameMap;
        public Integer cat;
        public String catName;
        public List<StockItem> datedstocks;
        public Integer days;
        public Map<String, List<StockItem>> stockidmap;

    }
    public MyStockData getStockData(IclijConfig conf, String market, PipelineData[] pipelineData) {
        MyStockData stockData = new MyStockData();
        return stockData;
    }

}

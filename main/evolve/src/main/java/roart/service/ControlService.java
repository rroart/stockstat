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
import roart.common.cache.MyCache;
import roart.common.config.CacheConstants;
import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.constants.EurekaConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.model.MetaItem;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.common.webflux.WebFluxUtil;
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
import roart.result.model.ResultItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockUtil;

public class ControlService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    // Config for the core, not iclij
    public IclijConfig conf;

    public static CuratorFramework curatorClient;

    public ControlService() {
        super();
    }
    
    public List<MetaItem> getMetas() {
        String key = CacheConstants.METAS;
        List<MetaItem> list = (List<MetaItem>) MyCache.getInstance().get(key);
        if (list != null) {
            return list;
        }
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(conf.getConfigData());
        IclijServiceResult result = WebFluxUtil.sendCMe(IclijServiceResult.class, param, EurekaConstants.GETMETAS);
        list = result.getMetas();
        MyCache.getInstance().put(key, list);
        return list;
    }
    
    public List<StockItem> getStocks(String market) {
        IclijServiceParam param = new IclijServiceParam();
        param.setConfigData(conf.getConfigData());
        param.setMarket(market);
        IclijServiceResult result = WebFluxUtil.sendCMe(IclijServiceResult.class, param, EurekaConstants.GETSTOCKS);
        return result.getStocks();      
    }
    

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    //protected static int[] otherTableNames = { Constants.EVENT, Constants.MLTIMES }; 

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

    private void cleanRows(ResultItemTableRow headrow, ResultItemTable table) {
        ResultItemTableRow sum = null;
        if (table.rows.size() > 1) {
            sum = table.rows.get(1);
            log.info("Cols {}", sum.cols.size());
        } else {
            return;
        }
        for (int j = 2; j < table.rows.size(); j++) {
            ResultItemTableRow row = table.rows.get(j);
            for (int i = 0; i < sum.cols.size(); i++) {
                if (sum.cols.get(i) == null) {
                    sum.cols.set(i, row.cols.get(i));
                }
            }
        }
        for (int i = sum.cols.size() - 1; i >= 0; i--) {
            if (sum.cols.get(i) == null) {
                //headrow.cols.remove(i);
                for (ResultItemTableRow row : table.rows) {
                    row.cols.remove(i);
                }
            }
        }
        if (table.rows.size() > 0) {
            log.info("Cols new {}",table.rows.get(0).cols.size());
        }
    }

    private void addOtherTables(Aggregator[] aggregates) {
        for (int i = 0; i < aggregates.length; i++) {
            Map<Integer, List<ResultItemTableRow>> tableMap = aggregates[i].otherTables();
            if (tableMap == null) {
                continue;
            }
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private void addOtherTables(List<AbstractPredictor> predictors) {
        for (AbstractPredictor predictor : predictors) {
            if (predictor == null) {
                continue;
            }
            Map<Integer, List<ResultItemTableRow>> tableMap = predictor.otherTables();
            if (tableMap == null) {
                continue;
            }
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private void createRows(IclijConfig conf, ResultItemTable table, List<StockItem> datedstocks, AbstractCategory[] categories,
            AbstractPredictor[] predictors, Aggregator[] aggregates) {
        if (conf.getConfigData().getMarket() == null) {
            return;
        }
        int cols = table.get(0).cols.size();
        Set set = new HashSet<>();
        set.addAll(table.get(0).cols);
        int cols2 = set.size();
        List list = table.get(0).cols;
        /*
        for (int i = 0; i < list.size(); i++) {
            for (int j = i+1; j <list.size() ; j++) {
                if(list.get(i).equals(list.get(j))){
                    System.out.println(list.get(i));
                }
            }
        }
        */
        for (StockItem stock : datedstocks) {
            ResultItemTableRow row = new ResultItemTableRow();
            row.add(stock.getId());
            row.add(stock.getIsin());
            row.add(stock.getName());
            row.add(TimeUtil.format(stock.getDate()));
            try {
                for (int i = 0; i < Constants.ALLPERIODS; i++) {
                    categories[i].addResultItem(row, stock);
                }
                for (AbstractPredictor predictor : predictors) {
                    if (predictor != null) {
                        row.addarr(predictor.getResultItem(stock));
                    }
                }
                for (int i = 0; i < aggregates.length; i++) {
                    if (aggregates[i].isEnabled()) {
                        aggregates[i].addResultItem(row, stock);
                    }
                }
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            if (cols == row.cols.size()) {
                table.add(row);
            } else {
                int jj = 0;
            }
        }
    }

    private void addOtherTables(AbstractCategory[] categories) {
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            Map<Integer, List<ResultItemTableRow>> tableMap = categories[i].otherTables();
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private void addOtherTables(AbstractPredictor[] predictors) {
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            if (predictors[i] == null) {
                continue;
            }
            Map<Integer, List<ResultItemTableRow>> tableMap = predictors[i].otherTables();
            for (Entry<Integer, List<ResultItemTableRow>> entry : tableMap.entrySet()) {
                List<ResultItemTableRow> resultItems = entry.getValue();
                ResultItemTable otherTable = otherTableMap.get(entry.getKey());
                for (ResultItemTableRow row : resultItems) {
                    otherTable.add(row);
                }
            }
        }
    }

    private ResultItemTableRow createHeadRow(AbstractCategory[] categories, AbstractPredictor[] predictors, Aggregator[] aggregates) {
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add(Constants.IMG);
        headrow.add("ISIN");
        headrow.add("Name");
        headrow.add("Date");
        for (int i = 0; i < Constants.ALLPERIODS; i++) {
            categories[i].addResultItemTitle(headrow);
        }
        for (AbstractPredictor predictor : predictors) {
            if (predictor != null) {
                headrow.addarr(predictor.getResultItemTitle());
            }
        }
        for (int i = 0; i < aggregates.length; i++) {
            if (aggregates[i].isEnabled()) {
                aggregates[i].addResultItemTitle(headrow);
            }
        }
        List<Object> duplicates = headrow.cols.stream()
                .filter(e -> Collections.frequency(headrow.cols, e) > 1)
                .distinct()
                .collect(Collectors.toList());
        if (duplicates.size() > 0) {
            log.error("Duplicates {}", duplicates);
        }
        return headrow;
    }

    private Aggregator[] getAggregates(IclijConfig conf, String[] periodText,
            Map<String, MarketData> marketdatamap,
            AbstractCategory[] categories,
            Pipeline[] datareaders, List<String> disableList, Map<String, String> idNameMap, String catName, Integer cat, NeuralNetCommand neuralnetcommand) throws Exception {
        Aggregator[] aggregates = new Aggregator[10];
        aggregates[0] = new MACDBase(conf, catName, catName, cat, datareaders, stockDates, inmemory);
        aggregates[1] = new AggregatorRecommenderIndicator(conf, catName, marketdatamap, categories, datareaders, disableList, inmemory);
        aggregates[2] = new RecommenderRSI(conf, catName, marketdatamap, categories, inmemory);
        aggregates[3] = new MLMACD(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
        aggregates[4] = new MLRSI(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
        aggregates[5] = new MLATR(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
        aggregates[6] = new MLCCI(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
        aggregates[7] = new MLSTOCH(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
        aggregates[8] = new MLMulti(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
        aggregates[9] = new MLIndicator(conf, catName, catName, cat, datareaders, neuralnetcommand, stockDates, inmemory);
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

}

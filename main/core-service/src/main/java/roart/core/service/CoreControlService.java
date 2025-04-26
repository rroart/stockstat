package roart.core.service;

import java.util.Arrays;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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
import roart.category.util.CategoryUtil;
import roart.common.communication.factory.CommunicationFactory;
import roart.common.communication.model.Communication;
import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.indicator.util.IndicatorUtils;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.MetaDTO;
import roart.common.model.StockDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialInteger;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialMeta;
import roart.common.pipeline.data.SerialPlain;
import roart.common.pipeline.data.SerialString;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.JsonUtil;
import roart.common.util.ServiceConnectionUtil;
import roart.common.util.TimeUtil;
import roart.core.graphcategory.GraphCategory;
import roart.core.graphcategory.GraphCategoryIndex;
import roart.core.graphcategory.GraphCategoryPeriod;
import roart.core.graphcategory.GraphCategoryPeriodTopBottom;
import roart.core.graphcategory.GraphCategoryPrice;
import roart.core.model.impl.DbDataSource;
import roart.core.service.util.ServiceUtil;
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
import roart.core.util.Math3Util;
import roart.model.io.IO;

public class CoreControlService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private static final ObjectMapper mapper = new JsonMapper().builder().addModule(new JavaTimeModule()).build();

    public List<String> getMarkets() {
        try {
            return io.getDbDao().getMarkets();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    public List<MetaDTO> getMetas() {
        try {
            return io.getDbDao().getMetas();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

    private IO io;
    
    public CoreControlService(IO io) {
        super();
        this.io = io;
    }

    public Map<String, String> getStocks(String market, IclijConfig conf) {
        // TODO pipeline
        try {
            Map<String, String> stockMap = new HashMap<>();
            List<StockDTO> stocks = io.getDbDao().getAll(market, conf, true);
            stocks.remove(null);
            for (StockDTO stock : stocks) {
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

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    ResultItemTable mlTimesTable = ServiceUtil.createMLTimesTable(otherTableMap);
    ResultItemTable eventTable = ServiceUtil.createEventTable(otherTableMap);

    private Function<String, Boolean> zkRegister;

    //protected static int[] otherTableNames = { Constants.EVENT, Constants.MLTIMES }; 

    /**
     * Create result lists
     * @param origparam TODO
     * @param maps 
     * @param pipelinedata TODO
     * @param dbio.getDataSource() 
     * @return the tabular result lists
     */

    public List<ResultItem> getContent(IclijConfig conf, List<String> disableList, IclijServiceResult result, IclijServiceParam origparam) {
        Inmemory inmemory = io.getInmemoryFactory().get(conf);
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        //createOtherTables();
        List<ResultItem> retlist = new ArrayList<>();
        ResultItemTable table = new ResultItemTable();
        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);
        PipelineData[] pipelinedata = new PipelineData[0];
        List<AbstractCategory> categories = new ArrayList<>();
        List<Aggregator> aggregates = new ArrayList<>();
        try {
            StockData stockData = new Extract(io.getDbDao()).getStockData(conf, true);
            if (stockData == null) {
                return new ArrayList<>();
            }

            IndicatorUtils iu = new IndicatorUtils();
            ExtraReader extraReader = new ExtraReader(conf, stockData.marketdatamap, 0, stockData);
            Map<String, StockData> extraStockDataMap = new IndicatorUtils().getExtraStockDataMap(conf, io.getDbDao(), extraReader, true);

            Pipeline[] datareaders = iu.getDataReaders(conf, stockData.periodText,
                    stockData.marketdatamap, stockData, extraStockDataMap, extraReader);

            /*
            pipelinedata = iu.createPipeline(conf, disableList, pipelinedata, categories, aggregates, stockData,
                    datareaders);
            */
            
            // pipelinedata from datareaders and new meta

            pipelinedata = iu.createDatareaderPipelineData(conf, pipelinedata, stockData, datareaders);

            // for categories and adding to pipelinedata

            List<StockDTO> dayStocks = iu.getDayStocks(conf, stockData);
            
            categories = Arrays.asList(new CategoryUtil().getCategories(conf, dayStocks,
                    stockData.periodText, pipelinedata, inmemory));
            
            // add all indicators for the category

            pipelinedata = iu.createPipelineDataCategories(pipelinedata, categories, stockData);

            // for aggregates and adding to the pipeline

            aggregates = Arrays.asList(getAggregates(conf, stockData.periodText,
                    stockData.marketdatamap, categories.toArray(new AbstractCategory[0]), pipelinedata , disableList, stockData.catName, stockData.cat, stockData.stockdates, inmemory));

            pipelinedata = iu.createPipelineAggregators(pipelinedata, aggregates);
            
            ResultItemTableRow headrow = createHeadRow(categories.toArray(new AbstractCategory[0]), new AbstractPredictor[0], aggregates.toArray(new Aggregator[0]));
            table.add(headrow);
            //log.info("sizes " + stocks.size() + " " + datedstocks.size() + " " + datedstocksoffset.size());
            createRows(conf, table, stockData.datedstocks, categories.toArray(new AbstractCategory[0]), new AbstractPredictor[0], aggregates.toArray(new Aggregator[0]));
            log.info("retlist2 {}",table.size());
            cleanRows(headrow, table);
            addOtherTables(categories.toArray(new AbstractCategory[0]));
            addOtherTables(aggregates.toArray(new Aggregator[0]));
            retlist.add(table);
            for (ResultItemTable list : otherTables) {
                retlist.add(list);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        PipelineUtils.printmap(pipelinedata);
        new CleanETL().fixmap(pipelinedata);
        PipelineUtils.printmap(pipelinedata);

        result.setList(retlist);
        if (origparam.getId() != null) {
            //PipelineUtils.setPipelineMap(pipelinedata, false);
            log.info("Before setPipelineMap");
            PipelineUtils.setPipelineMap(pipelinedata, origparam.getId());
            pipelinedata = PipelineUtils.setPipelineMap(pipelinedata, inmemory, io.getCuratorClient());
        }
        result.setPipelineData(pipelinedata);

        if (true) return retlist;
        // TODO refid
        for (PipelineData data : pipelinedata) {
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
        return retlist;
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

    private void createRows(IclijConfig conf, ResultItemTable table, List<StockDTO> datedstocks, AbstractCategory[] categories,
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
        for (StockDTO stock : datedstocks) {
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
            PipelineData[] datareaders, List<String> disableList, String catName, Integer cat, List<String> stockDates, Inmemory inmemory) throws Exception {
        Aggregator[] aggregates = new Aggregator[3];
        aggregates[0] = new MACDBase(conf, catName, catName, cat, datareaders, stockDates, inmemory);
        aggregates[1] = new AggregatorRecommenderIndicator(conf, catName, marketdatamap, categories, datareaders, disableList, inmemory);
        aggregates[2] = new RecommenderRSI(conf, catName, marketdatamap, categories, inmemory);
        /*
        aggregates[3] = new MLMACD(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand);
        aggregates[4] = new MLRSI(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand);
        aggregates[5] = new MLATR(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand);
        aggregates[6] = new MLCCI(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand);
        aggregates[7] = new MLSTOCH(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand);
        aggregates[8] = new MLMulti(conf, catName, catName, cat, idNameMap, datareaders, neuralnetcommand);
        aggregates[9] = new MLIndicator(conf, catName, catName, cat, datareaders, neuralnetcommand);
        */
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.MACHINELEARNING));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.AGGREGATORSMLMACD));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORSMACD));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORSRSI));
        log.info("Aggregate {}", conf.getConfigData().getConfigValueMap().get(ConfigConstants.INDICATORS));
        return aggregates;
    }

    private GraphCategory[] getGraphCategories(IclijConfig conf,
            String[] periodTextNot,
            Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap) {
        GraphCategory[] categories = new GraphCategory[Constants.PERIODS + 2];
        categories[0] = new GraphCategoryIndex(conf, Constants.INDEX, marketdatamap, periodDataMap);
        categories[1] = new GraphCategoryPrice(conf, Constants.PRICE, marketdatamap, periodDataMap);
        int i = 2;
        Set<String> keys = new TreeSet<>(periodDataMap.keySet());
        keys.remove(Constants.INDEX);
        keys.remove(Constants.PRICE);
        for (String periodText : keys) {
            categories[i ++] = new GraphCategoryPeriod(conf, i, periodText, marketdatamap, periodDataMap);
        }
        return categories;
    }

    /**
     * Create result graphs
     * @param guiSize gui size
     * 
     * @return the image list
     */

    public List<ResultItem> getContentGraph(IclijConfig conf, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate {}", conf.getConfigData().getDate());
            log.info("mydate {}", conf.getDays());
            StockData stockData = new Extract(io.getDbDao()).getStockData(conf, true);
            if (stockData == null) {
                return new ArrayList<>();
            }
            // sort based on date
            for (Entry<String, List<StockDTO>> entry : stockData.stockidmap.entrySet()) {
                List<StockDTO> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
            }

            List<StockDTO>[] datedstocklistsmove = StockUtil.getDatedstocklists(stockData.stockdatemap, conf.getConfigData().getDate(), stockData.days, conf.getTableMoveIntervalDays());

            List<StockDTO>[][] stocklistPeriod = StockUtil.getListSorted(datedstocklistsmove, stockData.days);
            
            GraphCategoryPeriodTopBottom[] categories = new GraphCategoryPeriodTopBottom[Constants.PERIODS];
            for (int i = 0; i < Constants.PERIODS; i++) {
                categories[i] = new GraphCategoryPeriodTopBottom(conf, i, stockData.periodText[i], stocklistPeriod);
            }

            for (GraphCategory category : categories) {
                category.addResult(retlist, null, guiSize);
            }

        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        log.info("retlist size {}", retlist.size());
        return retlist;
    }

    /**
     * Create result graphs for one
     * 
     * myid the id of the unit
     * @param guiSize gui size
     * @return the image list
     */

    public List<ResultItem> getContentGraph(IclijConfig conf, Set<Pair<String, String>> ids, GUISize guiSize) {
        List<ResultItem> retlist = new ArrayList<>();
        try {
            log.info("mydate {}", conf.getConfigData().getDate());
            Map<String, MarketData> marketdatamap = new HashMap<>();
            Map<String, PeriodData> periodDataMap = new HashMap<>();
            Map<String, StockData> stockDataMap = new HashMap<>();
            Set<String> markets = new ServiceUtil().getMarkets(ids);
            for (String market : markets) {
                StockData stockData = new Extract(io.getDbDao()).getStockData(conf, market, true);
                if (stockData == null) {
                    return new ArrayList<>();
                }
                stockDataMap.put(market, stockData);
                marketdatamap.putAll(stockData.marketdatamap);
                periodDataMap.putAll(new PeriodDataETL().getPerioddatamap(markets,
                        marketdatamap));
            }
            GraphCategory[] categories = getGraphCategories(conf, null, marketdatamap, periodDataMap);

            for (int i = 0; i < Constants.ALLPERIODS; i++) {
                categories[i].addResult(retlist, ids, guiSize);
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }

        log.info("retlist size {}", retlist.size());
        return retlist;
    }

    /**
     * Create stat result lists
     * 
     * @return the tabular result lists
     */

    public List<ResultItem> getContentStat(IclijConfig conf) {
        List<ResultItem> retList = new ArrayList<>();
        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow row = new ResultItemTableRow();
        row.add(Constants.IMG);
        row.add("Name 1");
        row.add("Name 2");
        row.add("Period");
        row.add("Size");
        row.add("Paired t");
        row.add("P-value");
        row.add("Alpha 0.05");
        row.add("Paired t (e)");
        row.add("P-value (e)");
        row.add("Alpha 0.05 (e)");
        row.add("Spearman (e)");
        row.add("Kendall (e)");
        row.add("Pearson (e)");
        table.add(row);
        try {
            List<StockDTO> stocks = io.getDbDao().getAll(conf.getConfigData().getMarket(), conf, true);
            log.info("stocks {}", stocks.size());
            Map<String, List<StockDTO>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockDTO>> stockdatemap = StockUtil.splitDate(stocks);
            stockdatemap = StockUtil.filterFew(stockdatemap, conf.getFilterDate());

            // sort based on date
            for (Entry<String, List<StockDTO>> entry : stockidmap.entrySet()) {
                List<StockDTO> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
            }

            int days = conf.getTableDays();

            // the main list, based on freshest or specific date.

            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */

            List<StockDTO> datedstocklists[] = StockUtil.getDatedstocklists(stockdatemap, conf.getConfigData().getDate(), days, conf.getTableIntervalDays());

            List<StockDTO> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return new ArrayList<>();
            }
            Math3Util.getStats(table, conf.getConfigData().getDate(), days, stockidmap, stockdatemap);

            log.info("retlist {}",retList.size());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        retList.add(table);
        return retList;
    }

    /// TODO too big
    public void getDates(IclijConfig conf, IclijServiceResult result, IclijServiceParam origparam) {
        Inmemory inmemory = io.getInmemoryFactory().get(conf);
        PipelineData[] pipelineData = new PipelineData[0];
        Map<String, Object> aMap = new HashMap<>();
        /*
        aMap.put(ConfigConstants.MACHINELEARNING, false);
        aMap.put(ConfigConstants.AGGREGATORS, false);
        aMap.put(ConfigConstants.INDICATORS, false);
        aMap.put(ConfigConstants.MISCTHRESHOLD, null);
        */        
        aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
        aMap.put(ConfigConstants.MISCMYDAYS, 0);
        /*
        aMap.put(ConfigConstants.MISCPERCENTIZEPRICEINDEX, true);
        aMap.put(ConfigConstants.MISCINTERPOLATIONMETHOD, market.getConfig().getInterpolate());
        aMap.put(ConfigConstants.MISCINTERPOLATIONLASTNULL, Boolean.TRUE);
        aMap.put(ConfigConstants.MISCMERGECY, false);        
        conf.setConfigValueMap(new HashMap<>(conf.getConfigValueMap()));
        */
        conf.getConfigData().getConfigValueMap().putAll(aMap);
        StockData stockData = new Extract(io.getDbDao()).getStockData(conf, true); // TODO false
        if (stockData != null) {
            PipelineData map = new PipelineData();
            map.setName(PipelineConstants.DATELIST);
            map.put(PipelineConstants.DATELIST, new SerialListPlain(stockData.stockdates));
            pipelineData = ArrayUtils.add(pipelineData, map);
            PipelineUtils.setPipelineMap(pipelineData, origparam.getId());
            if (origparam.getId() != null) {
                log.info("Before setPipelineMap");
                PipelineUtils.setPipelineMap(pipelineData, origparam.getId());
                pipelineData = PipelineUtils.setPipelineMap(pipelineData, inmemory, io.getCuratorClient());
            }
            result.setPipelineData(pipelineData);
            return;
        }
        
        // TODO not used anymore?
        List<String> dates = null;
        try {
            if ("0".equals(conf.getConfigData().getMarket())) {
                int jj = 0;
            }
            dates = io.getDbDao().getDates(conf.getConfigData().getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (dates == null) {
            return;
        }
        log.info("stocks {}", dates.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getConfigData().getMarket());

        try {
            Collections.sort(dates);
            PipelineData map = new PipelineData();
            map.setName(PipelineConstants.DATELIST);
            map.put(PipelineConstants.DATELIST, new SerialListPlain(dates));
            pipelineData = ArrayUtils.add(pipelineData, map);
            result.setPipelineData(pipelineData);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return;
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
        Communication c = io.getCommunicationFactory().get(sc.getLeft(), null, service, objectMapper, true, false, false, sc.getRight(), zkRegister, null);
        c.send(object);
    }

}

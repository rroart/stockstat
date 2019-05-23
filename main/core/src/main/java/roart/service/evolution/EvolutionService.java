package roart.service.evolution;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregatorindicator.impl.Recommend;
import roart.category.AbstractCategory;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.impl.IndicatorChromosome;
import roart.evolution.chromosome.impl.IndicatorEvaluationNew;
import roart.evolution.chromosome.impl.NeuralNetChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.impl.ProportionScore;
import roart.evolution.species.Individual;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.pipeline.impl.DataReader;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.service.ControlService;
import roart.service.util.ServiceUtil;
import roart.stockutil.StockUtil;
import roart.talib.util.TaUtil;

public class EvolutionService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    ResultItemTable mlTimesTable = ServiceUtil.createMLTimesTable(otherTableMap);
    ResultItemTable eventTable = ServiceUtil.createEventTable(otherTableMap);

    public List<ResultItem> getEvolveRecommender(MyMyConfig conf, List<String> disableList, Map<String, Object> updateMap) throws JsonParseException, JsonMappingException, IOException {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        ObjectMapper mapper = new ObjectMapper();
        EvolutionConfig evolutionConfig = mapper.readValue(conf.getTestIndictorrecommenderEvolutionConfig(), EvolutionConfig.class);
    
        //createOtherTables();
        List<StockItem> stocks = null;
        try {
            stocks = DbDao.getAll(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return new ArrayList<>();
        }
        log.info("stocks {}", stocks.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getMarket());
        Integer days = conf.getDays();
    
        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);
    
        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Config");
        headrow.add("Old value");
        headrow.add("New value");
        table.add(headrow);
    
        try {
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            log.info("datemapsize {}", stockdatemap.size());
            if (conf.getdate() == null) {
                new ServiceUtil().getCurrentDate(conf, stockdatemap);
            }
    
            Map<String, MarketData> marketdatamap = null;
            marketdatamap = new ServiceUtil().getMarketdatamap(days, markets, conf);
            Map<String, PeriodData> periodDataMap = new ServiceUtil().getPerioddatamap(markets,
                    marketdatamap);
    
            if (stocks.size() != marketdatamap.get(conf.getMarket()).stocks.size()) {
                log.error("Sizes {} {}", stocks.size(), marketdatamap.get(conf.getMarket()).stocks.size());
            }
            /*
            idNameMap = new HashMap<>();
            // sort based on date
            for (Entry<String, List<StockItem>> entry : stockidmap.entrySet()) {
                List<StockItem> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
                idNameMap.put(entry.getKey(), stocklist.get(0).getName());
            }
            */
            // the main list, based on freshest or specific date.
    
            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
    
            List<StockItem>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());
    
            List<StockItem> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return new ArrayList<>();
            }
            log.info("Datestocksize {}", datedstocks.size());
    
            Integer cat = IndicatorUtils.getWantedCategory(stocks, marketdatamap.get(conf.getMarket()).meta);
            if (cat == null) {
                return new ArrayList<>();
            }
            DataReader dataReader = new DataReader(conf, marketdatamap, periodDataMap, cat);
            Pipeline[] datareaders = new Pipeline[1];
            datareaders[0] = dataReader;
    
            // no...get this from the category
            // make oo of this
            // optimize with constructors, no need for duplicate
            // map from type (complex/simple) to recommender and keysets
            Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
            Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
            Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
            int category = cat;
            Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();
            createRecommendIndicatorMap(marketdatamap, datareaders, usedRecommenders, indicatorMap, category,
                    newIndicatorMap);
    
            findRecommendSettings(conf, evolutionConfig, disableList, table, usedRecommenders, recommendKeyMap, indicatorMap, updateMap);
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);
            return retlist;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            log.info("Market {}", conf.getMarket());
            return new ArrayList<>();
        }
    }

    private void findRecommendSettings(MyMyConfig conf, EvolutionConfig evolutionConfig, List<String> disableList, ResultItemTable table,
            Map<String, List<Recommend>> usedRecommenders, Map<String, List<String>[]> recommendKeyMap,
            Map<String, AbstractIndicator> indicatorMap, Map<String, Object> updateMap) throws Exception {
        TaUtil tu = new TaUtil();
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<AbstractIndicator> indicators = Recommend.getIndicators(entry.getKey(), usedRecommenders, indicatorMap);
            List<String>[] recommendList = recommendKeyMap.get(entry.getKey());
            Recommend recommend = entry.getValue().get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, recommend.getFutureDays(), conf.getTableDays(), recommend.getIntervalDays(), null);
            List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
            if (macdrsiMinMax == null || macdrsiMinMax.length == 1) {
                int jj = 0;
            }
    
            for (int i = 0; i < 2; i++) {
                List<String> scoreList = recommendList[i];
                IndicatorChromosome indicatorEval0 = new IndicatorChromosome(conf, scoreList, retObj, true, disableList, new ProportionScore());
    
                OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
    
                Individual fittestIndividual = evolution.getFittest(evolutionConfig, indicatorEval0);
    
                for (String id : scoreList) {
                    ResultItemTableRow row = new ResultItemTableRow();
                    row.add(id);
                    row.add("" + conf.getConfigValueMap().get(id));
                    //log.info("Buy {} {}", id, buy.getConf().getConfigValueMap().get(id));
                    //log.info("Buy {}", buy.getConf().getConfigValueMap().get(id).getClass().getName());
                    IndicatorChromosome newEval = (IndicatorChromosome) fittestIndividual.getEvaluation();
                    row.add("" + newEval.getConf().getConfigValueMap().get(id));
                    table.add(row);
                }
                // have a boolean here
                for (String id : scoreList) {
                    IndicatorChromosome newEval = (IndicatorChromosome) fittestIndividual.getEvaluation();
                    updateMap.put(id, newEval.getConf().getConfigValueMap().get(id));
                }
            }
        }
    }

    private void createRecommendIndicatorMap(Map<String, MarketData> marketdatamap, Pipeline[] datareaders,
            Map<String, List<Recommend>> usedRecommenders, Map<String, AbstractIndicator> indicatorMap, int category,
            Map<String, AbstractIndicator> newIndicatorMap) throws Exception {
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<Recommend> list = entry.getValue();
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                indicatorMap.put(indicator, recommend.getIndicator(marketdatamap, category, newIndicatorMap, null, datareaders));
            }
        }
    }

    public List<ResultItem> getEvolveRecommenderSingle(MyMyConfig conf, List<String> disableList, Map<String, Object> updateMap) throws JsonParseException, JsonMappingException, IOException {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        ObjectMapper mapper = new ObjectMapper();
        EvolutionConfig evolutionConfig = mapper.readValue(conf.getTestIndictorrecommenderEvolutionConfig(), EvolutionConfig.class);
    
        //createOtherTables();
        List<StockItem> stocks = null;
        try {
            stocks = DbDao.getAll(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return new ArrayList<>();
        }
        log.info("stocks {}", stocks.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getMarket());
        Integer days = conf.getDays();
    
        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);
    
        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Config");
        headrow.add("Old value");
        headrow.add("New value");
        table.add(headrow);
    
        try {
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            log.info("datemapsize {}", stockdatemap.size());
            if (conf.getdate() == null) {
                new ServiceUtil().getCurrentDate(conf, stockdatemap);
            }
    
            Map<String, MarketData> marketdatamap = null;
            marketdatamap = new ServiceUtil().getMarketdatamap(days, markets, conf);
            Map<String, PeriodData> periodDataMap = new ServiceUtil().getPerioddatamap(markets,
                    marketdatamap);
    
            if (stocks.size() != marketdatamap.get(conf.getMarket()).stocks.size()) {
                log.error("Sizes {} {}", stocks.size(), marketdatamap.get(conf.getMarket()).stocks.size());
            }
            /*
            idNameMap = new HashMap<>();
            // sort based on date
            for (Entry<String, List<StockItem>> entry : stockidmap.entrySet()) {
                List<StockItem> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
                idNameMap.put(entry.getKey(), stocklist.get(0).getName());
            }
    */
            // the main list, based on freshest or specific date.
    
            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
    
            List<StockItem>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());
    
            List<StockItem> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return new ArrayList<>();
            }
            log.info("Datestocksize {}", datedstocks.size());
    
            Integer cat = IndicatorUtils.getWantedCategory(stocks, marketdatamap.get(conf.getMarket()).meta);
            if (cat == null) {
                return new ArrayList<>();
            }
            DataReader dataReader = new DataReader(conf, marketdatamap, periodDataMap, cat);
            Pipeline[] datareaders = new Pipeline[1];
            datareaders[0] = dataReader;
    
            // no...get this from the category
            // make oo of this
            // optimize with constructors, no need for duplicate
            // map from type (complex/simple) to recommender and keysets
            Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
            Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders);
            Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
            int category = cat;
            Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();
            createRecommendIndicatorMap(marketdatamap, datareaders, usedRecommenders, indicatorMap, category,
                    newIndicatorMap);
    
            findRecommendSettingsNew(conf, evolutionConfig, disableList, table, usedRecommenders, recommendKeyMap, indicatorMap, updateMap);
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);
            return retlist;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return new ArrayList<>();
        }
    }

    private void findRecommendSettingsNew(MyMyConfig conf, EvolutionConfig evolutionConfig, List<String> disableList, ResultItemTable table,
            Map<String, List<Recommend>> usedRecommenders, Map<String, List<String>[]> recommendKeyMap,
            Map<String, AbstractIndicator> indicatorMap, Map<String, Object> updateMap) throws Exception {
        TaUtil tu = new TaUtil();
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<AbstractIndicator> indicators = Recommend.getIndicators(entry.getKey(), usedRecommenders, indicatorMap);
            List<String>[] recommendList = recommendKeyMap.get(entry.getKey());
            Recommend recommend = entry.getValue().get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, recommend.getFutureDays(), conf.getTableDays(), recommend.getIntervalDays(), null);
            List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
            if (macdrsiMinMax.length == 1) {
                int jj = 0;
            }
    
            List<String> buyList = recommendList[0];
            List<String> sellList = recommendList[1];
            findRecommendSettingsNew(conf, evolutionConfig, disableList, table, updateMap, retObj, buyList, true);
            findRecommendSettingsNew(conf, evolutionConfig, disableList, table, updateMap, retObj, sellList, false);
        }
    }

    private void findRecommendSettingsNew(MyMyConfig conf, EvolutionConfig evolutionConfig, List<String> disableList,
            ResultItemTable table, Map<String, Object> updateMap, Object[] retObj, List<String> keyList, boolean doBuy) throws Exception {
        for (String id : keyList) {
            if (disableList.contains(id)) {
                continue;
            }
            IndicatorEvaluationNew recommend = new IndicatorEvaluationNew(conf, id, retObj, doBuy, keyList.indexOf(id));
    
            OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
    
            Individual buysell = evolution.getFittest(evolutionConfig, recommend);
    
            ResultItemTableRow row = new ResultItemTableRow();
            row.add(id);
            row.add("" + conf.getConfigValueMap().get(id));
            //log.info("Buy {} {}", id, buy.getConf().getConfigValueMap().get(id));
            //log.info("Buy {}", buy.getConf().getConfigValueMap().get(id).getClass().getName());
            IndicatorEvaluationNew newEval = (IndicatorEvaluationNew) buysell.getEvaluation();
         
            row.add("" + newEval.getConf().getConfigValueMap().get(id));
            table.add(row);
            updateMap.put(id, newEval.getConf().getConfigValueMap().get(id));
        }
    }

    public List<ResultItem> getEvolveML(MyMyConfig conf, List<String> disableList, Map<String, Object> updateMap, String ml) throws JsonParseException, JsonMappingException, IOException {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        ObjectMapper mapper = new ObjectMapper();
        EvolutionConfig evolutionConfig = mapper.readValue(conf.getTestMLEvolutionConfig(), EvolutionConfig.class);
        //createOtherTables();
        List<StockItem> stocks = null;
        try {
            stocks = DbDao.getAll(conf.getMarket(), conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        if (stocks == null) {
            return new ArrayList<>();
        }
        log.info("stocks {}", stocks.size());
        Set<String> markets = new HashSet<>();
        markets.add(conf.getMarket());
        Integer days = conf.getDays();
    
        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);
    
        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Config");
        headrow.add("Old value");
        headrow.add("New value");
        table.add(headrow);
    
        try {
            Map<String, List<StockItem>> stockidmap = StockUtil.splitId(stocks);
            Map<String, List<StockItem>> stockdatemap = StockUtil.splitDate(stocks);
            log.info("datemapsize {}", stockdatemap.size());
            if (conf.getdate() == null) {
                new ServiceUtil().getCurrentDate(conf, stockdatemap);
            }
    
            Map<String, MarketData> marketdatamap = null;
            marketdatamap = new ServiceUtil().getMarketdatamap(days, markets, conf);
            Map<String, PeriodData> periodDataMap = new ServiceUtil().getPerioddatamap(markets,
                    marketdatamap);
    
            if (stocks.size() != marketdatamap.get(conf.getMarket()).stocks.size()) {
                log.error("Sizes {} {}", stocks.size(), marketdatamap.get(conf.getMarket()).stocks.size());
            }
            /*
            //idNameMap = new HashMap<>();
            // sort based on date
            for (Entry<String, List<StockItem>> entry : stockidmap.entrySet()) {
                List<StockItem> stocklist = entry.getValue();
                stocklist.sort(StockUtil.StockDateComparator);
                idNameMap.put(entry.getKey(), stocklist.get(0).getName());
            }
            */
            
            // the main list, based on freshest or specific date.
    
            /*
             * For all days with intervals
             * Make stock lists based on the intervals
             */
    
            List<StockItem>[] datedstocklists = StockUtil.getDatedstocklists(stockdatemap, conf.getdate(), 2, conf.getTableMoveIntervalDays());
    
            List<StockItem> datedstocks = datedstocklists[0];
            if (datedstocks == null) {
                return new ArrayList<>();
            }
            log.info("Datestocksize {}", datedstocks.size());
    
            Integer cat = IndicatorUtils.getWantedCategory(stocks, marketdatamap.get(conf.getMarket()).meta);
            if (cat == null) {
                return new ArrayList<>();
            }
            String[] periodText = DbDaoUtil.getPeriodText(conf.getMarket(), conf);
            String catName = getCatName(cat, periodText);
            DataReader dataReader = new DataReader(conf, marketdatamap, periodDataMap, cat);
            //Pipeline[] datareaders = new Pipeline[1];
            Pipeline[] datareaders = new ServiceUtil().getDataReaders(conf, stocks,
                    periodText, marketdatamap, periodDataMap);
    
            //datareaders[0] = dataReader;
    
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            String mydate = dt.format(conf.getdate());
            List<StockItem> dayStocks = stockdatemap.get(mydate);
            AbstractCategory[] categories = new ServiceUtil().getCategories(conf, dayStocks,
                    periodText, marketdatamap, periodDataMap, datedstocklists, datareaders);
            AbstractPredictor[] predictors = new ServiceUtil().getPredictors(conf, dayStocks,
                    periodText, marketdatamap, periodDataMap, datedstocklists, datareaders, categories);
            //new ServiceUtil().createPredictors(categories);
            new ServiceUtil().calculatePredictors(predictors);
    
            findMLSettings(conf, evolutionConfig, disableList, table, updateMap, ml, datareaders, categories, catName, cat);
    
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);
            return retlist;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return new ArrayList<>();
        }
    }

    public static String getCatName(Integer cat, String[] periodText) {
        String catName = null;
        if (cat >= 0) {
            catName = periodText[cat];
        }
        if (cat == Constants.INDEXVALUECOLUMN) {
            catName = Constants.INDEX;
        }
        if (cat == Constants.PRICECOLUMN) {
            catName = Constants.PRICE;
        }
        return catName;
    }

    private void findMLSettings(MyMyConfig conf, EvolutionConfig evolutionConfig, List<String> disableList, ResultItemTable table,
            Map<String, Object> updateMap, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat) throws Exception {
        TaUtil tu = new TaUtil();
        log.info("Evolution config {} {} {} {}", evolutionConfig.getGenerations(), evolutionConfig.getSelect(), evolutionConfig.getElite(), evolutionConfig.getMutate());
        NeuralNetConfigs nnConfigs = null;
        String nnconfigString = null;
        if (ml.equals(PipelineConstants.MLINDICATOR)) {
            nnconfigString = conf.getAggregatorsMLIndicatorMLConfig();
            if (nnconfigString != null) {
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
            }
        }
        if (ml.equals(PipelineConstants.MLMACD)) {
            nnconfigString = conf.getMLMACDMLConfig();
            if (nnconfigString != null) {
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
            }
        }
        if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
            nnconfigString = conf.getLSTMConfig();
            if (nnconfigString != null) {
                nnConfigs = new NeuralNetConfigs();
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                TensorflowLSTMConfig nnConfig = mapper.readValue(nnconfigString, TensorflowLSTMConfig.class);
                nnConfigs.setTensorflowLSTMConfig(nnConfig);
            }
        }
        if (nnConfigs == null) {
            nnConfigs = new NeuralNetConfigs();            
        }
        NeuralNetConfigs newNNConfigs = new NeuralNetConfigs();
        List<String> keys = new ArrayList<>();
        keys.add(ConfigConstants.MACHINELEARNINGSPARKMLLR);
        keys.add(ConfigConstants.MACHINELEARNINGSPARKMLMCP);
        keys.add(ConfigConstants.MACHINELEARNINGSPARKMLOVR);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWL);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM);
   
        for (String key : keys) {
            System.out.println(conf.getValueOrDefault(key));
            if (!Boolean.TRUE.equals(conf.getConfigValueMap().get(key))) {
                continue;
            }
            MyMyConfig workingConf = conf.copy();
            for (String tmpkey : keys) {
                boolean enabled = (boolean) workingConf.getValueOrDefault(tmpkey);
                boolean sameKey = key.equals(tmpkey);
                sameKey &= enabled;
                workingConf.getConfigValueMap().put(tmpkey, sameKey);
            }
            NeuralNetConfig nnconfig = nnConfigs.get(key);
            NeuralNetChromosome chromosome = new NeuralNetChromosome(workingConf, ml, dataReaders, categories, key, nnconfig, catName, cat);
            if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
                chromosome.setAscending(false);
            }
    
            OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
    
            Individual best = evolution.getFittest(evolutionConfig, chromosome);
    
            NeuralNetChromosome bestEval2 = (NeuralNetChromosome) best.getEvaluation();
            NeuralNetConfig newnnconf = bestEval2.getNnConfig();
            newNNConfigs.set(key, newnnconf);
        }
        ObjectMapper mapper = new ObjectMapper();
        String newNNConfigstring = mapper.writeValueAsString(newNNConfigs);
        String myKey = null;
        if (ml.equals(PipelineConstants.MLINDICATOR)) {
            myKey = ConfigConstants.AGGREGATORSINDICATORMLCONFIG;
        }
        if (ml.equals(PipelineConstants.MLMACD)) {
            myKey = ConfigConstants.AGGREGATORSMLMACDMLCONFIG;
        }
        if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
            myKey = ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG;
            newNNConfigstring = mapper.writeValueAsString(newNNConfigs.getTensorflowLSTMConfig());
        }
        updateMap.put(myKey, newNNConfigstring);
        ResultItemTableRow row = new ResultItemTableRow();
        row.add(myKey);
        row.add(nnconfigString);
        row.add(newNNConfigstring);
        table.add(row);
    }

}

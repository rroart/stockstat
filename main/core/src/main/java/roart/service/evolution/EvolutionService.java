package roart.service.evolution;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
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
import roart.common.constants.EvolveConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.NeuralNetTensorflowConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.model.MetaItem;
import roart.common.pipeline.PipelineConstants;
import roart.db.dao.DbDao;
import roart.db.dao.util.DbDaoUtil;
import roart.etl.MarketDataETL;
import roart.etl.PeriodDataETL;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.IndicatorChromosome;
import roart.evolution.chromosome.impl.IndicatorEvaluationNew;
import roart.evolution.chromosome.impl.NeuralNetChromosome2;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.impl.ProportionScore;
import roart.evolution.species.Individual;
import roart.gene.NeuralNetConfigGene;
import roart.gene.NeuralNetConfigGeneFactory;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.model.data.StockData;
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
import roart.common.util.JsonUtil;

public class EvolutionService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static Logger log2 = LoggerFactory.getLogger(EvolutionService.class);

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    ResultItemTable mlTimesTable = ServiceUtil.createMLTimesTable(otherTableMap);
    ResultItemTable eventTable = ServiceUtil.createEventTable(otherTableMap);

    public List<ResultItem> getEvolveRecommender(MyMyConfig conf, List<String> disableList, Map<String, Object> updateMap) throws JsonParseException, JsonMappingException, IOException {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        String market = conf.getMarket();
        ObjectMapper mapper = new ObjectMapper();
        EvolutionConfig evolutionConfig = mapper.readValue(conf.getTestIndictorrecommenderEvolutionConfig(), EvolutionConfig.class);
    
        //createOtherTables();
        StockData stockData = new ControlService().getStockData(conf);
        if (stockData == null) {
            return new ArrayList<>();
        }
        
    
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
            DataReader dataReader = new DataReader(conf, stockData.marketdatamap, stockData.cat);
            Pipeline[] datareaders = new Pipeline[1];
            datareaders[0] = dataReader;
    
            // no...get this from the category
            // make oo of this
            // optimize with constructors, no need for duplicate
            // map from type (complex/simple) to recommender and keysets
            Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
            Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
            int category = stockData.cat;
            Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();
            createRecommendIndicatorMap(stockData.marketdatamap, datareaders, usedRecommenders, indicatorMap, category,
                    newIndicatorMap);
            Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders, indicatorMap, conf);
    
            findRecommendSettings(conf, evolutionConfig, disableList, table, usedRecommenders, recommendKeyMap, indicatorMap, updateMap, stockData.days, datareaders);
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);
            return retlist;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            log.info("Market {}", conf.getMarket());
            return new ArrayList<>();
        }
    }
    
    private Double[] getThresholds(MyMyConfig conf, String thresholdString) {
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Double[].class);
    }

    private void findRecommendSettings(MyMyConfig conf, EvolutionConfig evolutionConfig, List<String> disableList, ResultItemTable table,
            Map<String, List<Recommend>> usedRecommenders, Map<String, List<String>[]> recommendKeyMap,
            Map<String, AbstractIndicator> indicatorMap, Map<String, Object> updateMap, int days, Pipeline[] datareaders) throws Exception {
        TaUtil tu = new TaUtil();
        String thresholdString = conf.getTestIndicatorRecommenderComplexThreshold();
        Double[] thresholds = getThresholds(conf, thresholdString);
        double threshold = thresholds[0];
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<AbstractIndicator> indicators = Recommend.getIndicators(entry.getKey(), usedRecommenders, indicatorMap);
            List<String>[] recommendList = recommendKeyMap.get(entry.getKey());
            Recommend recommend = entry.getValue().get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, recommend.getFutureDays(), conf.getTableDays(), recommend.getIntervalDays(), null, datareaders);
            List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
            if (macdrsiMinMax == null || macdrsiMinMax.length == 1) {
                int jj = 0;
            }
    
            for (int i = 0; i < 2; i++) {
                List<String> scoreList = recommendList[i];
                ////scoreList.removeAll(disableList);
                IndicatorChromosome indicatorEval0 = new IndicatorChromosome(conf, scoreList, retObj, true, disableList, new ProportionScore(i == 0), days, threshold);
                indicatorEval0.setAscending(i == 0);
                
                OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
    
                List<String> individuals = new ArrayList<>();
                Individual fittestIndividual = evolution.getFittest(evolutionConfig, indicatorEval0, individuals, null);
                String filename = evolution.print(conf.getMarket() + " " + "recommend" + " " + i, "recommend", individuals);
    
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

    @Deprecated
    public List<ResultItem> getEvolveRecommenderSingle(MyMyConfig conf, List<String> disableList, Map<String, Object> updateMap) throws JsonParseException, JsonMappingException, IOException {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        String market = conf.getMarket();
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
        String[] periodText = DbDaoUtil.getPeriodText(market, conf);
        MetaItem meta = null;
        try {
            meta = DbDao.getById(market, conf);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
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
            marketdatamap = new MarketDataETL().getMarketdatamap(days, market, conf, stocks, periodText, meta);
            Map<String, PeriodData> periodDataMap = new PeriodDataETL().getPerioddatamap(markets,
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
            DataReader dataReader = new DataReader(conf, marketdatamap, cat);
            Pipeline[] datareaders = new Pipeline[1];
            datareaders[0] = dataReader;
    
            // no...get this from the category
            // make oo of this
            // optimize with constructors, no need for duplicate
            // map from type (complex/simple) to recommender and keysets
            Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
            Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
            int category = cat;
            Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();
            createRecommendIndicatorMap(marketdatamap, datareaders, usedRecommenders, indicatorMap, category,
                    newIndicatorMap);
            Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders, indicatorMap, conf);
    
            findRecommendSettingsNew(conf, evolutionConfig, disableList, table, usedRecommenders, recommendKeyMap, indicatorMap, updateMap, datareaders);
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
            Map<String, AbstractIndicator> indicatorMap, Map<String, Object> updateMap, Pipeline[] datareaders) throws Exception {
        TaUtil tu = new TaUtil();
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<AbstractIndicator> indicators = Recommend.getIndicators(entry.getKey(), usedRecommenders, indicatorMap);
            List<String>[] recommendList = recommendKeyMap.get(entry.getKey());
            Recommend recommend = entry.getValue().get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, tu, indicators, recommend.getFutureDays(), conf.getTableDays(), recommend.getIntervalDays(), null, datareaders);
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
    
            Individual buysell = evolution.getFittest(evolutionConfig, recommend, null, null);
    
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

    public List<ResultItem> getEvolveML(MyMyConfig conf, List<String> disableList, Map<String, Object> updateMap, String ml, NeuralNetCommand neuralnetcommand, Map<String, Object> scoreMap, Map<String, Object> resultMap) throws JsonParseException, JsonMappingException, IOException {
        log.info("mydate {}", conf.getdate());
        log.info("mydate {}", conf.getDays());
        String market = conf.getMarket();
        if (conf.isDataset()) {
            ObjectMapper mapper = new ObjectMapper();
            EvolutionConfig evolutionConfig = mapper.readValue(conf.getTestMLEvolutionConfig(), EvolutionConfig.class);
            Set<String> markets = new HashSet<>();
            markets.add(conf.getMarket());
        
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
                findMLSettings(conf, evolutionConfig, table, updateMap, ml, neuralnetcommand, scoreMap, resultMap);
        
                List<ResultItem> retlist = new ArrayList<>();
                retlist.add(table);
                return retlist;
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                return new ArrayList<>();
            }
        }
        ObjectMapper mapper = new ObjectMapper();
        EvolutionConfig evolutionConfig = mapper.readValue(conf.getTestMLEvolutionConfig(), EvolutionConfig.class);
        //createOtherTables();
        StockData stockData = new ControlService().getStockData(conf);
        if (stockData == null) {
            return new ArrayList<>();
        }
    
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

            DataReader dataReader = new DataReader(conf, stockData.marketdatamap, stockData.cat);
            //Pipeline[] datareaders = new Pipeline[1];
            Pipeline[] datareaders = new ServiceUtil().getDataReaders(conf, stockData.periodText,
                    stockData.marketdatamap);
    
            //datareaders[0] = dataReader;
    
            SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
            String mydate = dt.format(conf.getdate());
            List<StockItem> dayStocks = stockData.stockdatemap.get(mydate);
            AbstractCategory[] categories = new ServiceUtil().getCategories(conf, dayStocks,
                    stockData.periodText, datareaders);
            AbstractPredictor[] predictors = new ServiceUtil().getPredictors(conf, stockData.periodText,
                    stockData.marketdatamap, stockData.datedstocklists, datareaders, categories, neuralnetcommand);
            //new ServiceUtil().createPredictors(categories);
            new ServiceUtil().calculatePredictors(predictors);

            findMLSettings(conf, evolutionConfig, disableList, table, updateMap, ml, datareaders, categories, stockData.catName, stockData.cat, neuralnetcommand, scoreMap, resultMap);
    
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
            Map<String, Object> updateMap, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat, NeuralNetCommand neuralnetcommand, Map<String, Object> scoreMap, Map<String, Object> resultMap) throws Exception {
        TaUtil tu = new TaUtil();
        log.info("Evolution config {} {} {} {}", evolutionConfig.getGenerations(), evolutionConfig.getSelect(), evolutionConfig.getElite(), evolutionConfig.getMutate());
        NeuralNetConfigs nnConfigs = null;
        String nnconfigString = null;
        /*
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
        if (ml.equals(PipelineConstants.MLRSI)) {
            nnconfigString = conf.getMLRSIMLConfig();
            if (nnconfigString != null) {
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
            }
        }
        if (ml.equals(PipelineConstants.MLMULTI)) {
            nnconfigString = conf.getAggregatorsMLMlmultiMLConfig();
            if (nnconfigString != null) {
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
            }
        }
        if (ml.equals(PipelineConstants.MLATR)) {
            nnconfigString = conf.getMLATRMLConfig();
            if (nnconfigString != null) {
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
            }
        }
        if (ml.equals(PipelineConstants.MLCCI)) {
            nnconfigString = conf.getMLCCIMLConfig();
            if (nnconfigString != null) {
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
            }
        }
        if (ml.equals(PipelineConstants.MLSTOCH)) {
            nnconfigString = conf.getMLSTOCHMLConfig();
            if (nnconfigString != null) {
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);            
            }
        }
        if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
            nnconfigString = conf.getTensorflowPredictorLSTMConfig();
            if (nnconfigString != null) {
                nnConfigs = new NeuralNetConfigs();
                log.info("NNConfig {}", nnconfigString);
                ObjectMapper mapper = new ObjectMapper();
                TensorflowPredictorLSTMConfig nnConfig = mapper.readValue(nnconfigString, TensorflowPredictorLSTMConfig.class);
                nnConfigs.setTensorflowConfig(new NeuralNetTensorflowConfig(null, null, null, null, null, null, null, null, nnConfig));
                //nnConfigs.getTensorflowConfig().setTensorflowPredictorLSTMConfig(nnConfig);
            }
        }
        */
        if (nnConfigs == null) {
            nnConfigs = new NeuralNetConfigs();            
        }
        //NeuralNetConfigs newNNConfigs = new NeuralNetConfigs();
        List<String> foundkeys = getFoundKeys(conf, nnConfigs);
            /*
            MyMyConfig workingConf = conf.copy();
            for (String tmpkey : keys) {
                boolean enabled = (boolean) workingConf.getValueOrDefault(tmpkey);
                boolean sameKey = key.equals(tmpkey);
                sameKey &= enabled;
                workingConf.getConfigValueMap().put(tmpkey, sameKey);
            }
            */
        for (String key : foundkeys) {
            String configKey = nnConfigs.getConfigMap().get(key);
            String configValue = (String) conf.getValueOrDefault(configKey);
            
            NeuralNetConfig nnconfig = nnConfigs.getAndSetConfig(key, configValue);
            NeuralNetConfigGene nnconfigGene = NeuralNetConfigGeneFactory.get(nnconfig, key);
            NeuralNetChromosome2 chromosome = new NeuralNetChromosome2(nnconfigGene);
            // no, now predictor got accuracy
            if (ml.equals(PipelineConstants.PREDICTOR)) {
                //chromosome.setAscending(false);
            }

            FitnessNeuralNet fitness = new FitnessNeuralNet(conf, ml, dataReaders, categories, key, catName, cat, neuralnetcommand);
    
            OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
            evolution.setParallel(false);
            evolution.fittest = fitness::fitness;
            
            List<String> individuals = new ArrayList<>();
            List<Pair<Double, AbstractChromosome>> results = new ArrayList<>();
            Individual best = evolution.getFittest(evolutionConfig, chromosome, individuals, results);
            String title = EvolveConstants.EVOLVE + " " + conf.getMarket() + " " + ml + " " + nnconfig.getClass().getSimpleName();
            String filename = evolution.print(title, null, individuals);
                        
            NeuralNetChromosome2 bestEval2 = (NeuralNetChromosome2) best.getEvaluation();
            NeuralNetConfigGene newnnconfgene = bestEval2.getNnConfig();
            NeuralNetConfig newnnconf = newnnconfgene.getConfig();
            //newNNConfigs.set(key, newnnconf);
            ObjectMapper mapper = new ObjectMapper();
            String newNNConfigstring = mapper.writeValueAsString(newnnconf);
            String myKey = null;
            if (ml.equals(PipelineConstants.MLINDICATOR)) {
                myKey = ConfigConstants.AGGREGATORSINDICATORMLCONFIG;
            }
            if (ml.equals(PipelineConstants.MLMACD)) {
                myKey = ConfigConstants.AGGREGATORSMLMACDMLCONFIG;
            }
            if (ml.equals(PipelineConstants.MLRSI)) {
                myKey = ConfigConstants.AGGREGATORSMLRSIMLCONFIG;
            }
            if (ml.equals(PipelineConstants.MLMULTI)) {
                myKey = ConfigConstants.AGGREGATORSMLMULTIMLCONFIG;
            }
            if (ml.equals(PipelineConstants.MLATR)) {
                myKey = ConfigConstants.AGGREGATORSMLATRMLCONFIG;
            }
            if (ml.equals(PipelineConstants.MLCCI)) {
                myKey = ConfigConstants.AGGREGATORSMLCCIMLCONFIG;
            }
            if (ml.equals(PipelineConstants.MLSTOCH)) {
                myKey = ConfigConstants.AGGREGATORSMLSTOCHMLCONFIG;
            }
            if (ml.equals(PipelineConstants.PREDICTOR)) {
                myKey = nnConfigs.getConfigMap().get(key);
                myKey = ConfigConstants.MACHINELEARNINGPREDICTORSMLCONFIG;
                //newNNConfigstring = mapper.writeValueAsString(newNNConfigs.getTensorflowConfig().getTensorflowLSTMConfig());
            }
            updateMap.put(configKey, newNNConfigstring);
            scoreMap.put(configKey, best.getFitness());
            resultMap.put(filename, results);
            resultMap.put(EvolveConstants.ID, filename);
            resultMap.put(EvolveConstants.TITLETEXT, title);
            resultMap.put(EvolveConstants.DEFAULT, nnconfig);
            //resultMap.put("id", filename);
            ResultItemTableRow row = new ResultItemTableRow();
            row.add(myKey);
            row.add(nnconfigString);
            row.add(newnnconf);
            table.add(row);
        }
    }

    private void findMLSettings(MyMyConfig conf, EvolutionConfig evolutionConfig, ResultItemTable table, Map<String, Object> updateMap,
            String ml, NeuralNetCommand neuralnetcommand, Map<String, Object> scoreMap, Map<String, Object> resultMap) throws Exception {
        log.info("Evolution config {} {} {} {}", evolutionConfig.getGenerations(), evolutionConfig.getSelect(), evolutionConfig.getElite(), evolutionConfig.getMutate());
        NeuralNetConfigs nnConfigs = null;
        String nnconfigString = null;
        if (nnConfigs == null) {
            nnConfigs = new NeuralNetConfigs();            
        }
        List<String> foundkeys = getFoundKeys(conf, nnConfigs);
        for (String key : foundkeys) {
            String configKey = nnConfigs.getConfigMap().get(key);
            String configValue = (String) conf.getValueOrDefault(configKey);
            
            NeuralNetConfig nnconfig = nnConfigs.getAndSetConfig(key, configValue);
            NeuralNetConfigGene nnconfigGene = NeuralNetConfigGeneFactory.get(nnconfig, key);
            NeuralNetChromosome2 chromosome = new NeuralNetChromosome2(nnconfigGene);
            if (configKey.contains(PipelineConstants.PREDICTOR)) {
                chromosome.setAscending(false);
            }
    
            FitnessNeuralNet fitness = new FitnessNeuralNet(conf, ml, null, null, key, null, 0, neuralnetcommand);

            OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
            evolution.setParallel(false);
            evolution.fittest = fitness::fitness;
            
            List<String> individuals = new ArrayList<>();
            List<Pair<Double, AbstractChromosome>> results = new ArrayList<>();
            Individual best = evolution.getFittest(evolutionConfig, chromosome, individuals, null);
            String title = EvolveConstants.EVOLVE + " " + conf.getMarket() + " " + ml + " " + nnconfig.getClass().getSimpleName();
            String filename = evolution.print(title, null, individuals);
    
            NeuralNetChromosome2 bestEval2 = (NeuralNetChromosome2) best.getEvaluation();
            NeuralNetConfigGene newnnconfgene = bestEval2.getNnConfig();
            NeuralNetConfig newnnconf = newnnconfgene.getConfig();
            ObjectMapper mapper = new ObjectMapper();
            String newNNConfigstring = mapper.writeValueAsString(newnnconf);
            String myKey = null;
            if (ml.equals(PipelineConstants.DATASET)) {
                myKey = ConfigConstants.DATASETMLCONFIG;                
            }
            updateMap.put(configKey, newNNConfigstring);
            scoreMap.put(configKey, best.getFitness());
            resultMap.put(filename, results);
            resultMap.put(EvolveConstants.TITLETEXT, title);
            resultMap.put(EvolveConstants.ID, filename);
            resultMap.put(EvolveConstants.DEFAULT, nnconfig);
            //resultMap.put("id", filename);
            ResultItemTableRow row = new ResultItemTableRow();
            row.add(myKey);
            row.add(nnconfigString);
            row.add(newnnconf);
            table.add(row);
        }
    }

    public static List<String> getFoundKeys(MyMyConfig conf, NeuralNetConfigs nnConfigs) {
        List<String> keys = getMLkeys();
        
        List<String> foundkeys = new ArrayList<>();
        for (String key : keys) {
            //System.out.println(conf.getValueOrDefault(key));
            if (!Boolean.TRUE.equals(conf.getConfigValueMap().get(key))) {
                continue;
            }
        
            Map<String, String> anotherConfigMap = nnConfigs.getAnotherConfigMap();
            if (!Boolean.TRUE.equals(conf.getConfigValueMap().get(anotherConfigMap.get(key)))) {
                continue;
            }
            foundkeys.add(key);
        }
        if (foundkeys.size() != 1) {
            log2.error("Foundkeys size {}", foundkeys.size());
        }
        return foundkeys;
    }

    private static List<String> getMLkeys() {
        List<String> keys = new ArrayList<>();
        keys.add(ConfigConstants.MACHINELEARNINGSPARKMLLOR);
        keys.add(ConfigConstants.MACHINELEARNINGSPARKMLMLPC);
        keys.add(ConfigConstants.MACHINELEARNINGSPARKMLOVR);
        keys.add(ConfigConstants.MACHINELEARNINGSPARKMLLSVC);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU);
        keys.add(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM);
        //keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM);
        keys.add(ConfigConstants.MACHINELEARNINGPYTORCHMLP);
        keys.add(ConfigConstants.MACHINELEARNINGPYTORCHCNN);
        keys.add(ConfigConstants.MACHINELEARNINGPYTORCHCNN2);
        keys.add(ConfigConstants.MACHINELEARNINGPYTORCHRNN);
        keys.add(ConfigConstants.MACHINELEARNINGPYTORCHGRU);
        keys.add(ConfigConstants.MACHINELEARNINGPYTORCHLSTM);
        keys.add(ConfigConstants.MACHINELEARNINGGEMEWC);
        keys.add(ConfigConstants.MACHINELEARNINGGEMGEM);
        keys.add(ConfigConstants.MACHINELEARNINGGEMICARL);
        keys.add(ConfigConstants.MACHINELEARNINGGEMINDEPENDENT);
        keys.add(ConfigConstants.MACHINELEARNINGGEMMULTIMODAL);
        keys.add(ConfigConstants.MACHINELEARNINGGEMSINGLE);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU);
        keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM);
        return keys;
    }

}

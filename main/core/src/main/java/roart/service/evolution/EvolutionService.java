package roart.service.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregatorindicator.impl.Recommend;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.JsonUtil;
import roart.db.dao.DbDao;
import roart.etl.db.Extract;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.impl.IndicatorChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.impl.ProportionScore;
import roart.evolution.species.Individual;
import roart.filesystem.FileSystemDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceResult;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.DataReader;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.service.ControlService;
import roart.service.util.ServiceUtil;

public class EvolutionService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static Logger log2 = LoggerFactory.getLogger(EvolutionService.class);

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    ResultItemTable mlTimesTable = ServiceUtil.createMLTimesTable(otherTableMap);
    ResultItemTable eventTable = ServiceUtil.createEventTable(otherTableMap);
    private DbDao dao;

    public EvolutionService(DbDao dao) {
        this.dao = dao;
    }

    public IclijServiceResult getEvolveRecommender(IclijConfig conf, List<String> disableList) throws JsonParseException, JsonMappingException, IOException {
        IclijServiceResult result = new IclijServiceResult();
        Map<String, Object> updateMap = new HashMap<>();
        Map<String, Object> scoreMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        EvolutionConfig evolutionConfig = JsonUtil.convertnostrip(conf.getEvolveIndicatorrecommenderEvolutionConfig(), EvolutionConfig.class);
    
        StockData stockData = new Extract(dao).getStockData(conf);
        if (stockData == null) {
            return result;
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
            DataReader dataReader = new DataReader(conf, stockData.marketdatamap, stockData.cat, conf.getConfigData().getMarket());
            Pipeline[] datareaders = new Pipeline[1];
            datareaders[0] = dataReader;
            PipelineData singlePipelineData = new PipelineData();
            singlePipelineData.setName(PipelineConstants.META);
            singlePipelineData.put(PipelineConstants.META, stockData.marketdatamap.get(conf.getConfigData().getMarket()).meta);
            singlePipelineData.put(PipelineConstants.CATEGORY, stockData.catName);
            singlePipelineData.put(PipelineConstants.WANTEDCAT, stockData.cat);
            singlePipelineData.put(PipelineConstants.NAME, stockData.idNameMap);
            singlePipelineData.put(PipelineConstants.DATELIST, stockData.stockdates);
            PipelineData[] pipelineData = new PipelineData[0];
            
            // no...get this from the category
            // make oo of this
            // optimize with constructors, no need for duplicate
            // map from type (complex/simple) to recommender and keysets
            Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
            Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
            int category = stockData.cat;
            Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();
            createRecommendIndicatorMap(stockData.marketdatamap, pipelineData, usedRecommenders, indicatorMap, category,
                    newIndicatorMap);
            Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders, indicatorMap, conf);
    
            findRecommendSettings(conf, evolutionConfig, disableList, table, usedRecommenders, recommendKeyMap, indicatorMap, updateMap, stockData.days, pipelineData, scoreMap, resultMap);
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);

            result.setList(retlist);
            PipelineData datum = getEvolveData(updateMap, scoreMap, resultMap);
            pipelineData = ArrayUtils.add(pipelineData, datum);
            result.setPipelineData(pipelineData);
            result.setConfigData(conf.getConfigData());

            return result;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            log.info("Market {}", conf.getConfigData().getMarket());
            return result;
        }
    }
    
    private PipelineData getEvolveData(Map<String, Object> updateMap, Map<String, Object> scoreMap,
            Map<String, Object> resultMap) {
        PipelineData maps = new PipelineData();
        maps.setName(PipelineConstants.EVOLVE);
        maps.put(PipelineConstants.UPDATE, updateMap);
        maps.put(PipelineConstants.SCORE, scoreMap);
        maps.put(PipelineConstants.RESULT, resultMap);
        return maps;
    }

    private Double[] getThresholds(IclijConfig conf, String thresholdString) {
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Double[].class);
    }

    private void findRecommendSettings(IclijConfig conf, EvolutionConfig evolutionConfig, List<String> disableList, ResultItemTable table,
            Map<String, List<Recommend>> usedRecommenders, Map<String, List<String>[]> recommendKeyMap,
            Map<String, AbstractIndicator> indicatorMap, Map<String, Object> updateMap, int days, PipelineData[] datareaders, Map<String, Object> scoreMap, Map<String, Object> resultMap) throws Exception {
        String thresholdString = conf.getTestIndicatorRecommenderComplexThreshold();
        Double[] thresholds = getThresholds(conf, thresholdString);
        double threshold = thresholds[0];
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<AbstractIndicator> indicators = Recommend.getIndicators(entry.getKey(), usedRecommenders, indicatorMap);
            List<String>[] recommendList = recommendKeyMap.get(entry.getKey());
            Recommend recommend = entry.getValue().get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, indicators.stream().map(AbstractIndicator::indicatorName).toList(), recommend.getFutureDays(), conf.getTableDays(), recommend.getIntervalDays(), null, datareaders);
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
                evolution.setParallel(false);
    
                List<String> individuals = new ArrayList<>();
                Individual fittestIndividual = null;
                try {
                	fittestIndividual = evolution.getFittest(evolutionConfig, indicatorEval0, individuals, null, null);
                } catch (InterruptedException e) {
                    resultMap.put(EvolveConstants.ID, "interrupted");
                    return;
                }
                String text = evolution.printtext(conf.getConfigData().getMarket() + " " + "recommend" + " " + i, "recommend", individuals);
                String node = conf.getEvolveSaveLocation();
                String mypath = conf.getEvolveSavePath();
                ControlService.configCurator(conf);
                String filename = new FileSystemDao(conf, ControlService.curatorClient).writeFile(node, mypath, null, text);
    
                for (String id : scoreList) {
                    ResultItemTableRow row = new ResultItemTableRow();
                    row.add(id);
                    row.add("" + conf.getConfigData().getConfigValueMap().get(id));
                    //log.info("Buy {} {}", id, buy.getConf().getConfigValueMap().get(id));
                    //log.info("Buy {}", buy.getConf().getConfigValueMap().get(id).getClass().getName());
                    IndicatorChromosome newEval = (IndicatorChromosome) fittestIndividual.getEvaluation();
                    row.add("" + newEval.getConf().getConfigData().getConfigValueMap().get(id));
                    table.add(row);
                }
                // have a boolean here
                for (String id : scoreList) {
                    IndicatorChromosome newEval = (IndicatorChromosome) fittestIndividual.getEvaluation();
                    updateMap.put(id, newEval.getConf().getConfigData().getConfigValueMap().get(id));
                }
                if (i == 0) {
                    List<Double> scorelist2 = new ArrayList<>();
                    Double fitness = fittestIndividual.getFitness();
                    if (!fitness.isNaN()) {
                        scorelist2.add(fitness);                    
                        scoreMap.put("score", fitness);
                        scoreMap.put("scores", scorelist2);            
                    }
                }
            }
        }
    }

    private void createRecommendIndicatorMap(Map<String, MarketData> marketdatamap, PipelineData[] datareaders,
            Map<String, List<Recommend>> usedRecommenders, Map<String, AbstractIndicator> indicatorMap, int category,
            Map<String, AbstractIndicator> newIndicatorMap) throws Exception {
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<Recommend> list = entry.getValue();
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                indicatorMap.put(indicator, recommend.getIndicator(category, newIndicatorMap, null, datareaders));
            }
        }
    }

}

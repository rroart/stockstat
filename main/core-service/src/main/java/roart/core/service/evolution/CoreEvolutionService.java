package roart.core.service.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DatabindException;

import roart.aggregatorindicator.impl.Recommend;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.MetaDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialPipeline;
import roart.common.pipeline.data.SerialInteger;
import roart.common.pipeline.data.SerialListMap;
import roart.common.pipeline.data.SerialListPlain;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.data.SerialMeta;
import roart.common.pipeline.data.SerialString;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.JsonUtil;
import roart.core.service.util.ServiceUtil;
import roart.etl.db.Extract;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
//import roart.evolution.chromosome.impl.IndicatorChromosome;
import roart.evolution.chromosome.impl.IndicatorChromosome3;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.impl.ProportionScore;
import roart.evolution.species.Individual;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.indicator.AbstractIndicator;
import roart.indicator.impl.Indicator;
import roart.indicator.util.IndicatorUtils;
import roart.model.data.MarketData;
import roart.model.data.StockData;
import roart.pipeline.Pipeline;
import roart.pipeline.impl.DataReader;
import roart.result.model.ResultItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.model.io.IO;

public class CoreEvolutionService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    Map<Integer, ResultItemTable> otherTableMap = new HashMap<>();

    ResultItemTable mlTimesTable = ServiceUtil.createMLTimesTable(otherTableMap);
    ResultItemTable eventTable = ServiceUtil.createEventTable(otherTableMap);

    private IO io;
    
    public CoreEvolutionService(IO io) {
        this.io = io;
    }

    public IclijServiceResult getEvolveRecommender(IclijConfig conf, List<String> disableList, IclijServiceParam origparam) throws StreamReadException, DatabindException, IOException {
        Inmemory inmemory = io.getInmemoryFactory().get(conf);
        IclijServiceResult result = new IclijServiceResult();
        Map<String, Object> updateMap = new HashMap<>();
        Map<String, Object> scoreMap = new HashMap<>();
        SerialPipeline resultMap = new SerialPipeline();
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        EvolutionConfig evolutionConfig = JsonUtil.convertnostrip(conf.getEvolveIndicatorrecommenderEvolutionConfig(), EvolutionConfig.class);
    
        StockData stockData = new Extract(io.getDbDao()).getStockData(conf, true);
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
            SerialPipeline list = new SerialPipeline();
            //singlePipelineData.setName(PipelineConstants.META);
            MetaDTO meta = stockData.marketdatamap.get(conf.getConfigData().getMarket()).meta;
            list.add(new PipelineData(PipelineConstants.META, PipelineConstants.META, null, new SerialMeta(meta.getMarketid(), meta.getPeriod(), meta.getPriority(), meta.getReset(), meta.isLhc()), false));
            list.add(new PipelineData(PipelineConstants.META, PipelineConstants.CATEGORY, null, new SerialString(stockData.catName), false));
            list.add(new PipelineData(PipelineConstants.META, PipelineConstants.WANTEDCAT, null, new SerialInteger(stockData.cat), false));
            list.add(new PipelineData(PipelineConstants.META, PipelineConstants.NAME, null, new SerialMapPlain(stockData.idNameMap), true));
            list.add(new PipelineData(PipelineConstants.META, PipelineConstants.DATELIST, null, new SerialListPlain(stockData.stockdates), true));
            SerialPipeline pipelineData = new SerialPipeline();
            pipelineData.add(list);
   
            for (Pipeline datareader : datareaders) {
                pipelineData.add(datareader.putData());
            }

            // no...get this from the category
            // make oo of this
            // optimize with constructors, no need for duplicate
            // map from type (complex/simple) to recommender and keysets
            Map<String, List<Recommend>> usedRecommenders = Recommend.getUsedRecommenders(conf);
            Map<String, AbstractIndicator> indicatorMap = new HashMap<>();
            int category = stockData.cat;
            Map<String, AbstractIndicator> newIndicatorMap = new HashMap<>();
            createRecommendIndicatorMap(stockData.marketdatamap, pipelineData, usedRecommenders, indicatorMap, category,
                    newIndicatorMap, stockData.catName, inmemory);
            for (AbstractIndicator indicator : indicatorMap.values()) {
                ((Indicator) indicator).calculate();
                SerialPipeline datum = ((Indicator) indicator).putData();
                pipelineData.add(datum);
            }
            Map<String, List<String>[]> recommendKeyMap = Recommend.getRecommenderKeyMap(usedRecommenders, indicatorMap, conf, Boolean.TRUE.equals(meta.isLhc()));
            
            findRecommendSettings(conf, evolutionConfig, disableList, table, usedRecommenders, recommendKeyMap, indicatorMap, updateMap, stockData.days, pipelineData, scoreMap, resultMap, inmemory);
            List<ResultItem> retlist = new ArrayList<>();
            retlist.add(table);

            result.setList(retlist);
            SerialPipeline datum = getEvolveData(updateMap, scoreMap, resultMap);
            pipelineData.add(datum);
            if (origparam.getId() != null) {
                log.info("Before setPipelineMap");
                PipelineUtils.setPipelineMap(pipelineData, origparam.getId());
                pipelineData = PipelineUtils.setPipelineMap(pipelineData, inmemory, io.getCuratorClient());
            }
            result.setPipelineData(pipelineData);
            result.setConfigData(conf.getConfigData());

            PipelineUtils.printkeys(pipelineData);

            return result;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            log.info("Market {}", conf.getConfigData().getMarket());
            return result;
        }
    }
    
    private SerialPipeline getEvolveData(Map<String, Object> updateMap, Map<String, Object> scoreMap,
            SerialPipeline resultMap) {
        SerialPipeline list = new SerialPipeline();
        //maps.setName(PipelineConstants.EVOLVE);
        list.add(new PipelineData(PipelineConstants.EVOLVE, PipelineConstants.UPDATE, null, new SerialMapPlain(updateMap), false));
        list.add(new PipelineData(PipelineConstants.EVOLVE, PipelineConstants.SCORE, null, new SerialMapPlain(scoreMap), false));
        // rec with own result
        list.add(resultMap);
        return list;
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
            Map<String, AbstractIndicator> indicatorMap, Map<String, Object> updateMap, int days, SerialPipeline datareaders, Map<String, Object> scoreMap, SerialPipeline resultMap, Inmemory inmemory) throws Exception {
        String thresholdString = conf.getTestIndicatorRecommenderComplexThreshold();
        Double[] thresholds = getThresholds(conf, thresholdString);
        double threshold = thresholds[0];
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<AbstractIndicator> indicators = Recommend.getIndicators(entry.getKey(), usedRecommenders, indicatorMap);
            List<String>[] recommendList = recommendKeyMap.get(entry.getKey());
            Recommend recommend = entry.getValue().get(0);
            Object[] retObj = IndicatorUtils.getDayIndicatorMap(conf, indicators.stream().map(AbstractIndicator::indicatorName).toList(), 0, conf.getTableDays(), 1, null, datareaders, inmemory);
            List<Double>[] macdrsiMinMax = (List<Double>[]) retObj[1];
            if (macdrsiMinMax == null || macdrsiMinMax.length == 1) {
                int jj = 0;
            }
            // TODO buy only
            for (int i = 0; i < 2; i++) {
                List<String> scoreList = recommendList[i];
                ////scoreList.removeAll(disableList);
                IndicatorChromosome3 indicatorEval0 = new IndicatorChromosome3(conf, scoreList, retObj, true, disableList, new ProportionScore(i == 0), days, threshold);
                indicatorEval0.setAscending(i == 0);
                
                OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
                evolution.setParallel(false);
    
                List<String> individuals = new ArrayList<>();
                Individual fittestIndividual = null;
                try {
                	fittestIndividual = evolution.getFittest(evolutionConfig, indicatorEval0, individuals, null, null);
                } catch (InterruptedException e) {
                    resultMap.add(new PipelineData(PipelineConstants.EVOLVE, PipelineConstants.RESULT, EvolveConstants.ID, new SerialString("interrupted"), false));
                    //resultMap.put(EvolveConstants.ID, "interrupted");
                    return;
                }
                //resultMap.put(EvolveConstants.ID, "dummy");
                String text = evolution.printtext(conf.getConfigData().getMarket() + " " + "recommend" + " " + i, "recommend", individuals);
                String node = conf.getEvolveSaveLocation();
                String mypath = conf.getEvolveSavePath();
                // TODO? String filename = new FileSystemDao(conf, CoreControlService.curatorClient).writeFile(node, mypath, null, text);
    
                for (String id : scoreList) {
                    ResultItemTableRow row = new ResultItemTableRow();
                    row.add(id);
                    row.add("" + conf.getConfigData().getConfigValueMap().get(id));
                    //log.info("Buy {} {}", id, buy.getConf().getConfigValueMap().get(id));
                    //log.info("Buy {}", buy.getConf().getConfigValueMap().get(id).getClass().getName());
                    IndicatorChromosome3 newEval = (IndicatorChromosome3) fittestIndividual.getEvaluation();
                    row.add("" + newEval.getConf().getConfigData().getConfigValueMap().get(id));
                    table.add(row);
                }
                // have a boolean here
                for (String id : scoreList) {
                    IndicatorChromosome3 newEval = (IndicatorChromosome3) fittestIndividual.getEvaluation();
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

    private void createRecommendIndicatorMap(Map<String, MarketData> marketdatamap, SerialPipeline datareaders,
            Map<String, List<Recommend>> usedRecommenders, Map<String, AbstractIndicator> indicatorMap, int category,
            Map<String, AbstractIndicator> newIndicatorMap, String catName, Inmemory inmemory) throws Exception {
        for (Entry<String, List<Recommend>> entry : usedRecommenders.entrySet()) {
            List<Recommend> list = entry.getValue();
            for (Recommend recommend : list) {
                String indicator = recommend.indicator();
                indicatorMap.put(indicator, recommend.getIndicator(category, newIndicatorMap, null, datareaders, catName, inmemory));
            }
        }
    }

}

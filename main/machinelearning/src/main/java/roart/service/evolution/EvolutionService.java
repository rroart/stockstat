package roart.service.evolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMapPlain;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.JsonUtil;
import roart.evolution.algorithm.impl.OrdinaryEvolution;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.species.Individual;
import roart.filesystem.FileSystemDao;
import roart.gene.NeuralNetConfigGene;
import roart.gene.NeuralNetConfigGeneFactory;
import roart.iclij.config.IclijConfig;
import roart.iclij.service.IclijServiceParam;
import roart.iclij.service.IclijServiceResult;
import roart.model.data.StockData;
import roart.pipeline.common.predictor.AbstractPredictor;
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

    public EvolutionService() {
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

    public IclijServiceResult getEvolveML(List<String> disableList, String ml, IclijServiceParam origparam) throws JsonParseException, JsonMappingException, IOException {
        Map<String, Object> updateMap = new HashMap<>();
        Map<String, Object> scoreMap = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        IclijConfig conf = new IclijConfig(origparam.getConfigData());
        NeuralNetCommand neuralnetcommand = origparam.getNeuralnetcommand();
        log.info("mydate {}", conf.getConfigData().getDate());
        log.info("mydate {}", conf.getDays());
        if (conf.getConfigData().isDataset()) {
            EvolutionConfig evolutionConfig = JsonUtil.convertnostrip(conf.getEvolveMLEvolutionConfig(), EvolutionConfig.class);
            Set<String> markets = new HashSet<>();
            markets.add(conf.getConfigData().getMarket());
        
            List<ResultItemTable> otherTables = new ArrayList<>();
            otherTables.add(mlTimesTable);
            otherTables.add(eventTable);
        
            ResultItemTable table = new ResultItemTable();
            ResultItemTableRow headrow = new ResultItemTableRow();
            headrow.add("Config");
            headrow.add("Old value");
            headrow.add("New value");
            table.add(headrow);
        
            IclijServiceResult result = new IclijServiceResult();
            try {
                findMLSettings(conf, evolutionConfig, table, updateMap, ml, neuralnetcommand, scoreMap, resultMap);
        
                List<ResultItem> retlist = new ArrayList<>();
                retlist.add(table);
                result.setList(retlist);
                result.setConfigData(conf.getConfigData());
                return result;
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
                return result;
            }
        }
        EvolutionConfig evolutionConfig = JsonUtil.convertnostrip(conf.getEvolveMLEvolutionConfig(), EvolutionConfig.class);
    
        List<ResultItemTable> otherTables = new ArrayList<>();
        otherTables.add(mlTimesTable);
        otherTables.add(eventTable);
    
        ResultItemTable table = new ResultItemTable();
        ResultItemTableRow headrow = new ResultItemTableRow();
        headrow.add("Config");
        headrow.add("Old value");
        headrow.add("New value");
        table.add(headrow);
    
        IclijServiceResult result = new ControlService().getContent(conf, origparam, disableList);
        
        List<ResultItem> retlist = result.getList();
        PipelineData[] pipelineData = result.getPipelineData();

        try {

            PipelineData[] datareaders = pipelineData;
    
            StockData stockData = getStockData(pipelineData);
            
            AbstractPredictor[] predictors = new ServiceUtil().getPredictors(conf, datareaders,
                    stockData.catName, stockData.cat, neuralnetcommand);
            //new ServiceUtil().createPredictors(categories);
            new ServiceUtil().calculatePredictors(predictors);

            findMLSettings(conf, evolutionConfig, disableList, table, updateMap, ml, datareaders, stockData.catName, stockData.cat, neuralnetcommand, scoreMap, resultMap);
    
            retlist.add(table);
            result.setList(retlist);
            PipelineData datum = getEvolveData(updateMap, scoreMap, resultMap);
            pipelineData = ArrayUtils.add(pipelineData, datum);
            result.setPipelineData(pipelineData);
            result.setConfigData(conf.getConfigData());
           return result;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }
    }

    private PipelineData getEvolveData(Map<String, Object> updateMap, Map<String, Object> scoreMap,
            Map<String, Object> resultMap) {
        PipelineData maps = new PipelineData();
        maps.setName(PipelineConstants.EVOLVE);
        maps.put(PipelineConstants.UPDATE, new SerialMapPlain(updateMap));
        maps.put(PipelineConstants.SCORE, new SerialMapPlain(scoreMap));
        maps.put(PipelineConstants.RESULT, new SerialMapPlain(resultMap));
        return maps;
    }

    private void findMLSettings(IclijConfig conf, EvolutionConfig evolutionConfig, List<String> disableList, ResultItemTable table,
            Map<String, Object> updateMap, String ml, PipelineData[] dataReaders, String catName, Integer cat, NeuralNetCommand neuralnetcommand, Map<String, Object> scoreMap, Map<String, Object> resultMap) throws Exception {
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
            NeuralNetChromosome chromosome = new NeuralNetChromosome(nnconfigGene);
            // no, now predictor got accuracy
            if (ml.equals(PipelineConstants.PREDICTOR)) {
                //chromosome.setAscending(false);
            }

            FitnessNeuralNet fitness = new FitnessNeuralNet(conf, ml, dataReaders, key, catName, cat, neuralnetcommand);
    
            OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
            evolution.setParallel(false);
            evolution.fittest = fitness::fitness;
            
            List<String> individuals = new ArrayList<>();
            List<Pair<Double, AbstractChromosome>> results = new ArrayList<>();
            Individual best = null;
            try {
            	best = evolution.getFittest(evolutionConfig, chromosome, individuals, results, null);
            } catch (InterruptedException e) {
                resultMap.put(EvolveConstants.ID, "interrupted");
                return;
            }
            String title = EvolveConstants.EVOLVE + " " + conf.getConfigData().getMarket() + " " + ml + " " + nnconfig.getClass().getSimpleName();
            String text = evolution.printtext(title, null, individuals);
            String node = conf.getEvolveSaveLocation();
            String mypath = conf.getEvolveSavePath();
            ControlService.configCurator(conf);
            String filename = new FileSystemDao(conf, ControlService.curatorClient).writeFile(node, mypath, null, text);
                     
            NeuralNetChromosome bestEval2 = (NeuralNetChromosome) best.getEvaluation();
            NeuralNetConfigGene newnnconfgene = bestEval2.getNnConfig();
            NeuralNetConfig newnnconf = newnnconfgene.getConfig();
            //newNNConfigs.set(key, newnnconf);
            String newNNConfigstring = JsonUtil.convert(newnnconf);
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
            scoreMap.put("scores", results.stream().map(Pair::getLeft).collect(Collectors.toList()));            
            resultMap.put(filename, results);
            resultMap.put(EvolveConstants.ID, filename);
            resultMap.put(EvolveConstants.TITLETEXT, title);
            resultMap.put(EvolveConstants.DEFAULT, nnconfig);

            ResultItemTableRow row = new ResultItemTableRow();
            row.add(myKey);
            row.add(nnconfigString);
            row.add(newnnconf);
            table.add(row);
        }
    }

    private void findMLSettings(IclijConfig conf, EvolutionConfig evolutionConfig, ResultItemTable table, Map<String, Object> updateMap,
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
            NeuralNetChromosome chromosome = new NeuralNetChromosome(nnconfigGene);
            if (configKey.contains(PipelineConstants.PREDICTOR)) {
                chromosome.setAscending(false);
            }
    
            FitnessNeuralNet fitness = new FitnessNeuralNet(conf, ml, null, key, null, 0, neuralnetcommand);

            OrdinaryEvolution evolution = new OrdinaryEvolution(evolutionConfig);
            evolution.setParallel(false);
            evolution.fittest = fitness::fitness;
            
            List<String> individuals = new ArrayList<>();
            List<Pair<Double, AbstractChromosome>> results = new ArrayList<>();
            Individual best = null;
            try {
            	best = evolution.getFittest(evolutionConfig, chromosome, individuals, results, null);
            } catch (InterruptedException e) {
                resultMap.put(EvolveConstants.ID, "interrupted");
            	return;
            }
            String title = EvolveConstants.EVOLVE + " " + conf.getConfigData().getMarket() + " " + ml + " " + nnconfig.getClass().getSimpleName();
            String text = evolution.printtext(title, null, individuals);
            String node = conf.getEvolveSaveLocation();
            String mypath = conf.getEvolveSavePath();
            ControlService.configCurator(conf);
            String filename = new FileSystemDao(conf, ControlService.curatorClient).writeFile(node, mypath, null, text);
            
            NeuralNetChromosome bestEval2 = (NeuralNetChromosome) best.getEvaluation();
            NeuralNetConfigGene newnnconfgene = bestEval2.getNnConfig();
            NeuralNetConfig newnnconf = newnnconfgene.getConfig();
            String newNNConfigstring = JsonUtil.convert(newnnconf);
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

            ResultItemTableRow row = new ResultItemTableRow();
            row.add(myKey);
            row.add(nnconfigString);
            row.add(newnnconf);
            table.add(row);
        }
    }

    public static List<String> getFoundKeys(IclijConfig conf, NeuralNetConfigs nnConfigs) {
        List<String> keys = getMLkeys();
        
        List<String> foundkeys = new ArrayList<>();
        for (String key : keys) {
            if (!Boolean.TRUE.equals(conf.getConfigData().getConfigValueMap().get(key))) {
                continue;
            }
        
            Map<String, String> anotherConfigMap = nnConfigs.getAnotherConfigMap();
            if (!Boolean.TRUE.equals(conf.getConfigData().getConfigValueMap().get(anotherConfigMap.get(key)))) {
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

    public StockData getStockData(PipelineData[] pipelineData) {
        StockData stockData = new StockData();
        PipelineData pipelineDatum = PipelineUtils.getPipeline(pipelineData, PipelineConstants.META);
        stockData.cat = PipelineUtils.getWantedcat(pipelineDatum);
        stockData.catName = PipelineUtils.getMetaCat(pipelineDatum);
        return stockData;
    }

}

package roart.service.evolution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLDataset;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.category.AbstractCategory;
import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.evolution.chromosome.impl.NeuralNetChromosome2;
import roart.gene.NeuralNetConfigGene;
import roart.model.data.MarketData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.predictor.impl.PredictorPytorchGRU;
import roart.predictor.impl.PredictorPytorchLSTM;
import roart.predictor.impl.PredictorPytorchMLP;
import roart.predictor.impl.PredictorPytorchRNN;
import roart.predictor.impl.PredictorTensorflowGRU;
import roart.predictor.impl.PredictorTensorflowLIR;
import roart.predictor.impl.PredictorTensorflowLSTM;
import roart.predictor.impl.PredictorTensorflowMLP;
import roart.predictor.impl.PredictorTensorflowRNN;

public class PipelineFactory {
    public PipelineResultData myfactory(IclijConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat, NeuralNetCommand neuralnetcommand, NeuralNetChromosome2 chromosome, String key, Map<String, MarketData> marketdatamap) throws Exception {
        NeuralNetConfigGene nnConfigGene = ((NeuralNetChromosome2) chromosome).getNnConfig();
        NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
        nnConfigs.set(key, nnConfigGene.getConfig());
        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(nnConfigs);
        PipelineResultData pipelineData = null;
        if (ml.equals(PipelineConstants.MLMULTI)) {
            conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMULTIMLCONFIG, value);
            pipelineData = new MLMulti(conf, catName, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
        } 
        if (ml.equals(PipelineConstants.MLSTOCH)) {
            conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLSTOCHMLCONFIG, value);
            pipelineData = new MLSTOCH(conf, catName, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
        } 
        if (ml.equals(PipelineConstants.MLCCI)) {
            conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLCCIMLCONFIG, value);
            pipelineData = new MLCCI(conf, catName, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
        } 
        if (ml.equals(PipelineConstants.MLATR)) {
            conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLATRMLCONFIG, value);
            pipelineData = new MLATR(conf, catName, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
        } 
        if (ml.equals(PipelineConstants.MLRSI)) {
            conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLRSIMLCONFIG, value);
            pipelineData = new MLRSI(conf, catName, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
        } 
        if (ml.equals(PipelineConstants.MLMACD)) {
            conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACDMLCONFIG, value);
            pipelineData = new MLMACD(conf, catName, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
        } 
        if (ml.equals(PipelineConstants.MLINDICATOR)) {
            conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORMLCONFIG, value);
            pipelineData = new MLIndicator(conf, catName, marketdatamap, catName, cat, categories, dataReaders, neuralnetcommand);
        }
        if (ml.equals(PipelineConstants.DATASET)) {
            conf.getConfigValueMap().put(ConfigConstants.DATASETMLCONFIG, value);
            pipelineData = new MLDataset(conf, catName, null, null, catName, cat, categories, dataReaders, neuralnetcommand);
        }
        if (ml.equals(PipelineConstants.PREDICTOR)) {
            conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNINGPREDICTORSMLCONFIG, value);
            //value = mapper.writeValueAsString(nnConfigs.getTensorflowConfig().getTensorflowLSTMConfig());
            //conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG, value);
            List<String> foundkeys = EvolutionService.getFoundKeys(conf, nnConfigs);
            pipelineData = null;
            for (String aKey : foundkeys) {
                switch (aKey) {
                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR:
                    pipelineData = new PredictorTensorflowLIR(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP:
                    pipelineData = new PredictorTensorflowMLP(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN:
                    pipelineData = new PredictorTensorflowRNN(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU:
                    pipelineData = new PredictorTensorflowGRU(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM:
                    pipelineData = new PredictorTensorflowLSTM(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP:
                    pipelineData = new PredictorPytorchMLP(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN:
                    pipelineData = new PredictorPytorchRNN(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU:
                    pipelineData = new PredictorPytorchGRU(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM:
                    pipelineData = new PredictorPytorchLSTM(conf, catName, null, catName, cat, categories, dataReaders, neuralnetcommand);
                    break;
                }
            }
            ((AbstractPredictor) pipelineData).calculate();
        }
        return pipelineData;
    }

}


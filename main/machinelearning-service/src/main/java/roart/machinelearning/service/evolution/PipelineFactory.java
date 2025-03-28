package roart.machinelearning.service.evolution;

import java.util.HashMap;
import java.util.List;

import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLDataset;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.common.config.ConfigConstants;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.model.PipelineResultData;
import roart.common.util.JsonUtil;
import roart.evolution.chromosome.impl.NeuralNetChromosome;
import roart.gene.NeuralNetConfigGene;
import roart.iclij.config.IclijConfig;
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
    public PipelineResultData myfactory(IclijConfig conf, String ml, PipelineData[] dataReaders, String catName, Integer cat, NeuralNetCommand neuralnetcommand, NeuralNetChromosome chromosome, String key, Inmemory inmemory) throws Exception {
        NeuralNetConfigGene nnConfigGene = ((NeuralNetChromosome) chromosome).getNnConfig();
        NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
        nnConfigs.set(key, nnConfigGene.getConfig());
        String value = JsonUtil.convert(nnConfigs);
        PipelineResultData pipelineData = null;
        List<String> stockDates = null;
        if (ml.equals(PipelineConstants.MLMULTI)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMULTIMLCONFIG, value);
            pipelineData = new MLMulti(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand, stockDates, inmemory );
        } 
        if (ml.equals(PipelineConstants.MLSTOCH)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLSTOCHMLCONFIG, value);
            pipelineData = new MLSTOCH(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand, stockDates, inmemory);
        } 
        if (ml.equals(PipelineConstants.MLCCI)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLCCIMLCONFIG, value);
            pipelineData = new MLCCI(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand, stockDates, inmemory);
        } 
        if (ml.equals(PipelineConstants.MLATR)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLATRMLCONFIG, value);
            pipelineData = new MLATR(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand, stockDates, inmemory);
        } 
        if (ml.equals(PipelineConstants.MLRSI)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLRSIMLCONFIG, value);
            pipelineData = new MLRSI(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand, stockDates, inmemory);
        } 
        if (ml.equals(PipelineConstants.MLMACD)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACDMLCONFIG, value);
            pipelineData = new MLMACD(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand, stockDates, inmemory);
        } 
        if (ml.equals(PipelineConstants.MLINDICATOR)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORMLCONFIG, value);
            pipelineData = new MLIndicator(conf, catName, catName, cat, dataReaders, neuralnetcommand, stockDates, inmemory);
        }
        if (ml.equals(PipelineConstants.DATASET)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATASETMLCONFIG, value);
            pipelineData = new MLDataset(conf, catName, null, catName, cat, neuralnetcommand, inmemory);
        }
        if (ml.equals(PipelineConstants.PREDICTOR)) {
            conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGPREDICTORSMLCONFIG, value);
            //value = mapper.writeValueAsString(nnConfigs.getTensorflowConfig().getTensorflowLSTMConfig());
            //conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG, value);
            List<String> foundkeys = MachineLearningEvolutionService.getFoundKeys(conf, nnConfigs);
            pipelineData = null;
            for (String aKey : foundkeys) {
                switch (aKey) {
                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR:
                    pipelineData = new PredictorTensorflowLIR(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP:
                    pipelineData = new PredictorTensorflowMLP(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN:
                    pipelineData = new PredictorTensorflowRNN(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU:
                    pipelineData = new PredictorTensorflowGRU(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM:
                    pipelineData = new PredictorTensorflowLSTM(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP:
                    pipelineData = new PredictorPytorchMLP(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN:
                    pipelineData = new PredictorPytorchRNN(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU:
                    pipelineData = new PredictorPytorchGRU(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;

                case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM:
                    pipelineData = new PredictorPytorchLSTM(conf, catName, catName, cat, dataReaders, neuralnetcommand, inmemory);
                    break;
                }
            }
            ((AbstractPredictor) pipelineData).calculate();
        }
        return pipelineData;
    }

}


package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLDataset;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome.MyFactory;
import roart.evolution.species.Individual;
import roart.gene.NeuralNetConfigGene;
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

public class NeuralNetChromosome2 extends AbstractChromosome {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private NeuralNetConfigGene nnConfigGene;
    
    public NeuralNetChromosome2(NeuralNetConfigGene nnConfigGene) {
        this.nnConfigGene = nnConfigGene;
    }

    public NeuralNetChromosome2(NeuralNetChromosome2 chromosome) {
        this(chromosome.nnConfigGene.copy());
    }

    public NeuralNetChromosome2() {
    }

    public NeuralNetConfigGene getNnConfig() {
        return nnConfigGene;
    }

    public void setNnConfig(NeuralNetConfigGene nnConfig) {
        this.nnConfigGene = nnConfig;
    }

    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void mutate() {
        nnConfigGene.mutate();
    }

    @Override
    public void getRandom()
            throws JsonParseException, JsonMappingException, IOException {
        nnConfigGene.randomize();
    }

    @Override
    public void transformToNode()
            throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode()
            throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public Individual crossover(AbstractChromosome evaluation) {
        NeuralNetConfigGene newNNConfig =  (NeuralNetConfigGene) nnConfigGene.crossover(((NeuralNetChromosome2) evaluation).nnConfigGene);
        NeuralNetChromosome2 eval = new NeuralNetChromosome2(newNNConfig);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        return new NeuralNetChromosome2(this);
    }
    
    @JsonIgnore
    @Override
    public boolean isEmpty() {
        return nnConfigGene == null || nnConfigGene.getConfig() == null || nnConfigGene.getConfig().empty();
    }
    
    @Override
    public String toString() {
        return "" + nnConfigGene;
    }

    @JsonIgnore
    @Override
    public double getFitness() throws JsonParseException, JsonMappingException, IOException {
        // TODO Auto-generated method stub
        return 0;
    }

}

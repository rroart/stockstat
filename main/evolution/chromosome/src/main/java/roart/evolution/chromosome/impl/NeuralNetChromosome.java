package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregator.impl.MLATR;
import roart.aggregator.impl.MLCCI;
import roart.aggregator.impl.MLIndicator;
import roart.aggregator.impl.MLMACD;
import roart.aggregator.impl.MLMulti;
import roart.aggregator.impl.MLRSI;
import roart.aggregator.impl.MLSTOCH;
import roart.category.AbstractCategory;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.gene.NeuralNetConfigGene;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.predictor.impl.PredictorLSTM;

public class NeuralNetChromosome extends AbstractChromosome {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;
    
    private String ml;

    private Pipeline[] dataReaders;

    private AbstractCategory[] categories;

    private String key;
    
    private NeuralNetConfigGene nnConfigGene;
    
    private String catName;
    
    private Integer cat;

    private NeuralNetCommand neuralnetcommand;
    
    public NeuralNetChromosome(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String key, NeuralNetConfigGene nnConfigGene, String catName, Integer cat, NeuralNetCommand neuralnetcommand) {
        this.conf = conf.copy();
        this.ml = ml;
        this.dataReaders = dataReaders;
        this.categories = categories;
        this.key = key;
        this.nnConfigGene = nnConfigGene;
        this.catName = catName;
        this.cat = cat;
        this.neuralnetcommand = neuralnetcommand;
    }

    public NeuralNetChromosome(NeuralNetChromosome chromosome) {
        this(chromosome.conf, chromosome.ml, chromosome.dataReaders, chromosome.categories, chromosome.key, chromosome.nnConfigGene.copy(), chromosome.catName, chromosome.cat, chromosome.neuralnetcommand);
    }

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
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
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
        PipelineResultData pipelineData = null;
        /*
        MyCallable callable = new MyCallable(conf, ml, dataReaders, categories);
        Future<Aggregator> future = MyExecutors.run(callable);
        aggregate = future.get();
        */
        try {
        pipelineData = new MyFactory().myfactory(conf, ml, dataReaders, categories, catName, cat, neuralnetcommand);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<String, Object> map = (Map<String, Object>) pipelineData.getLocalResultMap().get(PipelineConstants.PROBABILITY);
        if (map == null) {
            int jj = 0;
        }
        double fitness = 0;
        for (Entry<String, Object> entry : map.entrySet()) {
            Double value = (Double) entry.getValue();
            if (value != null) {
                fitness += value;
            }
        }
        if (!map.isEmpty()) {
            fitness = fitness / map.size();
        }
        return fitness;
    }

    class MyFactory {
        public PipelineResultData myfactory(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat, NeuralNetCommand neuralnetcommand) throws Exception {
            NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
            nnConfigs.set(key, nnConfigGene.getConfig());
            ObjectMapper mapper = new ObjectMapper();
            String value = mapper.writeValueAsString(nnConfigs);
            PipelineResultData pipelineData = null;
            if (ml.equals(PipelineConstants.MLMULTI)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMULTIMLCONFIG, value);
                pipelineData = new MLMulti(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLSTOCH)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLSTOCHMLCONFIG, value);
                pipelineData = new MLSTOCH(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLCCI)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLCCIMLCONFIG, value);
                pipelineData = new MLCCI(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLATR)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLATRMLCONFIG, value);
                pipelineData = new MLATR(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLRSI)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLRSIMLCONFIG, value);
                pipelineData = new MLRSI(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLMACD)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACDMLCONFIG, value);
                pipelineData = new MLMACD(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLINDICATOR)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORMLCONFIG, value);
                pipelineData = new MLIndicator(conf, catName, null, null, catName, cat, categories, dataReaders, neuralnetcommand);
            }
            if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
                value = mapper.writeValueAsString(nnConfigs.getTensorflowConfig().getTensorflowLSTMConfig());
                conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTMCONFIG, value);
                pipelineData = new PredictorLSTM(conf, catName, null, null, catName, cat, categories, dataReaders);
                ((AbstractPredictor) pipelineData).calculate();
            }
            return pipelineData;
        }

    }
    
    class MyCallable implements Callable {
        private MyMyConfig conf;
        private String ml;
        private Pipeline[] dataReaders;
        private AbstractCategory[] categories;
        private String catName;
        private Integer cat;
        private NeuralNetCommand neuralnetcommand;
        
        public MyCallable(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat, NeuralNetCommand neuralnetcommand) {
            this.conf = conf;
            this.ml = ml;
            this.dataReaders = dataReaders;
            this.categories = categories;
            this.catName = catName;
            this.cat = cat;
            this.neuralnetcommand = neuralnetcommand;
        }

        @Override
        public PipelineResultData call() throws Exception {
            return new MyFactory().myfactory(conf, ml, dataReaders, categories, catName, cat, neuralnetcommand);
        }
    }

    @Override
    public Individual crossover(AbstractChromosome evaluation) {
        NeuralNetConfigGene newNNConfig =  (NeuralNetConfigGene) nnConfigGene.crossover(((NeuralNetChromosome) evaluation).nnConfigGene);
        NeuralNetChromosome eval = new NeuralNetChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        return new NeuralNetChromosome(this);
    }
    
    @Override
    public boolean isEmpty() {
        return nnConfigGene == null || nnConfigGene.getConfig() == null || nnConfigGene.getConfig().empty();
    }
    
    @Override
    public String toString() {
        return key + " " + nnConfigGene;
    }

}

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
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
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
    
    private NeuralNetConfig nnConfig;
    
    private String catName;
    
    private Integer cat;
    
    public NeuralNetChromosome(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String key, NeuralNetConfig nnConfig, String catName, Integer cat) {
        this.conf = conf.copy();
        this.ml = ml;
        this.dataReaders = dataReaders;
        this.categories = categories;
        this.key = key;
        this.nnConfig = nnConfig;
        this.catName = catName;
        this.cat = cat;
    }

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
    }

    public NeuralNetConfig getNnConfig() {
        return nnConfig;
    }

    public void setNnConfig(NeuralNetConfig nnConfig) {
        this.nnConfig = nnConfig;
    }

    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void mutate() {
        nnConfig.mutate();
    }

    @Override
    public void getRandom()
            throws JsonParseException, JsonMappingException, IOException {
        nnConfig.randomize();
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
        pipelineData = new MyFactory().myfactory(conf, ml, dataReaders, categories, catName, cat);
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
            fitness += value;
        }
        if (!map.isEmpty()) {
            fitness = fitness / map.size();
        }
        return fitness;
    }

    class MyFactory {
        public PipelineResultData myfactory(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat) throws Exception {
            NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
            nnConfigs.set(key, nnConfig);
            ObjectMapper mapper = new ObjectMapper();
            String value = mapper.writeValueAsString(nnConfigs);
            PipelineResultData pipelineData = null;
            if (ml.equals(PipelineConstants.MLMULTI)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMULTIMLCONFIG, value);
                pipelineData = new MLMulti(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders);
            } 
            if (ml.equals(PipelineConstants.MLSTOCH)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLSTOCHMLCONFIG, value);
                pipelineData = new MLSTOCH(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders);
            } 
            if (ml.equals(PipelineConstants.MLCCI)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLCCIMLCONFIG, value);
                pipelineData = new MLCCI(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders);
            } 
            if (ml.equals(PipelineConstants.MLATR)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLATRMLCONFIG, value);
                pipelineData = new MLATR(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders);
            } 
            if (ml.equals(PipelineConstants.MLRSI)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLRSIMLCONFIG, value);
                pipelineData = new MLRSI(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders);
            } 
            if (ml.equals(PipelineConstants.MLMACD)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACDMLCONFIG, value);
                pipelineData = new MLMACD(conf, catName, null, null, catName, cat, categories, new HashMap<>(), dataReaders);
            } 
            if (ml.equals(PipelineConstants.MLINDICATOR)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORMLCONFIG, value);
                pipelineData = new MLIndicator(conf, catName, null, null, catName, cat, categories, dataReaders);
            }
            if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
                value = mapper.writeValueAsString(nnConfigs.getTensorflowLSTMConfig());
                conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG, value);
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
        
        public MyCallable(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat) {
            this.conf = conf;
            this.ml = ml;
            this.dataReaders = dataReaders;
            this.categories = categories;
            this.catName = catName;
            this.cat = cat;
        }

        @Override
        public PipelineResultData call() throws Exception {
            return new MyFactory().myfactory(conf, ml, dataReaders, categories, catName, cat);
        }
    }

    @Override
    public Individual crossover(AbstractChromosome evaluation) {
        NeuralNetConfig newNNConfig =  nnConfig.crossover(((NeuralNetChromosome) evaluation).nnConfig);
        NeuralNetChromosome eval = new NeuralNetChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat);
        return new Individual(eval);
    }

    @Override
    public AbstractChromosome copy() {
        NeuralNetConfig newNNConfig = null;
        if (nnConfig != null) {
            newNNConfig = (NeuralNetConfig) (nnConfig.copy());
        }
        return new NeuralNetChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat);
    }
    
    @Override
    public boolean isEmpty() {
        return nnConfig == null || nnConfig.empty();
    }
    
    @Override
    public String toString() {
        return key + " " + nnConfig;
    }

}

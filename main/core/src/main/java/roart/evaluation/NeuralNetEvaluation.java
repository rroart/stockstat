package roart.evaluation;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jfree.util.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.pipeline.common.aggregate.Aggregator;
import roart.executor.MyExecutors;
import roart.aggregate.MLIndicator;
import roart.aggregate.MLMACD;
import roart.calculate.CalcNodeUtils;
import roart.category.Category;
import roart.common.config.ConfigConstants;
import roart.common.config.MyConfig;
import roart.common.config.MyMyConfig;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.ml.NNConfig;
import roart.common.ml.NNConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.evolution.model.Evaluation;
import roart.evolution.species.Individual;
import roart.pipeline.Pipeline;
import roart.service.ControlService;

public class NeuralNetEvaluation extends Evaluation {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;
    
    private String ml;

    private Pipeline[] dataReaders;

    private Category[] categories;

    private String key;
    
    private NNConfig nnConfig;
    
    public NeuralNetEvaluation(MyMyConfig conf, String ml, Pipeline[] dataReaders, Category[] categories, String key, NNConfig nnConfig) {
        this.conf = conf.copy();
        this.ml = ml;
        this.dataReaders = dataReaders;
        this.categories = categories;
        this.key = key;
        this.nnConfig = nnConfig;
    }

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
    }

    public NNConfig getNnConfig() {
        return nnConfig;
    }

    public void setNnConfig(NNConfig nnConfig) {
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
        Aggregator aggregate = null;
        /*
        MyCallable callable = new MyCallable(conf, ml, dataReaders, categories);
        Future<Aggregator> future = MyExecutors.run(callable);
        aggregate = future.get();
        */
        try {
        aggregate = new MyFactory().myfactory(conf, ml, dataReaders, categories);
        } catch (Exception e) {
            log.info(Constants.EXCEPTION, e);
        }
        Map<String, Object> map = (Map<String, Object>) aggregate.getLocalResultMap().get(PipelineConstants.PROBABILITY);
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
        public Aggregator myfactory(MyMyConfig conf, String ml, Pipeline[] dataReaders, Category[] categories) throws Exception {
            NNConfigs nnConfigs = new NNConfigs();
            nnConfigs.set(key, nnConfig);
            ObjectMapper mapper = new ObjectMapper();
            String value = mapper.writeValueAsString(nnConfigs);
            Aggregator aggregate = null;
            if (ml.equals(PipelineConstants.MLMACD)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACDMLCONFIG, value);
                aggregate = new MLMACD(conf, Constants.PRICE, null, null, CategoryConstants.PRICE, 0, categories);
            } 
            if (ml.equals(PipelineConstants.MLINDICATOR)) {
                conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORMLCONFIG, value);
                aggregate = new MLIndicator(conf, Constants.PRICE, null, null, CategoryConstants.PRICE, 0, categories, dataReaders);
            }
            return aggregate;
        }

    }
    
    class MyCallable implements Callable {
        private MyMyConfig conf;
        private String ml;
        private Pipeline[] dataReaders;
        private Category[] categories;

        public MyCallable(MyMyConfig conf, String ml, Pipeline[] dataReaders, Category[] categories) {
            this.conf = conf;
            this.ml = ml;
            this.dataReaders = dataReaders;
            this.categories = categories;
        }

        @Override
        public Aggregator call() throws Exception {
            return new MyFactory().myfactory(conf, ml, dataReaders, categories);
        }
    }

    @Override
    public Individual crossover(Evaluation evaluation) {
        NNConfig newNNConfig =  nnConfig.crossover(((NeuralNetEvaluation) evaluation).nnConfig);
        NeuralNetEvaluation eval = new NeuralNetEvaluation(conf, ml, dataReaders, categories, key, newNNConfig);
        return new Individual(eval);
    }

    @Override
    public Evaluation copy() {
        NNConfig newNNConfig = null;
        if (nnConfig != null) {
            newNNConfig = (NNConfig) (nnConfig.copy());
        }
        return new NeuralNetEvaluation(conf, ml, dataReaders, categories, key, newNNConfig);
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

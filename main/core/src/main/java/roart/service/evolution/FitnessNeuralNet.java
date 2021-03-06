package roart.service.evolution;

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
import roart.category.AbstractCategory;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.model.PipelineResultData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome2;
import roart.evolution.fitness.Fitness;
import roart.gene.NeuralNetConfigGene;
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

public class FitnessNeuralNet extends Fitness {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MyMyConfig conf;
    
    private String ml;

    private Pipeline[] dataReaders;

    private AbstractCategory[] categories;

    private String key;
    
    private String catName;
    
    private Integer cat;

    private NeuralNetCommand neuralnetcommand;
    
    protected String titletext;
    
    public FitnessNeuralNet(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String key, String catName, Integer cat, NeuralNetCommand neuralnetcommand) {
        this.conf = conf.copy();
        this.ml = ml;
        this.dataReaders = dataReaders;
        this.categories = categories;
        this.key = key;
        this.catName = catName;
        this.cat = cat;
        this.neuralnetcommand = neuralnetcommand;
    }

    @Override
    public double fitness(AbstractChromosome chromosome) {
        PipelineResultData pipelineData = null;
        /*
        MyCallable callable = new MyCallable(conf, ml, dataReaders, categories);
        Future<Aggregator> future = MyExecutors.run(callable);
        aggregate = future.get();
        */
        try {
        pipelineData = new PipelineFactory().myfactory(conf, ml, dataReaders, categories, catName, cat, neuralnetcommand, ((NeuralNetChromosome2) chromosome), key);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<String, Object> accuracyMap = (Map<String, Object>) pipelineData.getLocalResultMap().get(PipelineConstants.ACCURACY);
        if (accuracyMap == null) {
            int jj = 0;
        }
        double fitness = 0;
        for (Entry<String, Object> entry : accuracyMap.entrySet()) {
            Double value = (Double) entry.getValue();
            if (value != null) {
                fitness += value;
            }
        }
        if (!accuracyMap.isEmpty()) {
            fitness = fitness / accuracyMap.size();
            double max = accuracyMap.values().stream().filter(Objects::nonNull).mapToDouble(e -> (Double) e).max().orElse(-1);
            List<Object> keys = Arrays.asList(accuracyMap.entrySet().stream().filter(entry -> entry.getValue() != null && max == (double) entry.getValue())
.map(Map.Entry::getKey).toArray());
            log.info("Fit #{} {} {} {}", new Object[] { this.hashCode(), fitness, max, keys });
        }
        log.info("Fit #{} {} {} {}", new Object[] { this.hashCode(), fitness, accuracyMap.values().stream().filter(Objects::nonNull).mapToDouble(e -> (Double) e).summaryStatistics(), this.toString()});
        return fitness;
    }

    class MyCallable implements Callable {
        private MyMyConfig conf;
        private String ml;
        private Pipeline[] dataReaders;
        private AbstractCategory[] categories;
        private String catName;
        private Integer cat;
        private NeuralNetCommand neuralnetcommand;
        private NeuralNetChromosome2 chromosome;
        
        public MyCallable(MyMyConfig conf, String ml, Pipeline[] dataReaders, AbstractCategory[] categories, String catName, Integer cat, NeuralNetCommand neuralnetcommand, NeuralNetChromosome2 chromosome) {
            this.conf = conf;
            this.ml = ml;
            this.dataReaders = dataReaders;
            this.categories = categories;
            this.catName = catName;
            this.cat = cat;
            this.neuralnetcommand = neuralnetcommand;
            this.chromosome = chromosome;
        }

        @Override
        public PipelineResultData call() throws Exception {
            return new PipelineFactory().myfactory(conf, ml, dataReaders, categories, catName, cat, neuralnetcommand, chromosome, key);
        }
    }

}

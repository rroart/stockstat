package roart.machinelearning.service.evolution;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.model.PipelineResultData;
import roart.common.pipeline.util.PipelineUtils;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.chromosome.impl.NeuralNetChromosome;
import roart.evolution.fitness.Fitness;
import roart.iclij.config.IclijConfig;

public class FitnessNeuralNet extends Fitness {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig conf;
    
    private String ml;

    private PipelineData[] dataReaders;

    private String key;
    
    private String catName;
    
    private Integer cat;

    private NeuralNetCommand neuralnetcommand;
    
    protected String titletext;

    private Inmemory inmemory;
    
    public FitnessNeuralNet(IclijConfig conf, String ml, PipelineData[] dataReaders, String key, String catName, Integer cat, NeuralNetCommand neuralnetcommand, Inmemory inmemory) {
        this.conf = conf.copy();
        this.ml = ml;
        this.dataReaders = dataReaders;
        this.key = key;
        this.catName = catName;
        this.cat = cat;
        this.neuralnetcommand = neuralnetcommand;
        this.inmemory = inmemory;
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
        pipelineData = new PipelineFactory().myfactory(conf, ml, dataReaders, catName, cat, neuralnetcommand, ((NeuralNetChromosome) chromosome), key, inmemory);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            // TODO null MyMyConfig.getAggregatorsIndicatorIntervaldays
            return 0;
        }
        // nothing is written to inmemory, so will not need to clean
        Map<String, Object> accuracyMap = PipelineUtils.getAccuracyMap(pipelineData.putData());
        if (accuracyMap == null) {
            return 0;
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
        private IclijConfig conf;
        private String ml;
        private PipelineData[] dataReaders;
        private String catName;
        private Integer cat;
        private NeuralNetCommand neuralnetcommand;
        private NeuralNetChromosome chromosome;
        
        public MyCallable(IclijConfig conf, String ml, PipelineData[] dataReaders, String catName, Integer cat, NeuralNetCommand neuralnetcommand, NeuralNetChromosome chromosome) {
            this.conf = conf;
            this.ml = ml;
            this.dataReaders = dataReaders;
            this.catName = catName;
            this.cat = cat;
            this.neuralnetcommand = neuralnetcommand;
            this.chromosome = chromosome;
        }

        @Override
        public PipelineResultData call() throws Exception {
            Inmemory inmemory = null;
            return new PipelineFactory().myfactory(conf, ml, dataReaders, catName, cat, neuralnetcommand, chromosome, key, inmemory);
        }
    }

}

package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.Objects;

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
import roart.iclij.config.IclijConfig;
import roart.common.constants.CategoryConstants;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.model.PipelineResultData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
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

public class NeuralNetChromosome extends AbstractChromosome {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private IclijConfig conf;
    
    private String ml;

    private PipelineData[] dataReaders;

    private AbstractCategory[] categories;

    private String key;
    
    private NeuralNetConfigGene nnConfigGene;
    
    private String catName;
    
    private Integer cat;

    private NeuralNetCommand neuralnetcommand;
    
    private Map<String, MarketData> marketdatamap;
    
    public NeuralNetChromosome(IclijConfig conf, String ml, PipelineData[] dataReaders, AbstractCategory[] categories, String key, NeuralNetConfigGene nnConfigGene, String catName, Integer cat, NeuralNetCommand neuralnetcommand, Map<String, MarketData> marketdatamap) {
        this.conf = conf.copy();
        this.ml = ml;
        this.dataReaders = dataReaders;
        this.categories = categories;
        this.key = key;
        this.nnConfigGene = nnConfigGene;
        this.catName = catName;
        this.cat = cat;
        this.neuralnetcommand = neuralnetcommand;
        this.marketdatamap = marketdatamap;
    }

    public NeuralNetChromosome(NeuralNetChromosome chromosome) {
        this(chromosome.conf, chromosome.ml, chromosome.dataReaders, chromosome.categories, chromosome.key, chromosome.nnConfigGene.copy(), chromosome.catName, chromosome.cat, chromosome.neuralnetcommand, chromosome.marketdatamap);
    }

    public IclijConfig getConf() {
        return conf;
    }

    public void setConf(IclijConfig conf) {
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
        Map<String, Object> accuracyMap = (Map<String, Object>) pipelineData.putData().get(PipelineConstants.ACCURACY);
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

    class MyFactory {
        public PipelineResultData myfactory(IclijConfig conf, String ml, PipelineData[] dataReaders, AbstractCategory[] categories, String catName, Integer cat, NeuralNetCommand neuralnetcommand) throws Exception {
            NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
            nnConfigs.set(key, nnConfigGene.getConfig());
            ObjectMapper mapper = new ObjectMapper();
            String value = mapper.writeValueAsString(nnConfigs);
            PipelineResultData pipelineData = null;
            if (ml.equals(PipelineConstants.MLMULTI)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMULTIMLCONFIG, value);
                pipelineData = new MLMulti(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLSTOCH)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLSTOCHMLCONFIG, value);
                pipelineData = new MLSTOCH(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLCCI)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLCCIMLCONFIG, value);
                pipelineData = new MLCCI(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLATR)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLATRMLCONFIG, value);
                pipelineData = new MLATR(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLRSI)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLRSIMLCONFIG, value);
                pipelineData = new MLRSI(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLMACD)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACDMLCONFIG, value);
                pipelineData = new MLMACD(conf, catName, catName, cat, new HashMap<>(), dataReaders, neuralnetcommand);
            } 
            if (ml.equals(PipelineConstants.MLINDICATOR)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORMLCONFIG, value);
                pipelineData = new MLIndicator(conf, catName, catName, cat, categories, dataReaders, neuralnetcommand);
            }
            if (ml.equals(PipelineConstants.DATASET)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.DATASETMLCONFIG, value);
                pipelineData = new MLDataset(conf, catName, null, catName, cat, neuralnetcommand);
            }
            if (ml.equals(PipelineConstants.PREDICTOR)) {
                conf.getConfigData().getConfigValueMap().put(ConfigConstants.MACHINELEARNINGPREDICTORSMLCONFIG, value);
                //value = mapper.writeValueAsString(nnConfigs.getTensorflowConfig().getTensorflowLSTMConfig());
                //conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG, value);
                List<String> foundkeys = getFoundKeys(conf, nnConfigs);
                pipelineData = null;
                for (String key : foundkeys) {
                    switch (key) {
                    case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR:
                        pipelineData = new PredictorTensorflowLIR(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP:
                        pipelineData = new PredictorTensorflowMLP(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN:
                        pipelineData = new PredictorTensorflowRNN(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU:
                        pipelineData = new PredictorTensorflowGRU(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM:
                        pipelineData = new PredictorTensorflowLSTM(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP:
                        pipelineData = new PredictorPytorchMLP(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN:
                        pipelineData = new PredictorPytorchRNN(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU:
                        pipelineData = new PredictorPytorchGRU(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;

                    case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM:
                        pipelineData = new PredictorPytorchLSTM(conf, catName, catName, cat, dataReaders, neuralnetcommand);
                        break;
                    }
                }
                ((AbstractPredictor) pipelineData).calculate();
            }
            return pipelineData;
        }

    }
    
    class MyCallable implements Callable {
        private IclijConfig conf;
        private String ml;
        private PipelineData[] dataReaders;
        private AbstractCategory[] categories;
        private String catName;
        private Integer cat;
        private NeuralNetCommand neuralnetcommand;
        
        public MyCallable(IclijConfig conf, String ml, PipelineData[] dataReaders, AbstractCategory[] categories, String catName, Integer cat, NeuralNetCommand neuralnetcommand) {
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
        NeuralNetChromosome eval = new NeuralNetChromosome(conf, ml, dataReaders, categories, key, newNNConfig, catName, cat, neuralnetcommand, marketdatamap);
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

    public List<String> getFoundKeys(IclijConfig conf, NeuralNetConfigs nnConfigs) {
        List<String> keys = getMLkeys();
        
        List<String> foundkeys = new ArrayList<>();
        for (String key : keys) {
            //System.out.println(conf.getValueOrDefault(key));
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
            log.error("Foundkeys size {}", foundkeys.size());
        }
        return foundkeys;
    }

    private List<String> getMLkeys() {
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
        //keys.add(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM);
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

}

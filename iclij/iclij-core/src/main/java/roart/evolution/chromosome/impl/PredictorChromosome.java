package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.component.model.ComponentData;
import roart.component.model.ComponentInput;
import roart.config.Market;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.PredictorService;
import roart.service.model.ProfitData;

public class PredictorChromosome extends ConfigMapChromosome {

    private TensorflowLSTMConfig config;
    
    public PredictorChromosome(List<String> confList, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String component) {
        super(confList, param, profitdata, market, positions, component);
    }

    public TensorflowLSTMConfig getConfig() {
        return config;
    }

    public void setConfig(TensorflowLSTMConfig config) {
        this.config = config;
    }

    @Override
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
        config.full = true;
        Map<String, Object> map = new HashMap<>();
        String string = JsonUtil.convert(config);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG, string);
        setMap(map);
        return super.getFitness();
    }

    @Override
    public void getRandom() throws JsonParseException, JsonMappingException, IOException {
        config.randomize();
    }
    
    @Override
    public void mutate() {
        config.mutate();
    }
    
    @Override
    public Individual crossover(AbstractChromosome other) {
        PredictorChromosome chromosome = new PredictorChromosome(confList, param, profitdata, market, positions, componentName);
        TensorflowLSTMConfig otherConfig = ((PredictorChromosome) other).config;
        NeuralNetConfig offspring = config.crossover(otherConfig);
        chromosome.config = (TensorflowLSTMConfig) offspring;
        return new Individual(chromosome);
    }
    
    @Override
    public double getEvaluations(int j) throws JsonParseException, JsonMappingException, IOException {
        return 0;
    }

    @Override
    public void transformToNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public void normalize() {
    }

    @Override
    public void transformFromNode() throws JsonParseException, JsonMappingException, IOException {
    }

    @Override
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        PredictorChromosome chromosome = new PredictorChromosome(confList, newparam, profitdata, market, positions, componentName);
        //chromosome.config = new TensorflowLSTMConfig(config.getEpochs(), config.getWindowsize(), config.getHorizon());
        chromosome.config = (TensorflowLSTMConfig) config.copy();
        return chromosome;
    }
    
    public double getFitnessNot()
            throws JsonParseException, JsonMappingException, IOException {
        //list.add(ConfigConstants.INDICATORSMACDDAYSAFTERZERO);
        List<MemoryItem> memoryItems = null;
        /*
        MyCallable callable = new MyCallable(conf, ml, dataReaders, categories);
        Future<Aggregator> future = MyExecutors.run(callable);
        aggregate = future.get();
        */
        try {
        memoryItems = new MyFactory().myfactory(null, PipelineConstants.PREDICTORSLSTM);
        } catch (Exception e) {
            //log.error(Constants.EXCEPTION, e);
        }
        double fitness = 0;
        for (MemoryItem memoryItem : memoryItems) {
            Double value = memoryItem.getConfidence();
            fitness += value;
        }
        if (!memoryItems.isEmpty()) {
            fitness = fitness / memoryItems.size();
        }
        return fitness;
    }

    class MyFactory {
        public List<MemoryItem> myfactory(MyMyConfig conf, String ml) throws Exception {
            /*
            ControlService srv = new ControlService();
            srv.getConfig();            
            srv.conf.getConfigValueMap().putAll(getMap());
            */
            if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
                List<MemoryItem> memories = new PredictorService().doPredict(new ComponentInput(conf.getMarket(), LocalDate.now(), Integer.valueOf(0), false, false), getMap());
                return memories;
            } 
            return null;
        }

    }
    
}

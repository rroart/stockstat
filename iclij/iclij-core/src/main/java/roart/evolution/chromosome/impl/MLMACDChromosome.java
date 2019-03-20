package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.component.model.ComponentInput;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.MLService;

public class MLMACDChromosome extends ConfigMapChromosome {

    public MLMACDChromosome(MyMyConfig conf, List<String> confList) {
        super(conf, confList);
        // TODO Auto-generated constructor stub
    }

    @Override
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
        List<MemoryItem> memoryItems = null;
        /*
        MyCallable callable = new MyCallable(conf, ml, dataReaders, categories);
        Future<Aggregator> future = MyExecutors.run(callable);
        aggregate = future.get();
        */
        try {
        memoryItems = new MyFactory().myfactory(getConf(), PipelineConstants.MLMACD);
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
            if (ml.equals(PipelineConstants.MLMACD)) {
                List<MemoryItem> memories = new MLService().doMLMACD(new ComponentInput(conf.getMarket(), null, null, false, false), getMap());
                return memories;
            } 
            if (ml.equals(PipelineConstants.MLINDICATOR)) {
                List<MemoryItem> memories = new MLService().doMLIndicator(new ComponentInput(conf.getMarket(), null, null, false, false), getMap());
                return memories;
            }
            return null;
        }

    }
    
}

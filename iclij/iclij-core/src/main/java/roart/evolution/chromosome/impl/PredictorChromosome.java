package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.PredictorService;

public class PredictorChromosome extends ConfigMapChromosome {

    public PredictorChromosome(MyMyConfig conf, List<String> confList) {
        super(conf, confList);
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
        memoryItems = new MyFactory().myfactory(getConf(), PipelineConstants.PREDICTORSLSTM);
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
            ControlService srv = new ControlService();
            srv.getConfig();            
            srv.conf.getConfigValueMap().putAll(getMap());
            if (ml.equals(PipelineConstants.PREDICTORSLSTM)) {
                List<MemoryItem> memories = new PredictorService().doPredict(srv, conf.getMarket(), 0, null, false, false);
                return memories;
            } 
            return null;
        }

    }
    
}

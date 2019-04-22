package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.action.FindProfitAction;
import roart.action.WebData;
import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.model.ComponentData;
import roart.component.model.ComponentInput;
import roart.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public class MLMACDChromosome extends ConfigMapChromosome {
    
    public MLMACDChromosome(ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component) {
        super(confList, param, profitdata, market, positions, component);
    }

    @Override
    public double getFitness()
            throws JsonParseException, JsonMappingException, IOException {
        /*
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.INDICATORSMACDDAYSBEFOREZERO);
        list.add(ConfigConstants.INDICATORSMACDDAYSAFTERZERO);
        */
        return super.getFitness();
        /*
        MyCallable callable = new MyCallable(conf, ml, dataReaders, categories);
        Future<Aggregator> future = MyExecutors.run(callable);
        aggregate = future.get();
        */
    }
     
    class MyFactoryNot {
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

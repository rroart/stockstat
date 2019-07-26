package roart.evolution.chromosome.impl;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
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
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.species.Individual;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.ControlService;
import roart.service.MLService;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public class MLMultiChromosome extends MLAggregatorChromosome {

    public MLMultiChromosome(ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy) {
        super(param, profitdata, confList, market, positions, component, buy);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLATRChromosome(newparam, profitdata, confList, market, positions, componentName, buy);
    }

    @Override
    protected List<String> getValidateList() {
        List<String> list = new ArrayList<>();
        list.add(ConfigConstants.AGGREGATORSMLMULTIMACD);
        list.add(ConfigConstants.AGGREGATORSMLMULTIRSI);
        list.add(ConfigConstants.AGGREGATORSMLMULTIATR);
        list.add(ConfigConstants.AGGREGATORSMLMULTICCI);
        list.add(ConfigConstants.AGGREGATORSMLMULTISTOCH);
        list.add(ConfigConstants.AGGREGATORSMLMULTISTOCHRSI);
        list.retainAll(confList);
        return list;
    }
    
}

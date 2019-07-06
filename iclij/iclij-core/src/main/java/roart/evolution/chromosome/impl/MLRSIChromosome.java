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

public class MLRSIChromosome extends ConfigMapChromosome {

    public MLRSIChromosome(ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy) {
        super(confList, param, profitdata, market, positions, component, buy);
    }

    @Override
    public boolean validate() {
        for (String key : getList()) {
            Object object = getMap().get(key);
            if (object != null && object instanceof Boolean) {
                if ((boolean) object) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public void fixValidation() { 
        Random rand = new Random();
        int index = rand.nextInt(getList().size());
        getMap().put(getList().get(index), true);
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

    private List<String> getList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLRSIBUYRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSIBUYSRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSISELLRSILIMIT);
        confList.add(ConfigConstants.AGGREGATORSMLRSISELLSRSILIMIT);
        return confList;
    }
    
    @Override
    public AbstractChromosome copy() {
        ComponentData newparam = new ComponentData(param);
        MLRSIChromosome chromosome = new MLRSIChromosome(newparam, profitdata, confList, market, positions, componentName, buy);
        return chromosome;
    }
    
    @Override
    public Individual crossover(AbstractChromosome other) {
        ComponentData newparam = new ComponentData(param);
        MLIndicatorChromosome chromosome = new MLIndicatorChromosome(confList, newparam, profitdata, market, positions, componentName, buy);
        Random rand = new Random();
        for (int conf = 0; conf < confList.size(); conf++) {
            String confName = confList.get(conf);
            if (rand.nextBoolean()) {
                chromosome.getMap().put(confName, this.getMap().get(confName));
            } else {
                chromosome.getMap().put(confName, ((ConfigMapChromosome) other).getMap().get(confName));
            }
        }
        if (!chromosome.validate()) {
            chromosome.fixValidation();
        }
        return new Individual(chromosome);
    }

}

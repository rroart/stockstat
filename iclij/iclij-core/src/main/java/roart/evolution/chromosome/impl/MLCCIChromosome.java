package roart.evolution.chromosome.impl;

import java.util.List;

import roart.component.model.ComponentData;
import roart.config.Market;
import roart.service.model.ProfitData;

public class MLCCIChromosome extends MLAggregatorChromosome {

    public MLCCIChromosome(ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy) {
        super(param, profitdata, confList, market, positions, component, buy);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLCCIChromosome(newparam, profitdata, confList, market, positions, componentName, buy);
    }
    
}
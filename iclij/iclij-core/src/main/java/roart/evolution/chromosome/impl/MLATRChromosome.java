package roart.evolution.chromosome.impl;

import java.util.List;

import roart.component.model.ComponentData;
import roart.config.Market;
import roart.service.model.ProfitData;

public class MLATRChromosome extends MLAggregatorChromosome {

    public MLATRChromosome(ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy, String subcomponent) {
        super(param, profitdata, confList, market, positions, component, buy, subcomponent);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLATRChromosome(newparam, profitdata, confList, market, positions, componentName, buy, subcomponent);
    }
    
}

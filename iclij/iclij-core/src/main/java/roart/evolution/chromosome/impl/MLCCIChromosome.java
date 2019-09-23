package roart.evolution.chromosome.impl;

import java.util.List;

import roart.action.MarketAction;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.service.model.ProfitData;

public class MLCCIChromosome extends MLAggregatorChromosome {

    public MLCCIChromosome(MarketAction action, ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy, String subcomponent) {
        super(action, param, profitdata, confList, market, positions, component, buy, subcomponent);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLCCIChromosome(action, newparam, profitdata, confList, market, positions, componentName, buy, subcomponent);
    }
    
}

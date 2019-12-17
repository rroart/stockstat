package roart.evolution.chromosome.impl;

import java.util.List;

import roart.action.MarketAction;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class MLATRChromosome extends MLAggregatorChromosome {

    public MLATRChromosome(MarketAction action, ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy, String subcomponent, Parameters parameters) {
        super(action, param, profitdata, confList, market, positions, component, buy, subcomponent, parameters);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLATRChromosome(action, newparam, profitdata, confList, market, positions, componentName, buy, subcomponent, parameters);
    }
    
}

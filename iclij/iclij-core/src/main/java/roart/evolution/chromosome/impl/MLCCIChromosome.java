package roart.evolution.chromosome.impl;

import java.util.List;

import roart.action.MarketAction;
import roart.component.model.ComponentData;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class MLCCIChromosome extends MLAggregatorChromosome {

    public MLCCIChromosome(MarketAction action, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String component, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene) {
        super(action, param, profitdata, market, positions, component, buy, subcomponent, parameters, gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLCCIChromosome(action, newparam, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene);
    }
    
}

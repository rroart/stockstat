package roart.evolution.chromosome.impl;

import java.util.List;

import roart.action.MarketAction;
import roart.component.model.ComponentData;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class MLSTOCHChromosome extends MLAggregatorChromosome {

    public MLSTOCHChromosome(MarketAction action, ComponentData param, ProfitData profitdata, Market market, List<Integer> positions, String component, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        super(action, param, profitdata, market, positions, component, buy, subcomponent, parameters, gene, mlTests);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLSTOCHChromosome(action, newparam, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene, mlTests);
    }
    
}

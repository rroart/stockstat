package roart.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.component.Memories;
import roart.component.model.ComponentData;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.Market;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.Parameters;
import roart.service.model.ProfitData;

public class MLMultiChromosome extends MLAggregatorChromosome {

    public MLMultiChromosome(MarketAction action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String component, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        super(action, param, profitdata, market, positions, component, buy, subcomponent, parameters, gene, mlTests);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLMultiChromosome(action, newparam, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene, mlTests);
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
        list.retainAll(getConfList());
        return list;
    }
    
}

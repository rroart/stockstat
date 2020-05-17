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

public class MLMACDChromosome extends MLAggregatorChromosome {

    public MLMACDChromosome(MarketAction action, ComponentData param, ProfitData profitdata, Market market, Memories positions, String component, Boolean buy, String subcomponent, Parameters parameters, ConfigMapGene gene, List<MLMetricsItem> mlTests) {
        super(action, param, profitdata, market, positions, component, buy, subcomponent, parameters, gene, mlTests);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLMACDChromosome(action, newparam, profitdata, market, positions, componentName, buy, subcomponent, parameters, gene, mlTests);
    }
    
    @Override
    protected List<String> getValidateList() {
        List<String> confList = new ArrayList<>();
        confList.add(ConfigConstants.AGGREGATORSMLMACDHISTOGRAMML);
        confList.add(ConfigConstants.AGGREGATORSMLMACDMACDML);
        confList.add(ConfigConstants.AGGREGATORSMLMACDSIGNALML);
        return confList;
    }
    
}

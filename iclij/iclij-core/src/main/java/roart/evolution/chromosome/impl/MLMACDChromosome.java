package roart.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.List;

import roart.action.MarketAction;
import roart.common.config.ConfigConstants;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.service.model.ProfitData;

public class MLMACDChromosome extends MLAggregatorChromosome {

    public MLMACDChromosome(MarketAction action, ComponentData param, ProfitData profitdata, List<String> confList, Market market, List<Integer> positions, String component, Boolean buy, String subcomponent) {
        super(action, param, profitdata, confList, market, positions, component, buy, subcomponent);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome(ComponentData newparam) {
        return new MLMACDChromosome(action, newparam, profitdata, confList, market, positions, componentName, buy, subcomponent);
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

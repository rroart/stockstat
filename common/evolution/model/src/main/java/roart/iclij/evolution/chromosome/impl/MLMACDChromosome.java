package roart.iclij.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.List;

import roart.common.config.ConfigConstants;
import roart.gene.impl.ConfigMapGene;

public class MLMACDChromosome extends MLAggregatorChromosome {

    public MLMACDChromosome() {
        super();
    }

    public MLMACDChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLMACDChromosome(gene);
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

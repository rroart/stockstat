package roart.iclij.evolution.chromosome.impl;

import java.util.ArrayList;
import java.util.List;

import roart.common.config.ConfigConstants;
import roart.gene.impl.ConfigMapGene;

public class MLMultiChromosome extends MLAggregatorChromosome {

    public MLMultiChromosome(ConfigMapGene gene) {
        super(gene);
    }

    @Override
    protected MLAggregatorChromosome getNewChromosome() {
        return new MLMultiChromosome(gene);
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

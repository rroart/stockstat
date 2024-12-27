package roart.evolution.marketfilter.genetics.gene.impl;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.evolution.marketfilter.common.gene.impl.MarketFilterMutateCommon;
import roart.iclij.config.MarketFilter;

public class MarketFilterMutate {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    Random random = new Random();
    
    public void mutate(MarketFilterGene gene) {
        MarketFilter myconfig = gene.getMarketfilter();
        new MarketFilterMutateCommon().mutateCommon(myconfig);
    }

}

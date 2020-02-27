package roart.evolution.marketfilter.genetics.gene.impl;

import java.util.Random;

import roart.iclij.config.MarketFilter;

public class MarketFilterCrossover {

    Random random = new Random();
    
    public void crossover(MarketFilterGene marketFilterGene, MarketFilterGene other) {
        MarketFilter myconfig = (MarketFilter) marketFilterGene.getMarketfilter();
        MarketFilter otherconfig = (MarketFilter) other.getMarketfilter();
        if (random.nextBoolean()) {
            myconfig.setDeccategory(otherconfig.getDeccategory());
        }
        if (random.nextBoolean()) {
            myconfig.setDecdays(otherconfig.getDecdays());
        }
        if (random.nextBoolean()) {
            myconfig.setDecthreshold(otherconfig.getDecthreshold());
        }
        if (random.nextBoolean()) {
            myconfig.setInccategory(otherconfig.getInccategory());
        }
        if (random.nextBoolean()) {
            myconfig.setIncdays(otherconfig.getIncdays());
        }
        if (random.nextBoolean()) {
            myconfig.setIncthreshold(otherconfig.getIncthreshold());
        }
        if (random.nextBoolean()) {
            myconfig.setConfidence(otherconfig.getConfidence());
        }
    }

}

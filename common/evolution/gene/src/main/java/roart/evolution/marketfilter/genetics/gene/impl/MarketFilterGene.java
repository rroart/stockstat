package roart.evolution.marketfilter.genetics.gene.impl;

import roart.iclij.config.MarketFilter;

import java.util.List;

import roart.evolution.marketfilter.common.gene.impl.MarketFilterMutateCommon;
import roart.gene.AbstractGene;

public class MarketFilterGene extends AbstractGene {
    
    private MarketFilter marketfilter;

    private List<String> categories;

    public MarketFilterGene(MarketFilter marketfilter, List<String> categories) {
        this.marketfilter = marketfilter;
        this.categories = categories;
    }

    public MarketFilterGene() {
        // Json
    }

    public MarketFilter getMarketfilter() {
        return marketfilter;
    }

    public void setMarketfilter(MarketFilter marketfilter) {
        this.marketfilter = marketfilter;
    }

    @Override
    public void randomize() {
        MarketFilter myconfig = marketfilter;
        MarketFilterMutateCommon gene =  new MarketFilterMutateCommon();
        myconfig.setDeccategory(gene.generateCategory(myconfig));
        if (random.nextBoolean()) {
            myconfig.setDecdays(gene.generateDays());
        }
        myconfig.setDecthreshold(gene.generateThreshold());
        myconfig.setInccategory(gene.generateCategory(myconfig));
        if (random.nextBoolean()) {
            myconfig.setIncdays(gene.generateDays());
        }
        myconfig.setIncthreshold(gene.generateThreshold());
        myconfig.setConfidence(gene.generateConfidence());
    }

    @Override
    public void mutate() {
        new MarketFilterMutate().mutate(this);
    }

    public AbstractGene crossover(MarketFilterGene other) {
        new MarketFilterCrossover().crossover(this, other);
        return this;
    }
    
    @Override
    public AbstractGene crossover(AbstractGene other) {
        MarketFilterGene offspring = new MarketFilterGene(marketfilter, categories);
        offspring.crossover((MarketFilterGene) other);
        return offspring;
    }

    public MarketFilterGene copy() {
        MarketFilter newFilter = new MarketFilter();
        newFilter.setConfidence(marketfilter.getConfidence());
        newFilter.setDeccategory(marketfilter.getDeccategory());
        newFilter.setDecdays(marketfilter.getDecdays());
        newFilter.setDecthreshold(marketfilter.getDecthreshold());
        newFilter.setInccategory(marketfilter.getInccategory());
        newFilter.setIncdays(marketfilter.getIncdays());
        newFilter.setIncthreshold(marketfilter.getIncthreshold());
        newFilter.setRecordage(marketfilter.getRecordage());
        newFilter.categories = marketfilter.categories;
        return new MarketFilterGene(newFilter, categories);
    }
    
    @Override 
    public String toString() {
        return marketfilter.toString();
    }
}

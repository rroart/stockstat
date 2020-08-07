package roart.evolution.marketfilter.genetics.gene.impl;

import roart.iclij.config.MarketFilter;

import java.util.List;

import roart.common.util.RandomUtil;
import roart.gene.AbstractGene;

public class MarketFilterGene extends AbstractGene {
    protected static final int RANDOMS = 7;
    
    private MarketFilter marketfilter;

    private List<String> categories;

    public MarketFilterGene(MarketFilter marketfilter, List<String> categories) {
        this.marketfilter = marketfilter;
        this.categories = categories;
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
        MarketFilterGene gene = this;
        myconfig.setDeccategory(gene.generateCategory());
        if (random.nextBoolean()) {
            myconfig.setDecdays(gene.generateDays());
        }
        myconfig.setDecthreshold(gene.generateThreshold());
        myconfig.setInccategory(gene.generateCategory());
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
        return new MarketFilterGene(newFilter, categories);
    }
    
    /*
     * private String inccategory;
    
    private Integer incdays;
    
    private Double incthreshold;

    private String deccategory;
    
    private Integer decdays;

    private Double decthreshold;

    private Double confidence;

  
     */
    protected int generateDays() {
        return RandomUtil.random(random, 5, 50);
    }

    protected double generateConfidence() {
        return RandomUtil.random(random, 0.01, 0.01, 100);
    }

    protected double generateThreshold() {
        return RandomUtil.random(random, 0.9, 0.01, 20);
    }

    protected String generateCategory() {
        //String[] categories = { "1w", "1m", "3m", "1y", "3y", "5y", "10y", "cy" };
        int index = random.nextInt(categories.size());
        return categories.get(index);
    }
    
    @Override 
    public String toString() {
        return marketfilter.toString();
    }
}

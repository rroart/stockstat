package roart.evolution.marketfilter.common.gene.impl;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.util.RandomUtil;
import roart.iclij.config.MarketFilter;

public class MarketFilterMutateCommon {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    protected static final int RANDOMS = 7;

    Random random = new Random();
    
    /*
     * private String inccategory;
    
    private Integer incdays;
    
    private Double incthreshold;
    
    private String deccategory;
    
    private Integer decdays;
    
    private Double decthreshold;
    
    private Double confidence;
    
    
     */
    public int generateDays() {
        return RandomUtil.random(random, 5, 50);
    }

    public double generateConfidence() {
        return RandomUtil.random(random, 0.01, 0.01, 100);
    }

    public double generateThreshold() {
        return RandomUtil.random(random, 0.9, 0.01, 20);
    }

    public String generateCategory(MarketFilter myconfig) {
        //String[] categories = { "1w", "1m", "3m", "1y", "3y", "5y", "10y", "cy" };
        if (myconfig.categories.isEmpty()) {
            return null;
        }
        int index = random.nextInt(myconfig.categories.size());
        return myconfig.categories.get(index);
    }

    public void mutateCommon(MarketFilter myconfig) {
        int task = random.nextInt(this.RANDOMS);
        switch (task) {
        case 0:
            myconfig.setDeccategory(this.generateCategory(myconfig));
            break;
        case 1:
            myconfig.setDecdays(this.generateDays());
            break;
        case 2:
            myconfig.setDecthreshold(this.generateThreshold());
            break;
        case 3:
            myconfig.setInccategory(this.generateCategory(myconfig));
            break;
        case 4:
            myconfig.setIncdays(this.generateDays());
            break;
        case 5:
            myconfig.setIncthreshold(this.generateThreshold());
            break;
        case 6:
            myconfig.setConfidence(this.generateConfidence());
            break;
        default:
            log.error(Constants.NOTFOUND, task);
        }
    }

}

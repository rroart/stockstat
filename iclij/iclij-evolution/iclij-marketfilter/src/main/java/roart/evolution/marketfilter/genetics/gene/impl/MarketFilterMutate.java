package roart.evolution.marketfilter.genetics.gene.impl;

import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.iclij.config.MarketFilter;

public class MarketFilterMutate {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    
    Random random = new Random();
    
    public void mutate(MarketFilterGene gene) {
        MarketFilter myconfig = gene.getMarketfilter();
        int task = random.nextInt(gene.RANDOMS);
        switch (task) {
        case 0:
            myconfig.setDeccategory(gene.generateCategory());
            break;
        case 1:
            myconfig.setDecdays(gene.generateDays());
            break;
        case 2:
            myconfig.setDecthreshold(gene.generateThreshold());
            break;
        case 3:
            myconfig.setInccategory(gene.generateCategory());
            break;
        case 4:
            myconfig.setIncdays(gene.generateDays());
            break;
        case 5:
            myconfig.setIncthreshold(gene.generateThreshold());
            break;
        case 6:
            myconfig.setConfidence(gene.generateConfidence());
            break;
        default:
            log.error(Constants.NOTFOUND, task);
        }
    }

}

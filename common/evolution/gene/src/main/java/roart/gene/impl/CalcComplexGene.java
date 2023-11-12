package roart.gene.impl;

import java.util.Random;

import roart.common.util.JsonUtil;
import roart.gene.AbstractGene;
import roart.gene.CalcGene;

public class CalcComplexGene extends CalcGene {

    double minMutateThresholdRange;
    double maxMutateThresholdRange;

    double threshold;
    boolean useminmaxthreshold;
    boolean usethreshold;

    //boolean divideminmaxthreshold;

    int weight;

    //boolean changeSignWhole;

    // not mutatable

    boolean useMax;

    //boolean threshMaxValue; //?
    //boolean norm;

    public double getMinMutateThresholdRange() {
        return minMutateThresholdRange;
    }

    public void setMinMutateThresholdRange(double minMutateThresholdRange) {
        this.minMutateThresholdRange = minMutateThresholdRange;
    }

    public double getMaxMutateThresholdRange() {
        return maxMutateThresholdRange;
    }

    public void setMaxMutateThresholdRange(double maxMutateThresholdRange) {
        this.maxMutateThresholdRange = maxMutateThresholdRange;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public boolean isUseminmaxthreshold() {
        return useminmaxthreshold;
    }

    public void setUseminmaxthreshold(boolean useminmaxthreshold) {
        this.useminmaxthreshold = useminmaxthreshold;
    }

    public boolean isUsethreshold() {
        return usethreshold;
    }

    public void setUsethreshold(boolean usethreshold) {
        this.usethreshold = usethreshold;
    }

    /*
    public boolean isDivideminmaxthreshold() {
        return divideminmaxthreshold;
    }

    public void setDivideminmaxthreshold(boolean divideminmaxthreshold) {
        this.divideminmaxthreshold = divideminmaxthreshold;
    }
*/
    
    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    /*
    public boolean isChangeSignWhole() {
        return changeSignWhole;
    }

    public void setChangeSignWhole(boolean changeSignWhole) {
        this.changeSignWhole = changeSignWhole;
    }
*/
    
    public boolean isUseMax() {
        return useMax;
    }

    public void setUseMax(boolean doBuy) {
        this.useMax = doBuy;
    }

    @Override
    public double calc(double val, double minmaxthreshold2) {
        double minmaxthreshold = maxMutateThresholdRange;
        if (!useMax) {
            minmaxthreshold = minMutateThresholdRange;
        }
        double mythreshold = 0;
        if (usethreshold) {
            mythreshold = threshold;
        }
        if (useminmaxthreshold) {
            mythreshold = minmaxthreshold;
        }
        double myvalue = val - mythreshold;
        // if usemax...?
        /*
        if (false && divideminmaxthreshold) {
            myvalue = myvalue / minmaxthreshold;
        }
        */
        if (maxMutateThresholdRange != minMutateThresholdRange) {
        myvalue = myvalue / (maxMutateThresholdRange - minMutateThresholdRange);
        } else {
            int jj = 0;
        }
        
        /*
        if (changeSignWhole) {
            myvalue = -myvalue;
        }
        */
        return myvalue * weight;
    }

    @Override
    public void randomize() {
        //getUseminmaxthreshold(rand);
        getUsethreshold(random);
        //getDivideminmaxthreshold(rand); // not
        //getChangeSignWhole(rand); // not
        getWeight(random);
        getThreshold(random);
    }

    private void getThreshold(Random rand) {
        /*
        if (useMax) {
            threshold = rand.nextDouble() * maxMutateThresholdRange;
        } else {
            threshold = rand.nextDouble() * minMutateThresholdRange;       
        }
        */
        threshold = minMutateThresholdRange + rand.nextDouble() * (maxMutateThresholdRange - minMutateThresholdRange);
        if (Double.isNaN(threshold)) {
            int jj = 0;
        }
    }

    private void getWeight(Random rand) {
        weight = 1 + rand.nextInt(100);
    }

    /*
    private void getChangeSignWhole(Random rand) {
        changeSignWhole = rand.nextBoolean();
    }

    private void getDivideminmaxthreshold(Random rand) {
        divideminmaxthreshold = rand.nextBoolean();
    }
    */

    private void getUsethreshold(Random rand) {
        usethreshold = rand.nextBoolean();
    }

    private void getUseminmaxthreshold(Random rand) {
        useminmaxthreshold = rand.nextBoolean();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(3);
        switch (task) {
        /*
        case 0:
            getUseminmaxthreshold(rand);
            break;
            */
        case 0:
            getUsethreshold(random);
            break;
        case 1:
            getWeight(random);
            break;
        case 2: 
            getThreshold(random);
            break;
            /*
        case 2:
            getDivideminmaxthreshold(rand);
            break;
        case 3:
            getChangeSignWhole(rand);
            break;
            */
        default:
            log.error("Too many");
            break;
        }

    }

    @Override
    public AbstractGene crossover(AbstractGene other) {
        CalcComplexGene node = new CalcComplexGene();
        node.setMaxMutateThresholdRange(maxMutateThresholdRange);
        node.setMinMutateThresholdRange(minMutateThresholdRange);
        node.setUseminmaxthreshold(useminmaxthreshold);
        if (random.nextBoolean()) {
            node.setThreshold(threshold);
        } else {
            node.setThreshold(((CalcComplexGene) other).getThreshold());
        }
        if (random.nextBoolean()) {
            node.setUseminmaxthreshold(useminmaxthreshold);
        } else {
            node.setUseminmaxthreshold(((CalcComplexGene) other).isUseminmaxthreshold());            
        }
        if (random.nextBoolean()) {
            node.setWeight(weight);
        } else {
            node.setWeight(((CalcComplexGene) other).getWeight());            
        }
        return node;
    }
    
    @Override
    public String toString() {
        return JsonUtil.convert(this);
    }
}
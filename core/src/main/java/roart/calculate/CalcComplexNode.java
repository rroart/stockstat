package roart.calculate;

import java.util.Random;

public class CalcComplexNode extends CalcNode {

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
        Random rand = new Random();
        //getUseminmaxthreshold(rand);
        getUsethreshold(rand);
        //getDivideminmaxthreshold(rand); // not
        //getChangeSignWhole(rand); // not
        getWeight(rand);
        getThreshold(rand);
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
        Random rand = new Random();
        int task = rand.nextInt(3);
        switch (task) {
        /*
        case 0:
            getUseminmaxthreshold(rand);
            break;
            */
        case 0:
            getUsethreshold(rand);
            break;
        case 1:
            getWeight(rand);
            break;
        case 2: 
            getThreshold(rand);
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
            System.out.println("Too many");
            break;
        }

    }
}
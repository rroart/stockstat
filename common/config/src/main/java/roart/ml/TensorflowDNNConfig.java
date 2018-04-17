package roart.ml;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.config.ConfigConstants;
import roart.config.MLConstants;

public class TensorflowDNNConfig extends TensorflowConfig {
    private Integer steps;
    
    private Integer prevhiddenlayers;
    
    private Integer[] prevhiddenunits;

    private Integer hiddenlayers;
    
    private Integer[] hiddenunits;

    public TensorflowDNNConfig(Integer steps, Integer hiddenlayers) {
        super(MLConstants.DNN);
        this.steps = steps;
        this.hiddenlayers = hiddenlayers;
    }

    public TensorflowDNNConfig() {
        super(MLConstants.DNN);
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Integer getHiddenlayers() {
        return hiddenlayers;
    }

    public void setHiddenlayers(Integer hiddenlayers) {
        this.hiddenlayers = hiddenlayers;
    }

    public Integer[] getHiddenunits() {
        return hiddenunits;
    }

    public void setHiddenunits(Integer[] hiddenunits) {
        this.hiddenunits = hiddenunits;
    }

    @Override
    public void randomize() {
        Random rand = new Random();
        generateSteps(rand);
        generateHiddenlayers(rand);
        generateHiddenUnits(rand);
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int task = rand.nextInt(3);
        switch (task) {
        case 0:
            generateHiddenlayers(rand);
            break;
        case 1:
            mutateHiddenUnits(rand);
            break;
        case 2:
            generateSteps(rand);
            break;
        }
    }

    private void mutateHiddenUnits(Random rand) {
        if (hiddenlayers == null || hiddenunits == null) {
                int jj = 0;
            }
        int hiddenlayer = rand.nextInt(hiddenlayers);
        if (hiddenlayer >= hiddenunits.length) {
            int jj = 0;
        }
        hiddenunits[hiddenlayer] = ThreadLocalRandom.current().nextInt(2, 50);
    }

    private void generateSteps(Random rand) {
        steps = 1 + rand.nextInt(200);
    }

    private void generateHiddenUnits(Random rand) {
        prevhiddenunits = hiddenunits;
        hiddenunits = new Integer[hiddenlayers];
        if (prevhiddenunits == null) {
            for(int i = 0; i < hiddenlayers; i++) {
                hiddenunits[i] = ThreadLocalRandom.current().nextInt(2, 50);
            }
        } else {
            
        }
    }

    private void generateHiddenlayers(Random rand) {
        prevhiddenlayers = hiddenlayers;
        hiddenlayers = ThreadLocalRandom.current().nextInt(1, 5);
        if (prevhiddenlayers != null && prevhiddenlayers.intValue() != hiddenlayers.intValue()) {
            prevhiddenunits = hiddenunits;
            hiddenunits = new Integer[hiddenlayers];
            int min = Math.min(prevhiddenlayers, hiddenlayers);
            if (prevhiddenunits != null) {
                for (int i = 0; i < min; i++) {
                    hiddenunits[i] = prevhiddenunits[i];
                }
            }
            for (int i = min; i < hiddenlayers; i++) {
                hiddenunits[i] = ThreadLocalRandom.current().nextInt(2, 50);
            }
        }
    }

    @Override
    public NNConfig crossover(NNConfig otherNN) {
        TensorflowDNNConfig offspring = new TensorflowDNNConfig(steps, hiddenlayers);
        offspring.setHiddenunits(Arrays.copyOf(hiddenunits, hiddenunits.length));
        TensorflowDNNConfig other = (TensorflowDNNConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.steps = other.getSteps();
        }
       if (rand.nextBoolean()) {
            offspring.hiddenlayers = other.getHiddenlayers();
        }
        if (rand.nextBoolean()) {
            offspring.hiddenunits = other.getHiddenunits();
        }
        return offspring;
    }

    @Override
    public NNConfig copy() {
        TensorflowDNNConfig newL = new TensorflowDNNConfig(hiddenlayers, hiddenlayers);
        if (hiddenunits != null) {
            newL.setHiddenunits(Arrays.copyOf(hiddenunits, hiddenunits.length));
        }
        return newL;
    }
    
    @Override
    public String toString() {
        String array = "";
        if (hiddenunits != null) {
            array = Arrays.toString(hiddenunits);
        }
        return getName() + " " + hiddenlayers + " " + array + " " + steps;
    }
}

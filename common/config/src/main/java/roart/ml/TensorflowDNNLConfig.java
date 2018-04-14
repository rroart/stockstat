package roart.ml;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.config.ConfigConstants;
import roart.config.MLConstants;

public class TensorflowDNNLConfig extends TensorflowConfig {
    private Integer steps;
    
    private Integer prevdnnhiddenlayers;
    
    private Integer[] prevdnnhiddenunits;

    private Integer dnnhiddenlayers;
    
    private Integer[] dnnhiddenunits;

    public TensorflowDNNLConfig(Integer steps, Integer hiddenlayers) {
        super(MLConstants.DNNL);
        this.steps = steps;
        this.dnnhiddenlayers = hiddenlayers;
    }

    public TensorflowDNNLConfig() {
        super(MLConstants.DNNL);
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    public Integer getDnnhiddenlayers() {
        return dnnhiddenlayers;
    }

    public void setDnnhiddenlayers(Integer dnnhiddenlayers) {
        this.dnnhiddenlayers = dnnhiddenlayers;
    }

    public Integer[] getDnnhiddenunits() {
        return dnnhiddenunits;
    }

    public void setDnnhiddenunits(Integer[] dnnhiddenunits) {
        this.dnnhiddenunits = dnnhiddenunits;
    }

    @Override
    public void randomize() {
        Random rand = new Random();
        generateHiddenlayers(rand);
        generateHiddenUnits(rand);
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int task = rand.nextInt(2);
        switch (task) {
        case 0:
            generateHiddenlayers(rand);
            break;
        case 1:
            mutateHiddenUnits(rand);
            break;
        }
    }

    private void mutateHiddenUnits(Random rand) {
        if (dnnhiddenlayers == null) {
                int jj = 0;
            }
        int hiddenlayer = rand.nextInt(dnnhiddenlayers);
        dnnhiddenunits[hiddenlayer] = ThreadLocalRandom.current().nextInt(2, 50);
    }

    private void generateHiddenUnits(Random rand) {
        prevdnnhiddenunits = dnnhiddenunits;
        dnnhiddenunits = new Integer[dnnhiddenlayers];
        if (prevdnnhiddenunits == null) {
            for(int i = 0; i < dnnhiddenlayers; i++) {
                dnnhiddenunits[i] = ThreadLocalRandom.current().nextInt(2, 50);
            }
        } else {
            
        }
    }

    private void generateHiddenlayers(Random rand) {
        prevdnnhiddenlayers = dnnhiddenlayers;
        dnnhiddenlayers = ThreadLocalRandom.current().nextInt(1, 5);
        if (prevdnnhiddenlayers != null && prevdnnhiddenlayers.intValue() != dnnhiddenlayers.intValue()) {
            prevdnnhiddenunits = dnnhiddenunits;
            dnnhiddenunits = new Integer[dnnhiddenlayers];
            int min = Math.min(prevdnnhiddenlayers, dnnhiddenlayers);
            for (int i = 0; i < min; i++) {
                dnnhiddenunits[i] = prevdnnhiddenunits[i];
            }
            for (int i = min; i < dnnhiddenlayers; i++) {
                dnnhiddenunits[i] = ThreadLocalRandom.current().nextInt(2, 50);
            }
        }
    }

    @Override
    public NNConfig crossover(NNConfig otherNN) {
        TensorflowDNNLConfig offspring = new TensorflowDNNLConfig(steps, dnnhiddenlayers);
        offspring.setDnnhiddenunits(Arrays.copyOf(dnnhiddenunits, dnnhiddenunits.length));
        TensorflowDNNLConfig other = (TensorflowDNNLConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.steps = other.getSteps();
        }
       if (rand.nextBoolean()) {
            offspring.dnnhiddenlayers = other.getDnnhiddenlayers();
        }
        if (rand.nextBoolean()) {
            offspring.dnnhiddenunits = other.getDnnhiddenunits();
        }
        return offspring;
    }

    @Override
    public NNConfig copy() {
        TensorflowDNNLConfig newL = new TensorflowDNNLConfig(dnnhiddenlayers, dnnhiddenlayers);
        if (dnnhiddenunits != null) {
            newL.setDnnhiddenunits(Arrays.copyOf(dnnhiddenunits, dnnhiddenunits.length));
        }
        return newL;
    }
    
    @Override
    public String toString() {
        String array = "";
        if (dnnhiddenunits != null) {
            array = Arrays.toString(dnnhiddenunits);
        }
        return getName() + " " + dnnhiddenlayers + " " + array + " " + steps;
    }
}

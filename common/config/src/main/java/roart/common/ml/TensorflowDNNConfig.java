package roart.common.ml;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;

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
        //validate();
        Random rand = new Random();
        generateSteps(rand);
        generateHiddenlayers(rand);
        //generateHiddenUnits(rand);
        validate();
    }

    @Override
    public void mutate() {
        validate();
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
        validate();
    }

    private void mutateHiddenUnits(Random rand) {
        if (hiddenlayers == null || hiddenunits == null) {
                int jj = 0;
            }
        int hiddenlayer = rand.nextInt(hiddenlayers);
        if (hiddenlayer >= hiddenunits.length) {
            int jj = 0;
        }
        hiddenunits[hiddenlayer] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
    }

    private void generateSteps(Random rand) {
        steps = 1 + rand.nextInt(MAX_STEPS);
    }

    private void generateHiddenUnits(Random rand) {
        prevhiddenunits = hiddenunits;
        hiddenunits = new Integer[hiddenlayers];
        if (prevhiddenunits == null) {
            for(int i = 0; i < hiddenlayers; i++) {
                hiddenunits[i] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
            }
        } else {
            
        }
    }

    private void generateHiddenlayers(Random rand) {
        prevhiddenlayers = hiddenlayers;
        hiddenlayers = ThreadLocalRandom.current().nextInt(1, MAX_HIDDENLAYERS + 1);
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
                hiddenunits[i] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
            }
        } else {
            if (hiddenunits == null) {
                hiddenunits = new Integer[hiddenlayers];
            } else {
                int jj = 0;
            }
            for(int i = 0; i < hiddenlayers; i++) {
                hiddenunits[i] = ThreadLocalRandom.current().nextInt(MIN_NODE, MAX_NODE + 1);
            }
        }
    }

    @Override
    public NeuralNetConfig crossover(NeuralNetConfig otherNN) {
        validate();
        TensorflowDNNConfig offspring = new TensorflowDNNConfig(steps, hiddenlayers);
        offspring.setHiddenunits(Arrays.copyOf(hiddenunits, hiddenunits.length));
        TensorflowDNNConfig other = (TensorflowDNNConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.steps = other.getSteps();
        }
        if (rand.nextBoolean()) {
            offspring.hiddenlayers = other.getHiddenlayers();
            offspring.hiddenunits = Arrays.copyOf(other.hiddenunits, other.hiddenunits.length);
        }
        offspring.validate();
        return offspring;
    }

    @Override
    public NeuralNetConfig copy() {
        TensorflowDNNConfig newL = new TensorflowDNNConfig(hiddenlayers, hiddenlayers);
        if (hiddenunits != null) {
            newL.setHiddenunits(Arrays.copyOf(hiddenunits, hiddenunits.length));
        }
        return newL;
    }
    
    @Override
    public boolean empty() {
        return steps == null;
    }
    
   @Override
    public String toString() {
        String array = "";
        if (hiddenunits != null) {
            array = Arrays.toString(hiddenunits);
        }
        return getName() + " " + hiddenlayers + " " + array + " " + steps;
    }
    
    private void validate() {
        if (steps == null || steps == 0) {
            int jj = 0;
        }
        if (hiddenlayers == null || hiddenlayers == 0) {
            int jj = 0;
        }
        if (hiddenunits != null && hiddenlayers != null) {
            if (hiddenlayers != hiddenunits.length) {
                int jj = 0;
            }
        }
        if (hiddenunits == null) {
            int jj = 0;
        } else {
            for (int i = 0; i < hiddenlayers; i++) {
                if (hiddenunits[i] == null || hiddenunits[i] == 0) {
                    int jj = 0;
                }
            }
        }
    }
}

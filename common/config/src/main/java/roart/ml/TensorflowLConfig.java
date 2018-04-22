package roart.ml;

import java.util.Random;

import roart.config.ConfigConstants;
import roart.config.MLConstants;

public class TensorflowLConfig extends TensorflowConfig {
    private Integer steps;    

    public TensorflowLConfig(Integer steps) {
        super(MLConstants.L);
        this.steps = steps;
    }

    public TensorflowLConfig() {
        super(MLConstants.L);
    }

    public Integer getSteps() {
        return steps;
    }

    public void setSteps(Integer steps) {
        this.steps = steps;
    }

    @Override
    public void randomize() {
        Random rand = new Random();
        steps = 1 + rand.nextInt(MAX_STEPS);
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        steps = 1 + rand.nextInt(MAX_STEPS);
    }

    @Override
    public NNConfig crossover(NNConfig otherNN) {
        TensorflowLConfig offspring = new TensorflowLConfig(steps);
        TensorflowLConfig other = (TensorflowLConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.steps = other.getSteps();
        }
        return offspring;
     }

    @Override
    public NNConfig copy() {
         return new TensorflowLConfig(steps);
    }
    
    @Override
    public boolean empty() {
        return steps == null;
    }
    
    @Override
    public String toString() {
        return getName() + " " + steps;
    }
}

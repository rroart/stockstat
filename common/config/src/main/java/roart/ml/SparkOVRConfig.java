package roart.ml;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.config.ConfigConstants;
import roart.config.MLConstants;

public class SparkOVRConfig extends SparkConfig {
    private Integer maxiter;

    private Double tol;
    
    private Boolean fitintercept;

    public Integer getMaxiter() {
        return maxiter;
    }

    public void setMaxiter(Integer maxiter) {
        this.maxiter = maxiter;
    }

    public Double getTol() {
        return tol;
    }

    public void setTol(Double tol) {
        this.tol = tol;
    }

    public Boolean getFitintercept() {
        return fitintercept;
    }

    public void setFitintercept(Boolean fitintercept) {
        this.fitintercept = fitintercept;
    }
    
    public SparkOVRConfig(Integer maxiter, Double tol, Boolean fitintercept) {
        super(MLConstants.OVR);
        this.maxiter = maxiter;
        this.tol = tol;
        this.fitintercept = fitintercept;
    }

    public SparkOVRConfig() {
        super(MLConstants.OVR);
    }

    @Override
    public void randomize() {
        Random rand = new Random();
            generateMaxiter(rand);
            tol = generateTol();
            generateFitintercept(rand);
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int task = rand.nextInt(3);
        switch (task) {
        case 0:
            generateMaxiter(rand);
            break;
        case 1:
            tol = generateTol();
            break;
        case 2:
            generateFitintercept(rand);
            break;
        }
    }

    private void generateMaxiter(Random rand) {
        maxiter = 1 + rand.nextInt(MAX_ITER);
    }

    private void generateFitintercept(Random rand) {
        fitintercept = rand.nextBoolean();
    }
    
    private void generateTol(Random rand) {
        tol = ThreadLocalRandom.current().nextDouble(0, 1);
    }

    @Override
    public NNConfig crossover(NNConfig otherNN) {
        SparkOVRConfig offspring = new SparkOVRConfig(maxiter, tol, fitintercept);
        SparkOVRConfig other = (SparkOVRConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.maxiter = other.getMaxiter();
        }
        if (rand.nextBoolean()) {
            offspring.tol = other.getTol();
        }
        if (rand.nextBoolean()) {
            offspring.fitintercept = other.getFitintercept();
        }
        return offspring;
    }

    @Override
    public NNConfig copy() {
        return new SparkOVRConfig(maxiter, tol, fitintercept);
    }
    
    @Override
    public boolean empty() {
        return maxiter == null;
    }
    
    @Override
    public String toString() {
        return getName() + " " + maxiter + " " + tol + " " + fitintercept;
    }

    @Override
    public Double generateTol() {
        return super.generateTol();
        //return Math.pow(0.1, ThreadLocalRandom.current().nextInt(MIN_TOL - 1, MAX_TOL + 1));
    }
    
}

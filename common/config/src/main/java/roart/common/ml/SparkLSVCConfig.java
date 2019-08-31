package roart.common.ml;

import java.util.Random;

import roart.common.config.MLConstants;

public class SparkLSVCConfig extends SparkConfig {
    private Random rand = new Random();

    private Integer maxiter;

    private Double tol;
    
    private Boolean fitintercept;

    public SparkLSVCConfig(Integer maxiter, Double tol, Boolean fitintercept) {
        super(MLConstants.LSVC);
        this.maxiter = maxiter;
	this.tol = tol;
	this.fitintercept = fitintercept;
    }

    public SparkLSVCConfig() {
        super(MLConstants.LSVC);
    }

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

    @Override
    public void randomize() {
        //validate();
        generateMaxiter(rand);
        tol = generateTol();
        generateFitintercept(rand);
        validate();
    }

    private void generateMaxiter(Random rand) {
        maxiter = 1 + rand.nextInt(MAX_ITER);
    }

    private void generateFitintercept(Random rand) {
        fitintercept = rand.nextBoolean();
    }
    
    @Override
    public void mutate() {
        validate();
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
        validate();
    }

    @Override
    public NeuralNetConfig crossover(NeuralNetConfig otherNN) {
        validate();
        SparkLSVCConfig offspring = new SparkLSVCConfig(maxiter, tol, fitintercept);
        SparkLSVCConfig other = (SparkLSVCConfig) otherNN;
        if (rand.nextBoolean()) {
            offspring.maxiter = other.getMaxiter();
        }
        if (rand.nextBoolean()) {
            offspring.tol = other.getTol();
        }
        if (rand.nextBoolean()) {
            offspring.fitintercept = other.getFitintercept();
        }
        offspring.validate();
        return offspring;
    }

    @Override
    public NeuralNetConfig copy() {
        SparkLSVCConfig newLSVC = new SparkLSVCConfig(maxiter, tol, fitintercept);
        return newLSVC;
    }

    @Override
    public boolean empty() {
        return maxiter == null;
    }
    
    @Override
    public String toString() {
        return getName() + " " + maxiter + " " + tol + " " + fitintercept;
    }

    private void validate() {
        if (maxiter == null || maxiter == 0) {
            int jj = 0;
        }
    }

    @Override
    public Double generateTol() {
        return super.generateTol();
    }
}

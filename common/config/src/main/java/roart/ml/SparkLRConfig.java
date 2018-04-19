package roart.ml;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.config.ConfigConstants;
import roart.config.MLConstants;

public class SparkLRConfig extends SparkConfig {
    private Integer maxiter;
    
    private Double reg;
    
    private Double elasticnet;

    public SparkLRConfig(Integer maxiter, Double reg, Double elasticnet) {
        super(MLConstants.LR);
        this.maxiter = maxiter;
        this.reg = reg;
        this.elasticnet = elasticnet;
    }

    public SparkLRConfig() {
        super(MLConstants.LR);
    }

    public Integer getMaxiter() {
        return maxiter;
    }

    public void setMaxiter(Integer maxiter) {
        this.maxiter = maxiter;
    }

    public Double getReg() {
        return reg;
    }

    public void setReg(Double reg) {
        this.reg = reg;
    }

    public Double getElasticnet() {
        return elasticnet;
    }

    public void setElasticnet(Double elasticnet) {
        this.elasticnet = elasticnet;
    }

    @Override
    public void randomize() {
        Random rand = new Random();
        generateMaxiter(rand);
        generateReg(rand);
        generateElasticnet(rand);
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
            generateReg(rand);
            break;
        case 2:
            generateElasticnet(rand);
            break;
        }
    }

    private void generateElasticnet(Random rand) {
        elasticnet = ThreadLocalRandom.current().nextDouble(0, 1);
    }

    private void generateReg(Random rand) {
        reg = ThreadLocalRandom.current().nextDouble(0, 1);
    }

    private void generateMaxiter(Random rand) {
        maxiter = 1 + rand.nextInt(200);
    }

    @Override
    public NNConfig crossover(NNConfig otherNN) {
        SparkLRConfig baby = new SparkLRConfig(maxiter, reg, elasticnet);
        SparkLRConfig other = (SparkLRConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            baby.maxiter = other.getMaxiter();
        }
        if (rand.nextBoolean()) {
            baby.reg = other.getReg();
        }
        if (rand.nextBoolean()) {
            baby.elasticnet = other.getElasticnet();
        }
        return baby;
    }

    @Override
    public NNConfig copy() {
        return new SparkLRConfig(maxiter, reg, elasticnet);
    }
    
    @Override
    public boolean empty() {
        return maxiter == null;
    }
    @Override
    public String toString() {
        return getName() + " " + maxiter + " " + reg + " " + elasticnet;
    }
}
package roart.common.ml;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;

public class SparkLRConfig extends SparkConfig {
    private Integer maxiter;
    
    //private Double reg;
    
    //private Double elasticnet;

    private Double tol;
    
    public SparkLRConfig(Integer maxiter, Double tol) {
        super(MLConstants.LR);
        this.maxiter = maxiter;
	this.tol = tol;
        //this.reg = reg;
        //this.elasticnet = elasticnet;
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

    /*
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
*/
    
    public Double getTol() {
        return tol;
    }

    public void setTol(Double tol) {
        this.tol = tol;
    }

    @Override
    public void randomize() {
        Random rand = new Random();
        generateMaxiter(rand);
        //generateReg(rand);
        //generateElasticnet(rand);
	tol = generateTol();
    }

    @Override
    public void mutate() {
        Random rand = new Random();
        int task = rand.nextInt(2);
        switch (task) {
        case 0:
            generateMaxiter(rand);
            break;
	    /*
        case 1:
            generateReg(rand);
            break;
        case 2:
            generateElasticnet(rand);
            break;
	    */
	case 1:
	    tol = generateTol();
	    break;
        }
    }

    /*
    private void generateElasticnet(Random rand) {
        elasticnet = ThreadLocalRandom.current().nextDouble(0, 1);
    }

    private void generateReg(Random rand) {
        reg = ThreadLocalRandom.current().nextDouble(0, 1);
    }
*/
    
    private void generateMaxiter(Random rand) {
        maxiter = 1 + rand.nextInt(MAX_ITER);
    }

    @Override
    public NeuralNetConfig crossover(NeuralNetConfig otherNN) {
        //SparkLRConfig offspring = new SparkLRConfig(maxiter, reg, elasticnet);
        SparkLRConfig offspring = new SparkLRConfig(maxiter, tol);
        SparkLRConfig other = (SparkLRConfig) otherNN;
        Random rand = new Random();
        if (rand.nextBoolean()) {
            offspring.maxiter = other.getMaxiter();
        }
        if (rand.nextBoolean()) {
            offspring.tol = other.tol;
        }
        /*
        if (rand.nextBoolean()) {
            offspring.reg = other.getReg();
        }
        if (rand.nextBoolean()) {
            offspring.elasticnet = other.getElasticnet();
        }
        */
        return offspring;
    }

    @Override
    public NeuralNetConfig copy() {
        return new SparkLRConfig(maxiter, tol);
        //return new SparkLRConfig(maxiter, reg, elasticnet);
    }
    
    @Override
    public boolean empty() {
        return maxiter == null;
    }
    @Override
    public String toString() {
        return getName() + " " + maxiter + " " + tol;
        //return getName() + " " + maxiter + " " + reg + " " + elasticnet;
    }

}

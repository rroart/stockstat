package roart.common.ml;

import roart.common.config.MLConstants;

public class SparkLORConfig extends SparkConfig {
    
    //private Double reg;
    
    //private Double elasticnet;

    public SparkLORConfig(int maxiter, double tol) {
        super(MLConstants.LOR, maxiter, tol);
        //this.reg = reg;
        //this.elasticnet = elasticnet;
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
    
    /*
    private void generateElasticnet(Random rand) {
        elasticnet = ThreadLocalRandom.current().nextDouble(0, 1);
    }

    private void generateReg(Random rand) {
        reg = ThreadLocalRandom.current().nextDouble(0, 1);
    }
*/
    
}

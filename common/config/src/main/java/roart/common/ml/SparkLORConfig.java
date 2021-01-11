package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class SparkLORConfig extends SparkConfig {
    
    //private Double reg;
    
    //private Double elasticnet;

    @JsonCreator
    public SparkLORConfig(
            @JsonProperty("maxiter") int maxiter, 
            @JsonProperty("tol") double tol) {
        super(MLConstants.LOR, maxiter, tol);
        //this.reg = reg;
        //this.elasticnet = elasticnet;
    }

    public SparkLORConfig(String name) {
        super(name);
    }

    public SparkLORConfig(SparkLORConfig config) {
        this(config.getMaxiter(), config.getTol());
    }

    public SparkLORConfig() {
        super();
        // JSON
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

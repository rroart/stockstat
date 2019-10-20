package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class SparkOVRConfig extends SparkConfig {
    
    private Boolean fitintercept;

    public Boolean getFitintercept() {
        return fitintercept;
    }

    public void setFitintercept(Boolean fitintercept) {
        this.fitintercept = fitintercept;
    }
    
    @JsonCreator
    public SparkOVRConfig(
            @JsonProperty("maxiter") Integer maxiter, 
            @JsonProperty("tol") Double tol, 
            @JsonProperty("fitintercept") Boolean fitintercept) {
        super(MLConstants.OVR, maxiter, tol);
        this.fitintercept = fitintercept;
    }

    public SparkOVRConfig(SparkOVRConfig config) {
        this(config.getMaxiter(), config.getTol(), config.getFitintercept());
    }

    public SparkOVRConfig(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString() + " " + fitintercept;
    }

}

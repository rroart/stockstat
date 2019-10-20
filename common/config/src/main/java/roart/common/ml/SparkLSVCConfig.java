package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class SparkLSVCConfig extends SparkConfig {

    private Boolean fitintercept;

    @JsonCreator
    public SparkLSVCConfig(
            @JsonProperty("maxiter") Integer maxiter, 
            @JsonProperty("tol") Double tol, 
            @JsonProperty("fitintercept") Boolean fitintercept) {
        super(MLConstants.LSVC, maxiter, tol);
	this.fitintercept = fitintercept;
    }

    public SparkLSVCConfig(SparkLSVCConfig config) {
        this(config.getMaxiter(), config.getTol(), config.getFitintercept());
    }

    public SparkLSVCConfig(String name) {
        super(name);
    }

    public Boolean getFitintercept() {
        return fitintercept;
    }

    public void setFitintercept(Boolean fitintercept) {
        this.fitintercept = fitintercept;
    }

    @Override
    public String toString() {
        return super.toString() + " " + fitintercept;
    }

}

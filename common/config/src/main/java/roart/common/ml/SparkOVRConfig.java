package roart.common.ml;

import roart.common.config.MLConstants;

public class SparkOVRConfig extends SparkConfig {
    
    private Boolean fitintercept;

    public Boolean getFitintercept() {
        return fitintercept;
    }

    public void setFitintercept(Boolean fitintercept) {
        this.fitintercept = fitintercept;
    }
    
    public SparkOVRConfig(Integer maxiter, Double tol, Boolean fitintercept) {
        super(MLConstants.OVR, maxiter, tol);
        this.fitintercept = fitintercept;
    }

    @Override
    public String toString() {
        return super.toString() + " " + fitintercept;
    }

}

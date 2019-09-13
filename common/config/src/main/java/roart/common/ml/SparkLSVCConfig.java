package roart.common.ml;

import roart.common.config.MLConstants;

public class SparkLSVCConfig extends SparkConfig {

    private Boolean fitintercept;

    public SparkLSVCConfig(Integer maxiter, Double tol, Boolean fitintercept) {
        super(MLConstants.LSVC, maxiter, tol);
	this.fitintercept = fitintercept;
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

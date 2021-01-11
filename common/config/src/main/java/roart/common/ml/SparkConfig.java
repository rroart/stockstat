package roart.common.ml;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(  
        use = JsonTypeInfo.Id.NAME,  
        include = JsonTypeInfo.As.PROPERTY,  
        property = "_class")  
@JsonSubTypes({  
    @Type(value = SparkLORConfig.class, name = "SparkLORConfig"),
    @Type(value = SparkLSVCConfig.class, name = "SparkLSVCConfig"),
    @Type(value = SparkMLPCConfig.class, name = "SparkMLPCConfig"),
    @Type(value = SparkOVRConfig.class, name = "SparkOVRConfig") })  
public abstract class SparkConfig extends NeuralNetConfig {

    private int maxiter;

    private double tol;
        
    public int getMaxiter() {
        return maxiter;
    }

    public void setMaxiter(int maxiter) {
        this.maxiter = maxiter;
    }

    public double getTol() {
        return tol;
    }

    public void setTol(double tol) {
        this.tol = tol;
    }

    public SparkConfig(String name, int maxiter, double tol) {
        super(name);
        this.maxiter = maxiter;
        this.tol = tol;
    }

    public SparkConfig(String name) {
        super(name);
    }

    public SparkConfig() {
        super();
        // JSON
    }

    @Override
    public NeuralNetConfig copy() {
        return this;
    }
    
    @Override
    public boolean empty() {
        return maxiter == 0;
    }

    @Override
    public String toString() {
        return getName() + " " + maxiter + " " + tol;
    }
}

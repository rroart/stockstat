package roart.common.ml;

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

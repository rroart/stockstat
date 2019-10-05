package roart.common.ml;

public abstract class TensorflowFeedConfig extends TensorflowConfig {

    protected int layers;
    
    protected int hidden;
    
    protected double lr;
    
    public int getLayers() {
        return layers;
    }

    public void setLayers(int layers) {
        this.layers = layers;
    }

    public int getHidden() {
        return hidden;
    }

    public void setHidden(int hidden) {
        this.hidden = hidden;
    }

    public double getLr() {
        return lr;
    }

    public void setLr(double lr) {
        this.lr = lr;
    }

    public TensorflowFeedConfig(String name, int steps, int layers, int hidden, double lr) {
        super(name, steps);
        this.layers = layers;
        this.hidden = hidden;
        this.lr = lr;        
    }

    public TensorflowFeedConfig(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString() + " " + layers + " " + hidden + " " + " " + lr;
    }
}

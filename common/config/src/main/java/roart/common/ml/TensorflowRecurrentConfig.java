package roart.common.ml;

public abstract class TensorflowRecurrentConfig extends TensorflowFeedConfig {

    protected int slide;
    
    protected double dropoutin;
    
    protected double dropout;
    
    public int getSlide() {
        return slide;
    }

    public void setSlide(int slide) {
        this.slide = slide;
    }

    public double getDropoutin() {
        return dropoutin;
    }

    public void setDropoutin(double dropoutin) {
        this.dropoutin = dropoutin;
    }

    public double getDropout() {
        return dropout;
    }

    public void setDropout(double dropout) {
        this.dropout = dropout;
    }

    public TensorflowRecurrentConfig(String name, int steps, int layers, int hidden, double lr, int slide, double dropout, double dropoutin) {
        super(name, steps, layers, hidden, lr);
        this.slide = slide;
        this.dropout = dropout;
        this.dropoutin = dropoutin;
    }

    @Override
    public String toString() {
        return super.toString() + " " + slide + " " + dropout + " " + dropoutin;
    }
}

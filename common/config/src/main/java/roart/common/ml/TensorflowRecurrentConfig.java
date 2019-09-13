package roart.common.ml;

public abstract class TensorflowRecurrentConfig extends TensorflowFeedConfig {

    protected int slide;
    
    public int getSlide() {
        return slide;
    }

    public void setSlide(int slide) {
        this.slide = slide;
    }

    public TensorflowRecurrentConfig(String name, int steps, int layers, int hidden, double lr, int slide) {
        super(name, steps, layers, hidden, lr);
        this.slide = slide;
    }

}

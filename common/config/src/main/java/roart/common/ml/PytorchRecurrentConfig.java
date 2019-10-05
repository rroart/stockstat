package roart.common.ml;

public abstract class PytorchRecurrentConfig extends PytorchFeedConfig {

    protected int slide;

    public int getSlide() {
        return slide;
    }

    public void setSlide(int slide) {
        this.slide = slide;
    }

    public PytorchRecurrentConfig(String name, int steps, int layers, int hidden, double lr, int slide) {
        super(name, steps, layers, hidden, lr);
        this.slide = slide;
    }
    
    public PytorchRecurrentConfig(String name) {
        super(name);
    }

    @Override
    public String toString() {
        return super.toString() + " " + slide;
    }
}

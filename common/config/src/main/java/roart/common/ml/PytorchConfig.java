package roart.common.ml;

public abstract class PytorchConfig extends NeuralNetConfig {

    protected int steps;
    
    public PytorchConfig(String name, int steps) {
        super(name);
        this.steps = steps;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    @Override
    public boolean empty() {
        return false;
    }

    @Override
    public PytorchConfig copy() {
        return this;
    }
}

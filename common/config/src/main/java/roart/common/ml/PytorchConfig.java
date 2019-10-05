package roart.common.ml;

public abstract class PytorchConfig extends NeuralNetConfig {

    protected int steps;
    
    public PytorchConfig(String name, int steps) {
        super(name);
        this.steps = steps;
    }

    public PytorchConfig(String name) {
        super(name);
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

    @Override
    public String toString() {
        return super.toString() + " " + steps;
    }
}

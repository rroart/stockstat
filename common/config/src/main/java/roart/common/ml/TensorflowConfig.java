package roart.common.ml;

public abstract class TensorflowConfig extends NeuralNetConfig {

    protected int steps;

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public TensorflowConfig(String name, int steps) {
        super(name);
        this.steps = steps;
    }
    
    public TensorflowConfig(String name) {
        super(name);
    }

    @Override
    public boolean empty() {
        return steps == 0;
    }

    @Override
    public TensorflowConfig copy() {
        return this;
    }
    
    @Override
    public String toString() {
        return super.toString() + " " + steps;
    }
}

package roart.common.ml;

public abstract class NeuralNetConfig {
    private String name;

    public NeuralNetConfig(String name) {
        super();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    public abstract void randomize();
    
    public abstract void mutate();
    
    public abstract NeuralNetConfig crossover(NeuralNetConfig other);

    public abstract NeuralNetConfig copy();

    public abstract boolean empty();
}

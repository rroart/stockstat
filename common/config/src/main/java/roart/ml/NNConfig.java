package roart.ml;

public abstract class NNConfig {
    private String name;

    public NNConfig(String name) {
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
    
    public abstract NNConfig crossover(NNConfig other);

    public abstract NNConfig copy();
}

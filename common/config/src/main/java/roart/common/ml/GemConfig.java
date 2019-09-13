package roart.common.ml;

import java.util.Random;

public abstract class GemConfig extends NeuralNetConfig {

    protected int steps;
    
    protected int layers;
    
    protected int hidden;
    
    protected double lr;
    
    public GemConfig(String name, int steps, int layers, int hidden, double lr) {
        super(name);
        this.steps = steps;
        this.layers = layers;
        this.hidden = hidden;
        this.lr = lr;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

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

    /*
    private void generateSteps(Random rand) {
        steps = 1 + rand.nextInt(MAX_STEPS);
    }
    */

    @Override
    public NeuralNetConfig copy() {
        return this;
    }
}

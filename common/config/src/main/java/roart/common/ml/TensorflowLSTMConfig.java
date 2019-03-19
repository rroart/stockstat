package roart.common.ml;

import java.util.Random;

import roart.common.config.MLConstants;

public class TensorflowLSTMConfig extends TensorflowConfig {

    private Random random = new Random();
    
    private static final int MAX_EPOCHS = 10;
    
    private static final int MAX_WINDOWSIZE = 10;
    
    private static final int MAX_HORIZON = 10;
    
    private Integer epochs;
    
    private Integer windowsize;
    
    private Integer horizon;
    
    public Integer getEpochs() {
        return epochs;
    }

    public void setEpochs(Integer epochs) {
        this.epochs = epochs;
    }

    public Integer getWindowsize() {
        return windowsize;
    }

    public void setWindowsize(Integer windowsize) {
        this.windowsize = windowsize;
    }

    public Integer getHorizon() {
        return horizon;
    }

    public void setHorizon(Integer horizon) {
        this.horizon = horizon;
    }

    public TensorflowLSTMConfig(Integer epochs, Integer windowsize, Integer horizon) {
        super(MLConstants.LSTM);
        this.epochs = epochs;
        this.windowsize = windowsize;
        this.horizon = horizon;
    }

    public TensorflowLSTMConfig() {
        super(MLConstants.LSTM);
    }

    private void generateEpochs() {
        epochs = 1 + random.nextInt(MAX_EPOCHS);
    }
    
    private void generateWindowsize() {
        windowsize = 2 + random.nextInt(MAX_WINDOWSIZE - 1);
    }
    
    private void generateHorizon() {
        horizon = 1 + random.nextInt(MAX_HORIZON);
    }

    @Override
    public void randomize() {
        generateEpochs();
        generateWindowsize();
        // generateHorizon();
    }

    @Override
    public void mutate() {
        int task = random.nextInt(2);
        switch (task) {
        case 0:
            generateEpochs();
            break;
        case 1:
            generateWindowsize();
            break;
        case 2:
            generateHorizon();
            break;
        }
    }

    @Override
    public NeuralNetConfig crossover(NeuralNetConfig otherNN) {
        TensorflowLSTMConfig offspring = new TensorflowLSTMConfig(epochs, windowsize, horizon);
        TensorflowLSTMConfig other = (TensorflowLSTMConfig) otherNN;
        if (random.nextBoolean()) {
            offspring.epochs = other.getEpochs();
        }
        if (random.nextBoolean()) {
            offspring.windowsize = other.getWindowsize();
        }
        if (random.nextBoolean()) {
            // offspring.horizon = other.getHorizon();
        }
        return offspring;
    }

    @Override
    public NeuralNetConfig copy() {
        return new TensorflowLSTMConfig(epochs, windowsize, horizon);
    }

    @Override
    public boolean empty() {
        return epochs == null || windowsize == null || horizon == null;
    }

    @Override
    public String toString() {
        return getName() + " " + getEpochs() + " " + getWindowsize() + " " + getHorizon();
    }

}

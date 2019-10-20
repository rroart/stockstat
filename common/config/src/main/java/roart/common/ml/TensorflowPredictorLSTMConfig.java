package roart.common.ml;

import java.util.Random;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import roart.common.config.MLConstants;

public class TensorflowPredictorLSTMConfig extends NeuralNetConfig {

    private static final int MAX_EPOCHS = 10;
    
    private static final int MAX_WINDOWSIZE = 10;
    
    private static final int MAX_HORIZON = 10;
    
    private Integer epochs;
    
    private Integer windowsize;
    
    private Integer horizon;
    
    public boolean full = false;
    
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

    @JsonCreator
    public TensorflowPredictorLSTMConfig(
            @JsonProperty("epochs") Integer epochs, 
            @JsonProperty("windowsize") Integer windowsize, 
            @JsonProperty("horizon") Integer horizon) {
        super(MLConstants.PREDICTORLSTM);
        this.epochs = epochs;
        this.windowsize = windowsize;
        this.horizon = horizon;
    }

    public TensorflowPredictorLSTMConfig(TensorflowPredictorLSTMConfig config) {
        this(config.epochs, config.windowsize, config.horizon);
    }

    public TensorflowPredictorLSTMConfig() {
        super(MLConstants.PREDICTORLSTM);
    }

    public TensorflowPredictorLSTMConfig(Integer epochs, Integer windowsize, Integer horizon, boolean full) {
        super(MLConstants.PREDICTORLSTM);
        this.epochs = epochs;
        this.windowsize = windowsize;
        this.horizon = horizon;
        this.full = full;
    }

    public TensorflowPredictorLSTMConfig(String name) {
        super(name);
    }

    @Override
    public NeuralNetConfig copy() {
        return new TensorflowPredictorLSTMConfig(epochs, windowsize, horizon, full);
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

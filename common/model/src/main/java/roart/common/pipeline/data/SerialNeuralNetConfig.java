package roart.common.pipeline.data;

import roart.common.ml.NeuralNetConfig;

public class SerialNeuralNetConfig extends SerialObject {
    private NeuralNetConfig neuralNetConfig;

    public SerialNeuralNetConfig() {
        super();
    }

    public SerialNeuralNetConfig(NeuralNetConfig neuralNetConfig) {
        super();
        this.neuralNetConfig = neuralNetConfig;
    }

    public NeuralNetConfig getNeuralNetConfig() {
        return neuralNetConfig;
    }

    public void setNeuralNetConfig(NeuralNetConfig neuralNetConfig) {
        this.neuralNetConfig = neuralNetConfig;
    }
    
}

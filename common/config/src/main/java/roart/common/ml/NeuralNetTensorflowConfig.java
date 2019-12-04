package roart.common.ml;

public class NeuralNetTensorflowConfig {

    private TensorflowDNNConfig tensorflowDNNConfig;
    
    private TensorflowLICConfig tensorflowLICConfig;
    
    private TensorflowLIRConfig tensorflowLIRConfig;
    
    private TensorflowMLPConfig tensorflowMLPConfig;

    private TensorflowCNNConfig tensorflowCNNConfig;

    private TensorflowCNN2Config tensorflowCNN2Config;

    private TensorflowRNNConfig tensorflowRNNConfig;

    private TensorflowGRUConfig tensorflowGRUConfig;

    private TensorflowLSTMConfig tensorflowLSTMConfig;

    //private TensorflowPredictorLSTMConfig tensorflowPredictorLSTMConfig;

    public NeuralNetTensorflowConfig() {
        super();
    }

    public TensorflowDNNConfig getTensorflowDNNConfig() {
        return tensorflowDNNConfig;
    }

    public void setTensorflowDNNConfig(TensorflowDNNConfig tensorflowDNNConfig) {
        this.tensorflowDNNConfig = tensorflowDNNConfig;
    }

    public TensorflowLICConfig getTensorflowLICConfig() {
        return tensorflowLICConfig;
    }

    public void setTensorflowLICConfig(TensorflowLICConfig tensorflowLICConfig) {
        this.tensorflowLICConfig = tensorflowLICConfig;
    }

    public TensorflowLIRConfig getTensorflowLIRConfig() {
        return tensorflowLIRConfig;
    }

    public void setTensorflowLIRConfig(TensorflowLIRConfig tensorflowLIRConfig) {
        this.tensorflowLIRConfig = tensorflowLIRConfig;
    }

    public TensorflowMLPConfig getTensorflowMLPConfig() {
        return tensorflowMLPConfig;
    }

    public void setTensorflowMLPConfig(TensorflowMLPConfig tensorflowMLPConfig) {
        this.tensorflowMLPConfig = tensorflowMLPConfig;
    }

    public TensorflowCNNConfig getTensorflowCNNConfig() {
        return tensorflowCNNConfig;
    }

    public void setTensorflowCNNConfig(TensorflowCNNConfig tensorflowCNNConfig) {
        this.tensorflowCNNConfig = tensorflowCNNConfig;
    }

    public TensorflowCNN2Config getTensorflowCNN2Config() {
        return tensorflowCNN2Config;
    }

    public void setTensorflowCNN2Config(TensorflowCNN2Config tensorflowCNN2Config) {
        this.tensorflowCNN2Config = tensorflowCNN2Config;
    }

    public TensorflowRNNConfig getTensorflowRNNConfig() {
        return tensorflowRNNConfig;
    }

    public void setTensorflowRNNConfig(TensorflowRNNConfig tensorflowRNNConfig) {
        this.tensorflowRNNConfig = tensorflowRNNConfig;
    }

    public TensorflowGRUConfig getTensorflowGRUConfig() {
        return tensorflowGRUConfig;
    }

    public void setTensorflowGRUConfig(TensorflowGRUConfig tensorflowGRUConfig) {
        this.tensorflowGRUConfig = tensorflowGRUConfig;
    }

    public TensorflowLSTMConfig getTensorflowLSTMConfig() {
        return tensorflowLSTMConfig;
    }

    public void setTensorflowLSTMConfig(TensorflowLSTMConfig tensorflowLSTMConfig) {
        this.tensorflowLSTMConfig = tensorflowLSTMConfig;
    }

    /*
    public TensorflowPredictorLSTMConfig getTensorflowPredictorLSTMConfig() {
        return tensorflowPredictorLSTMConfig;
    }

    public void setTensorflowPredictorLSTMConfig(TensorflowPredictorLSTMConfig tensorflowPredictorLSTMConfig) {
        this.tensorflowPredictorLSTMConfig = tensorflowPredictorLSTMConfig;
    }
    */

    public NeuralNetTensorflowConfig(TensorflowDNNConfig tensorflowDNNConfig, TensorflowLICConfig tensorflowLICConfig,
            TensorflowLIRConfig tensorflowLIRConfig, TensorflowMLPConfig tensorflowMLPConfig,
            TensorflowCNNConfig tensorflowCNNConfig, TensorflowCNN2Config tensorflowCNN2Config, TensorflowRNNConfig tensorflowRNNConfig,
            TensorflowGRUConfig tensorflowGRUConfig, TensorflowLSTMConfig tensorflowLSTMConfig
            /*TensorflowPredictorLSTMConfig tensorflowPredictorLSTMConfig*/) {
        super();
        this.tensorflowDNNConfig = tensorflowDNNConfig;
        this.tensorflowLICConfig = tensorflowLICConfig;
        this.tensorflowLIRConfig = tensorflowLIRConfig;
        this.tensorflowMLPConfig = tensorflowMLPConfig;
        this.tensorflowCNNConfig = tensorflowCNNConfig;
        this.tensorflowCNN2Config = tensorflowCNN2Config;
        this.tensorflowRNNConfig = tensorflowRNNConfig;
        this.tensorflowGRUConfig = tensorflowGRUConfig;
        this.tensorflowLSTMConfig = tensorflowLSTMConfig;
        //this.tensorflowPredictorLSTMConfig = tensorflowPredictorLSTMConfig;
    }
    
}

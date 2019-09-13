package roart.common.ml;

public class NeuralNetPytorchConfig {

    private PytorchMLPConfig pytorchMLPConfig;

    private PytorchCNNConfig pytorchCNNConfig;

    private PytorchRNNConfig pytorchRNNConfig;

    private PytorchGRUConfig pytorchGRUConfig;

    private PytorchLSTMConfig pytorchLSTMConfig;

    public PytorchMLPConfig getPytorchMLPConfig() {
        return pytorchMLPConfig;
    }

    public void setPytorchMLPConfig(PytorchMLPConfig pytorchMLPConfig) {
        this.pytorchMLPConfig = pytorchMLPConfig;
    }

    public PytorchCNNConfig getPytorchCNNConfig() {
        return pytorchCNNConfig;
    }

    public void setPytorchCNNConfig(PytorchCNNConfig pytorchCNNConfig) {
        this.pytorchCNNConfig = pytorchCNNConfig;
    }

    public PytorchRNNConfig getPytorchRNNConfig() {
        return pytorchRNNConfig;
    }

    public void setPytorchRNNConfig(PytorchRNNConfig pytorchRNNConfig) {
        this.pytorchRNNConfig = pytorchRNNConfig;
    }

    public PytorchGRUConfig getPytorchGRUConfig() {
        return pytorchGRUConfig;
    }

    public void setPytorchGRUConfig(PytorchGRUConfig pytorchGRUConfig) {
        this.pytorchGRUConfig = pytorchGRUConfig;
    }

    public PytorchLSTMConfig getPytorchLSTMConfig() {
        return pytorchLSTMConfig;
    }

    public void setPytorchLSTMConfig(PytorchLSTMConfig pytorchLSTMConfig) {
        this.pytorchLSTMConfig = pytorchLSTMConfig;
    }

    public NeuralNetPytorchConfig(PytorchMLPConfig pytorchMLPConfig, PytorchCNNConfig pytorchCNNConfig,
            PytorchRNNConfig pytorchRNNConfig, PytorchGRUConfig pytorchGRUConfig, PytorchLSTMConfig pytorchLSTMConfig) {
        super();
        this.pytorchMLPConfig = pytorchMLPConfig;
        this.pytorchCNNConfig = pytorchCNNConfig;
        this.pytorchRNNConfig = pytorchRNNConfig;
        this.pytorchGRUConfig = pytorchGRUConfig;
        this.pytorchLSTMConfig = pytorchLSTMConfig;
    }

}

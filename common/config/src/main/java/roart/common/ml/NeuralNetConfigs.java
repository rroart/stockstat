package roart.common.ml;

import roart.common.config.ConfigConstants;

public class NeuralNetConfigs {
    private SparkLRConfig sparkLRConfig;

    private SparkMCPConfig sparkMCPConfig;

    private SparkOVRConfig sparkOVRConfig;

    private TensorflowDNNConfig tensorflowDNNConfig;

    private TensorflowLConfig tensorflowLConfig;

    private TensorflowLSTMConfig tensorflowLSTMConfig;

    public NeuralNetConfigs(SparkLRConfig sparkLRConfig, SparkMCPConfig sparkMCPConfig, SparkOVRConfig sparkOVRConfig,
            TensorflowDNNConfig tensorflowDNNConfig, TensorflowLConfig tensorflowLConfig, TensorflowLSTMConfig tensorflowLSTMConfig) {
        super();
        this.sparkLRConfig = sparkLRConfig;
        this.sparkMCPConfig = sparkMCPConfig;
        this.sparkOVRConfig = sparkOVRConfig;
        this.tensorflowDNNConfig = tensorflowDNNConfig;
        this.tensorflowLConfig = tensorflowLConfig;
        this.tensorflowLSTMConfig = tensorflowLSTMConfig;
    }

    public NeuralNetConfigs() {
        super();
    }

    public SparkLRConfig getSparkLRConfig() {
        return sparkLRConfig;
    }

    public void setSparkLRConfig(SparkLRConfig sparkLRConfig) {
        this.sparkLRConfig = sparkLRConfig;
    }

    public SparkMCPConfig getSparkMCPConfig() {
        return sparkMCPConfig;
    }

    public void setSparkMCPConfig(SparkMCPConfig sparkMCPConfig) {
        this.sparkMCPConfig = sparkMCPConfig;
    }

    public SparkOVRConfig getSparkOVRConfig() {
        return sparkOVRConfig;
    }

    public void setSparkOVRConfig(SparkOVRConfig sparkOVRConfig) {
        this.sparkOVRConfig = sparkOVRConfig;
    }

    public TensorflowDNNConfig getTensorflowDNNConfig() {
        return tensorflowDNNConfig;
    }

    public void setTensorflowDNNConfig(TensorflowDNNConfig tensorflowDNNConfig) {
        this.tensorflowDNNConfig = tensorflowDNNConfig;
    }

    public TensorflowLConfig getTensorflowLConfig() {
        return tensorflowLConfig;
    }

    public void setTensorflowLConfig(TensorflowLConfig tensorflowLConfig) {
        this.tensorflowLConfig = tensorflowLConfig;
    }

    public TensorflowLSTMConfig getTensorflowLSTMConfig() {
        return tensorflowLSTMConfig;
    }

    public void setTensorflowLSTMConfig(TensorflowLSTMConfig tensorflowLSTMConfig) {
        this.tensorflowLSTMConfig = tensorflowLSTMConfig;
    }

    public void set(String key, NeuralNetConfig conf) {
        switch (key) {
        case ConfigConstants.MACHINELEARNINGSPARKMLLR:
            sparkLRConfig = (SparkLRConfig) conf;
            break;
        case ConfigConstants.MACHINELEARNINGSPARKMLMCP:
            sparkMCPConfig = (SparkMCPConfig) conf;
            break;
        case ConfigConstants.MACHINELEARNINGSPARKMLOVR:
            sparkOVRConfig = (SparkOVRConfig) conf;
            break;
        case ConfigConstants.MACHINELEARNINGTENSORFLOWDNN:
            tensorflowDNNConfig = (TensorflowDNNConfig) conf;
            break;
        case ConfigConstants.MACHINELEARNINGTENSORFLOWL:
            tensorflowLConfig = (TensorflowLConfig) conf;
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            tensorflowLSTMConfig = (TensorflowLSTMConfig) conf;
            break; 
        }
    }

    public NeuralNetConfig get(String key) {
        switch (key) {
        case ConfigConstants.MACHINELEARNINGSPARKMLLR:
            if (sparkLRConfig == null) {
                sparkLRConfig = new SparkLRConfig();
            }
            return sparkLRConfig;
        case ConfigConstants.MACHINELEARNINGSPARKMLMCP:
            if (sparkMCPConfig == null) {
                sparkMCPConfig = new SparkMCPConfig();
            }
            return sparkMCPConfig;
        case ConfigConstants.MACHINELEARNINGSPARKMLOVR:
            if (sparkOVRConfig == null) {
                sparkOVRConfig = new SparkOVRConfig();
            }
            return sparkOVRConfig;
        case ConfigConstants.MACHINELEARNINGTENSORFLOWDNN:
            if (tensorflowDNNConfig == null) {
                tensorflowDNNConfig = new TensorflowDNNConfig();
            }
            return tensorflowDNNConfig;
        case ConfigConstants.MACHINELEARNINGTENSORFLOWL:
            if (tensorflowLConfig == null) {
                tensorflowLConfig = new TensorflowLConfig();
            }
            return tensorflowLConfig;
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            if (tensorflowLSTMConfig == null) {
                tensorflowLSTMConfig = new TensorflowLSTMConfig();
            }
            return tensorflowLSTMConfig;
        }
        return null;
    }
}

package roart.common.ml;

import roart.common.config.ConfigConstants;

public class NeuralNetConfigs {
    private NeuralNetSparkConfig sparkConfig;
    
    private NeuralNetTensorflowConfig tensorflowConfig;
    
    private NeuralNetPytorchConfig pytorchConfig;
    
    private NeuralNetGemConfig gemConfig;
    
    public NeuralNetConfigs(NeuralNetSparkConfig sparkConfig, NeuralNetTensorflowConfig tensorflowConfig,
            NeuralNetPytorchConfig pytorchConfig, NeuralNetGemConfig gemConfig) {
        super();
        this.sparkConfig = sparkConfig;
        this.tensorflowConfig = tensorflowConfig;
        this.pytorchConfig = pytorchConfig;
        this.gemConfig = gemConfig;
    }
    
    public NeuralNetConfigs() {
        super();
    }

    public NeuralNetSparkConfig getSparkConfig() {
        return sparkConfig;
    }

    public void setSparkConfig(NeuralNetSparkConfig sparkConfig) {
        this.sparkConfig = sparkConfig;
    }

    public NeuralNetTensorflowConfig getTensorflowConfig() {
        return tensorflowConfig;
    }

    public void setTensorflowConfig(NeuralNetTensorflowConfig tensorflowConfig) {
        this.tensorflowConfig = tensorflowConfig;
    }

    public NeuralNetPytorchConfig getPytorchConfig() {
        return pytorchConfig;
    }

    public void setPytorchConfig(NeuralNetPytorchConfig pytorchConfig) {
        this.pytorchConfig = pytorchConfig;
    }

    public NeuralNetGemConfig getGemConfig() {
        return gemConfig;
    }

    public void setGemConfig(NeuralNetGemConfig gemConfig) {
        this.gemConfig = gemConfig;
    }

    public void set(String key, NeuralNetConfig conf) {
        switch (key) {
        case ConfigConstants.MACHINELEARNINGSPARKMLLOR:
            sparkConfig.setSparkLORConfig((SparkLORConfig) conf);
            break;
        case ConfigConstants.MACHINELEARNINGSPARKMLMLPC:
            sparkConfig.setSparkMLPCConfig((SparkMLPCConfig) conf);
            break;
        case ConfigConstants.MACHINELEARNINGSPARKMLOVR:
            sparkConfig.setSparkOVRConfig((SparkOVRConfig) conf);
            break;
        case ConfigConstants.MACHINELEARNINGSPARKMLLSVC:
            sparkConfig.setSparkLSVCConfig((SparkLSVCConfig) conf);
            break;
        case ConfigConstants.MACHINELEARNINGTENSORFLOWDNN:
            tensorflowConfig.setTensorflowDNNConfig((TensorflowDNNConfig) conf);
            break;
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLIC:
            tensorflowConfig.setTensorflowLICConfig((TensorflowLICConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLIR:
            tensorflowConfig.setTensorflowLIRConfig((TensorflowLIRConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN:
            tensorflowConfig.setTensorflowCNNConfig((TensorflowCNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWRNN:
            tensorflowConfig.setTensorflowRNNConfig((TensorflowRNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWGRU:
            tensorflowConfig.setTensorflowGRUConfig((TensorflowGRUConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWMLP:
            tensorflowConfig.setTensorflowMLPConfig((TensorflowMLPConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            tensorflowConfig.setTensorflowLSTMConfig((TensorflowLSTMConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTM:
            tensorflowConfig.setTensorflowPredictorLSTMConfig((TensorflowPredictorLSTMConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN:
            pytorchConfig.setPytorchCNNConfig((PytorchCNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPYTORCHRNN:
            pytorchConfig.setPytorchRNNConfig((PytorchRNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPYTORCHGRU:
            pytorchConfig.setPytorchGRUConfig((PytorchGRUConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPYTORCHMLP:
            pytorchConfig.setPytorchMLPConfig((PytorchMLPConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPYTORCHLSTM:
            pytorchConfig.setPytorchLSTMConfig((PytorchLSTMConfig) conf);
            break;
	case ConfigConstants.MACHINELEARNINGGEMEWC:
	    gemConfig.setGemEWCConfig((GemEWCConfig) conf);
	    break;
	case ConfigConstants.MACHINELEARNINGGEMGEM:
	    gemConfig.setGemGEMConfig((GemGEMConfig) conf);
	    break;
	case ConfigConstants.MACHINELEARNINGGEMICARL:
	    gemConfig.setGemIcarlConfig((GemIcarlConfig) conf);
	    break;
	case ConfigConstants.MACHINELEARNINGGEMINDEPENDENT:
	    gemConfig.setGemIConfig((GemIConfig) conf);
	    break;
	case ConfigConstants.MACHINELEARNINGGEMMULTIMODAL:
	    gemConfig.setGemMMConfig((GemMMConfig) conf);
	    break;
	case ConfigConstants.MACHINELEARNINGGEMSINGLE:
	    gemConfig.setGemSConfig((GemSConfig) conf);
	    break;
        }
    }

    public NeuralNetConfig get(String key) {
        switch (key) {
        case ConfigConstants.MACHINELEARNINGSPARKMLLOR:
            if (sparkConfig.getSparkLORConfig() == null) {
                //sparkConfig.setSparkLRConfig(new SparkLORConfig());
            }
            return sparkConfig.getSparkLORConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLMLPC:
            if (sparkConfig.getSparkMLPCConfig() == null) {
                //sparkConfig.setSparkMLPCConfig(new SparkMLPCConfig());
            }
            return sparkConfig.getSparkMLPCConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLOVR:
            if (sparkConfig.getSparkOVRConfig() == null) {
                //sparkConfig.setSparkOVRConfig(new SparkOVRConfig());
            }
            return sparkConfig.getSparkOVRConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLLSVC:
            if (sparkConfig.getSparkLSVCConfig() == null) {
                //sparkConfig.setSparkLSVCConfig(new SparkLSVCConfig());
            }
            return sparkConfig.getSparkLSVCConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWDNN:
            if (tensorflowConfig.getTensorflowDNNConfig() == null) {
                //tensorflowConfig.setTensorflowDNNConfig(new TensorflowDNNConfig());
            }
            return tensorflowConfig.getTensorflowDNNConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLIC:
            if (tensorflowConfig.getTensorflowLICConfig() == null) {
                //tensorflowConfig.setTensorflowLConfig(new TensorflowLICConfig());
            }
            return tensorflowConfig.getTensorflowLICConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLIR:
            if (tensorflowConfig.getTensorflowLIRConfig() == null) {
                //tensorflowConfig.setTensorflowLConfig(new TensorflowLICConfig());
            }
            return tensorflowConfig.getTensorflowLIRConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWMLP:
            if (tensorflowConfig.getTensorflowMLPConfig() == null) {
                //tensorflowConfig.setTensorflowMLPConfig(new TensorflowMLPConfig());
            }
            return tensorflowConfig.getTensorflowMLPConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN:
            if (tensorflowConfig.getTensorflowCNNConfig() == null) {
                //tensorflowConfig.setTensorflowCNNConfig(new TensorflowCNNConfig());
            }
            return tensorflowConfig.getTensorflowCNNConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWRNN:
            if (tensorflowConfig.getTensorflowRNNConfig() == null) {
                //tensorflowConfig.setTensorflowRNNConfig(new TensorflowRNNConfig());
            }
            return tensorflowConfig.getTensorflowRNNConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWGRU:
            if (tensorflowConfig.getTensorflowGRUConfig() == null) {
                //tensorflowConfig.setTensorflowGRUConfig(new TensorflowGRUConfig());
            }
            return tensorflowConfig.getTensorflowGRUConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            if (tensorflowConfig.getTensorflowLSTMConfig() == null) {
                //tensorflowConfig.setTensorflowLSTMConfig(new TensorflowLSTMConfig());
            }
            return tensorflowConfig.getTensorflowLSTMConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTM:
            if (tensorflowConfig.getTensorflowLSTMConfig() == null) {
                //tensorflowConfig.setTensorflowLSTMConfig(new TensorflowPredictorLSTMConfig());
            }
            return tensorflowConfig.getTensorflowLSTMConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHMLP:
            if (pytorchConfig.getPytorchMLPConfig() == null) {
                //pytorchConfig.setPytorchMLPConfig(new PytorchMLPConfig());
            }
            return pytorchConfig.getPytorchMLPConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN:
            if (pytorchConfig.getPytorchCNNConfig() == null) {
                //pytorchConfig.setPytorchCNNConfig(new PytorchCNNConfig());
            }
            return pytorchConfig.getPytorchCNNConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHRNN:
            if (pytorchConfig.getPytorchRNNConfig() == null) {
                //pytorchConfig.setPytorchRNNConfig(new PytorchRNNConfig());
            }
            return pytorchConfig.getPytorchRNNConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHGRU:
            if (pytorchConfig.getPytorchGRUConfig() == null) {
                //pytorchConfig.setPytorchGRUConfig(new PytorchGRUConfig());
            }
            return pytorchConfig.getPytorchGRUConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHLSTM:
            if (pytorchConfig.getPytorchLSTMConfig() == null) {
                //pytorchConfig.setPytorchLSTMConfig(new PytorchLSTMConfig());
            }
            return pytorchConfig.getPytorchLSTMConfig();
        case ConfigConstants.MACHINELEARNINGGEMEWC:
            if (gemConfig.getGemEWCConfig() == null) {
                //gemConfig.setGemEWCConfig(new GemEWCConfig());
            }
            return gemConfig.getGemEWCConfig();
        case ConfigConstants.MACHINELEARNINGGEMGEM:
            if (gemConfig.getGemGEMConfig() == null) {
                //gemConfig.setGemGEMConfig(new GemGEMConfig());
            }
            return gemConfig.getGemGEMConfig();
        case ConfigConstants.MACHINELEARNINGGEMICARL:
            if (gemConfig.getGemIcarlConfig() == null) {
                //gemConfig.setGemIcarlConfig(new GemIcarlConfig());
            }
            return gemConfig.getGemIcarlConfig();
        case ConfigConstants.MACHINELEARNINGGEMINDEPENDENT:
            if (gemConfig.getGemIConfig() == null) {
                //gemConfig.setGemIConfig(new GemIConfig());
            }
            return gemConfig.getGemIConfig();
        case ConfigConstants.MACHINELEARNINGGEMMULTIMODAL:
            if (gemConfig.getGemMMConfig() == null) {
                //gemConfig.setGemMMConfig(new GemMMConfig());
            }
            return gemConfig.getGemMMConfig();
        case ConfigConstants.MACHINELEARNINGGEMSINGLE:
            if (gemConfig.getGemSConfig() == null) {
                //gemConfig.setGemSConfig(new GemSConfig());
            }
            return gemConfig.getGemSConfig();
        }
        return null;
    }

}

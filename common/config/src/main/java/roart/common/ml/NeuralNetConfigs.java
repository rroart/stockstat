package roart.common.ml;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.util.JsonUtil;

public class NeuralNetConfigs {
    private NeuralNetSparkConfig sparkConfig = new NeuralNetSparkConfig();
    
    private NeuralNetTensorflowConfig tensorflowConfig = new NeuralNetTensorflowConfig();
    
    private NeuralNetPytorchConfig pytorchConfig = new NeuralNetPytorchConfig();
    
    private NeuralNetGemConfig gemConfig = new NeuralNetGemConfig();
    
    private static final ObjectMapper mapper = JsonMapper.builder().configure(MapperFeature.USE_BASE_TYPE_AS_DEFAULT_IMPL, true).build();
    
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
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR:
            tensorflowConfig.setTensorflowLIRConfig((TensorflowLIRConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN:
            tensorflowConfig.setTensorflowCNNConfig((TensorflowCNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2:
            tensorflowConfig.setTensorflowCNN2Config((TensorflowCNN2Config) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWRNN:
            tensorflowConfig.setTensorflowRNNConfig((TensorflowRNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWGRU:
            tensorflowConfig.setTensorflowGRUConfig((TensorflowGRUConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWMLP:
            tensorflowConfig.setTensorflowMLPConfig((TensorflowMLPConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            tensorflowConfig.setTensorflowLSTMConfig((TensorflowLSTMConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN:
            pytorchConfig.setPytorchCNNConfig((PytorchCNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN2:
            pytorchConfig.setPytorchCNN2Config((PytorchCNN2Config) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN:
        case ConfigConstants.MACHINELEARNINGPYTORCHRNN:
            pytorchConfig.setPytorchRNNConfig((PytorchRNNConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU:
        case ConfigConstants.MACHINELEARNINGPYTORCHGRU:
            pytorchConfig.setPytorchGRUConfig((PytorchGRUConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP:
        case ConfigConstants.MACHINELEARNINGPYTORCHMLP:
            pytorchConfig.setPytorchMLPConfig((PytorchMLPConfig) conf);
            break; 
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM:
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

    public NeuralNetConfig getAndSet(String key) {
        switch (key) {
        case ConfigConstants.MACHINELEARNINGSPARKMLLOR:
            if (sparkConfig.getSparkLORConfig() == null) {
                sparkConfig.setSparkLORConfig((SparkLORConfig) get(key));
            }
            return sparkConfig.getSparkLORConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLMLPC:
            if (sparkConfig.getSparkMLPCConfig() == null) {
                sparkConfig.setSparkMLPCConfig((SparkMLPCConfig) get(key));
            }
            return sparkConfig.getSparkMLPCConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLOVR:
            if (sparkConfig.getSparkOVRConfig() == null) {
                sparkConfig.setSparkOVRConfig((SparkOVRConfig) get(key));
            }
            return sparkConfig.getSparkOVRConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLLSVC:
            if (sparkConfig.getSparkLSVCConfig() == null) {
                sparkConfig.setSparkLSVCConfig((SparkLSVCConfig) get(key));
            }
            return sparkConfig.getSparkLSVCConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWDNN:
            if (tensorflowConfig.getTensorflowDNNConfig() == null) {
                tensorflowConfig.setTensorflowDNNConfig((TensorflowDNNConfig) get(key));
            }
            return tensorflowConfig.getTensorflowDNNConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLIC:
            if (tensorflowConfig.getTensorflowLICConfig() == null) {
                tensorflowConfig.setTensorflowLICConfig((TensorflowLICConfig) get(key));
            }
            return tensorflowConfig.getTensorflowLICConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR:
            if (tensorflowConfig.getTensorflowLIRConfig() == null) {
                tensorflowConfig.setTensorflowLIRConfig((TensorflowLIRConfig) get(key));
            }
            return tensorflowConfig.getTensorflowLIRConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWMLP:
            if (tensorflowConfig.getTensorflowMLPConfig() == null) {
                tensorflowConfig.setTensorflowMLPConfig((TensorflowMLPConfig) get(key));
            }
            return tensorflowConfig.getTensorflowMLPConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN:
            if (tensorflowConfig.getTensorflowCNNConfig() == null) {
                tensorflowConfig.setTensorflowCNNConfig((TensorflowCNNConfig) get(key));
            }
            return tensorflowConfig.getTensorflowCNNConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2:
            if (tensorflowConfig.getTensorflowCNN2Config() == null) {
                tensorflowConfig.setTensorflowCNN2Config((TensorflowCNN2Config) get(key));
            }
            return tensorflowConfig.getTensorflowCNN2Config();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWRNN:
            if (tensorflowConfig.getTensorflowRNNConfig() == null) {
                tensorflowConfig.setTensorflowRNNConfig((TensorflowRNNConfig) get(key));
            }
            return tensorflowConfig.getTensorflowRNNConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWGRU:
            if (tensorflowConfig.getTensorflowGRUConfig() == null) {
                tensorflowConfig.setTensorflowGRUConfig((TensorflowGRUConfig) get(key));
            }
            return tensorflowConfig.getTensorflowGRUConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            if (tensorflowConfig.getTensorflowLSTMConfig() == null) {
                tensorflowConfig.setTensorflowLSTMConfig((TensorflowLSTMConfig) get(key));
            }
            return tensorflowConfig.getTensorflowLSTMConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP:
        case ConfigConstants.MACHINELEARNINGPYTORCHMLP:
            if (pytorchConfig.getPytorchMLPConfig() == null) {
                pytorchConfig.setPytorchMLPConfig((PytorchMLPConfig) get(key));
            }
            return pytorchConfig.getPytorchMLPConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN:
            if (pytorchConfig.getPytorchCNNConfig() == null) {
                pytorchConfig.setPytorchCNNConfig((PytorchCNNConfig) get(key));
            }
            return pytorchConfig.getPytorchCNNConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN2:
            if (pytorchConfig.getPytorchCNN2Config() == null) {
                pytorchConfig.setPytorchCNN2Config((PytorchCNN2Config) get(key));
            }
            return pytorchConfig.getPytorchCNN2Config();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN:
        case ConfigConstants.MACHINELEARNINGPYTORCHRNN:
            if (pytorchConfig.getPytorchRNNConfig() == null) {
                pytorchConfig.setPytorchRNNConfig((PytorchRNNConfig) get(key));
            }
            return pytorchConfig.getPytorchRNNConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU:
        case ConfigConstants.MACHINELEARNINGPYTORCHGRU:
            if (pytorchConfig.getPytorchGRUConfig() == null) {
                pytorchConfig.setPytorchGRUConfig((PytorchGRUConfig) get(key));
            }
            return pytorchConfig.getPytorchGRUConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM:
        case ConfigConstants.MACHINELEARNINGPYTORCHLSTM:
            if (pytorchConfig.getPytorchLSTMConfig() == null) {
                pytorchConfig.setPytorchLSTMConfig((PytorchLSTMConfig) get(key));
            }
            return pytorchConfig.getPytorchLSTMConfig();
        case ConfigConstants.MACHINELEARNINGGEMEWC:
            if (gemConfig.getGemEWCConfig() == null) {
                gemConfig.setGemEWCConfig((GemEWCConfig) get(key));
            }
            return gemConfig.getGemEWCConfig();
        case ConfigConstants.MACHINELEARNINGGEMGEM:
            if (gemConfig.getGemGEMConfig() == null) {
                gemConfig.setGemGEMConfig((GemGEMConfig) get(key));
            }
            return gemConfig.getGemGEMConfig();
        case ConfigConstants.MACHINELEARNINGGEMICARL:
            if (gemConfig.getGemIcarlConfig() == null) {
                gemConfig.setGemIcarlConfig((GemIcarlConfig) get(key));
            }
            return gemConfig.getGemIcarlConfig();
        case ConfigConstants.MACHINELEARNINGGEMINDEPENDENT:
            if (gemConfig.getGemIConfig() == null) {
                gemConfig.setGemIConfig((GemIConfig) get(key));
            }
            return gemConfig.getGemIConfig();
        case ConfigConstants.MACHINELEARNINGGEMMULTIMODAL:
            if (gemConfig.getGemMMConfig() == null) {
                gemConfig.setGemMMConfig((GemMMConfig) get(key));
            }
            return gemConfig.getGemMMConfig();
        case ConfigConstants.MACHINELEARNINGGEMSINGLE:
            if (gemConfig.getGemSConfig() == null) {
                gemConfig.setGemSConfig((GemSConfig) get(key));
            }
            return gemConfig.getGemSConfig();
        }
        return null;
    }

    public NeuralNetConfig getAndSetConfig(String key, String config) {
        switch (key) {
        case ConfigConstants.MACHINELEARNINGSPARKMLLOR:
            if (sparkConfig.getSparkLORConfig() == null) {
                sparkConfig.setSparkLORConfig((SparkLORConfig) get(key, config));
            }
            return sparkConfig.getSparkLORConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLMLPC:
            if (sparkConfig.getSparkMLPCConfig() == null) {
                sparkConfig.setSparkMLPCConfig((SparkMLPCConfig) get(key, config));
            }
            return sparkConfig.getSparkMLPCConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLOVR:
            if (sparkConfig.getSparkOVRConfig() == null) {
                sparkConfig.setSparkOVRConfig((SparkOVRConfig) get(key, config));
            }
            return sparkConfig.getSparkOVRConfig();
        case ConfigConstants.MACHINELEARNINGSPARKMLLSVC:
            if (sparkConfig.getSparkLSVCConfig() == null) {
                sparkConfig.setSparkLSVCConfig((SparkLSVCConfig) get(key, config));
            }
            return sparkConfig.getSparkLSVCConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWDNN:
            if (tensorflowConfig.getTensorflowDNNConfig() == null) {
                tensorflowConfig.setTensorflowDNNConfig((TensorflowDNNConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowDNNConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLIC:
            if (tensorflowConfig.getTensorflowLICConfig() == null) {
                tensorflowConfig.setTensorflowLICConfig((TensorflowLICConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowLICConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR:
            if (tensorflowConfig.getTensorflowLIRConfig() == null) {
                tensorflowConfig.setTensorflowLIRConfig((TensorflowLIRConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowLIRConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWMLP:
            if (tensorflowConfig.getTensorflowMLPConfig() == null) {
                tensorflowConfig.setTensorflowMLPConfig((TensorflowMLPConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowMLPConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN:
            if (tensorflowConfig.getTensorflowCNNConfig() == null) {
                tensorflowConfig.setTensorflowCNNConfig((TensorflowCNNConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowCNNConfig();
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2:
            if (tensorflowConfig.getTensorflowCNN2Config() == null) {
                tensorflowConfig.setTensorflowCNN2Config((TensorflowCNN2Config) get(key, config));
            }
            return tensorflowConfig.getTensorflowCNN2Config();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWRNN:
            if (tensorflowConfig.getTensorflowRNNConfig() == null) {
                tensorflowConfig.setTensorflowRNNConfig((TensorflowRNNConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowRNNConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWGRU:
            if (tensorflowConfig.getTensorflowGRUConfig() == null) {
                tensorflowConfig.setTensorflowGRUConfig((TensorflowGRUConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowGRUConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM:
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            if (tensorflowConfig.getTensorflowLSTMConfig() == null) {
                tensorflowConfig.setTensorflowLSTMConfig((TensorflowLSTMConfig) get(key, config));
            }
            return tensorflowConfig.getTensorflowLSTMConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP:
        case ConfigConstants.MACHINELEARNINGPYTORCHMLP:
            if (pytorchConfig.getPytorchMLPConfig() == null) {
                pytorchConfig.setPytorchMLPConfig((PytorchMLPConfig) get(key, config));
            }
            return pytorchConfig.getPytorchMLPConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN:
            if (pytorchConfig.getPytorchCNNConfig() == null) {
                pytorchConfig.setPytorchCNNConfig((PytorchCNNConfig) get(key, config));
            }
            return pytorchConfig.getPytorchCNNConfig();
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN2:
            if (pytorchConfig.getPytorchCNN2Config() == null) {
                pytorchConfig.setPytorchCNN2Config((PytorchCNN2Config) get(key, config));
            }
            return pytorchConfig.getPytorchCNN2Config();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN:
        case ConfigConstants.MACHINELEARNINGPYTORCHRNN:
            if (pytorchConfig.getPytorchRNNConfig() == null) {
                pytorchConfig.setPytorchRNNConfig((PytorchRNNConfig) get(key, config));
            }
            return pytorchConfig.getPytorchRNNConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU:
        case ConfigConstants.MACHINELEARNINGPYTORCHGRU:
            if (pytorchConfig.getPytorchGRUConfig() == null) {
                pytorchConfig.setPytorchGRUConfig((PytorchGRUConfig) get(key, config));
            }
            return pytorchConfig.getPytorchGRUConfig();
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM:
        case ConfigConstants.MACHINELEARNINGPYTORCHLSTM:
            if (pytorchConfig.getPytorchLSTMConfig() == null) {
                pytorchConfig.setPytorchLSTMConfig((PytorchLSTMConfig) get(key, config));
            }
            return pytorchConfig.getPytorchLSTMConfig();
        case ConfigConstants.MACHINELEARNINGGEMEWC:
            if (gemConfig.getGemEWCConfig() == null) {
                gemConfig.setGemEWCConfig((GemEWCConfig) get(key, config));
            }
            return gemConfig.getGemEWCConfig();
        case ConfigConstants.MACHINELEARNINGGEMGEM:
            if (gemConfig.getGemGEMConfig() == null) {
                gemConfig.setGemGEMConfig((GemGEMConfig) get(key, config));
            }
            return gemConfig.getGemGEMConfig();
        case ConfigConstants.MACHINELEARNINGGEMICARL:
            if (gemConfig.getGemIcarlConfig() == null) {
                gemConfig.setGemIcarlConfig((GemIcarlConfig) get(key, config));
            }
            return gemConfig.getGemIcarlConfig();
        case ConfigConstants.MACHINELEARNINGGEMINDEPENDENT:
            if (gemConfig.getGemIConfig() == null) {
                gemConfig.setGemIConfig((GemIConfig) get(key, config));
            }
            return gemConfig.getGemIConfig();
        case ConfigConstants.MACHINELEARNINGGEMMULTIMODAL:
            if (gemConfig.getGemMMConfig() == null) {
                gemConfig.setGemMMConfig((GemMMConfig) get(key, config));
            }
            return gemConfig.getGemMMConfig();
        case ConfigConstants.MACHINELEARNINGGEMSINGLE:
            if (gemConfig.getGemSConfig() == null) {
                gemConfig.setGemSConfig((GemSConfig) get(key, config));
            }
            return gemConfig.getGemSConfig();
        }
        return null;
    }

    private Map<String, Pair<Class<NeuralNetConfig>, String>> getMap() {
        Map<String, Pair<Class<NeuralNetConfig>, String>> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLMLPC, new ImmutablePair(SparkMLPCConfig.class, MLConstants.SPARKMLPCCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLOR, new ImmutablePair(SparkLORConfig.class, MLConstants.SPARKLORCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLOVR, new ImmutablePair(SparkOVRConfig.class, MLConstants.SPARKOVRCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLSVC, new ImmutablePair(SparkLSVCConfig.class, MLConstants.SPARKLSVCCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, new ImmutablePair(TensorflowDNNConfig.class, MLConstants.TENSORFLOWDNNCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC, new ImmutablePair(TensorflowLICConfig.class, MLConstants.TENSORFLOWLICCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP, new ImmutablePair(TensorflowMLPConfig.class, MLConstants.TENSORFLOWMLPCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN, new ImmutablePair(TensorflowCNNConfig.class, MLConstants.TENSORFLOWCNNCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2, new ImmutablePair(TensorflowCNN2Config.class, MLConstants.TENSORFLOWCNN2CONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN, new ImmutablePair(TensorflowRNNConfig.class, MLConstants.TENSORFLOWRNNCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU, new ImmutablePair(TensorflowGRUConfig.class, MLConstants.TENSORFLOWGRUCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM, new ImmutablePair(TensorflowLSTMConfig.class, MLConstants.TENSORFLOWLSTMCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHMLP, new ImmutablePair(PytorchMLPConfig.class, MLConstants.PYTORCHMLPCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN, new ImmutablePair(PytorchCNNConfig.class, MLConstants.PYTORCHCNNCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN2, new ImmutablePair(PytorchCNN2Config.class, MLConstants.PYTORCHCNN2CONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHRNN, new ImmutablePair(PytorchRNNConfig.class, MLConstants.PYTORCHRNNCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHGRU, new ImmutablePair(PytorchGRUConfig.class, MLConstants.PYTORCHGRUCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHLSTM, new ImmutablePair(PytorchLSTMConfig.class, MLConstants.PYTORCHLSTMCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGGEMEWC, new ImmutablePair(GemEWCConfig.class, MLConstants.GEMEWCCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGGEMGEM, new ImmutablePair(GemGEMConfig.class, MLConstants.GEMGEMCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGGEMICARL, new ImmutablePair(GemIcarlConfig.class, MLConstants.GEMICARLCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGGEMINDEPENDENT, new ImmutablePair(GemIConfig.class, MLConstants.GEMINDEPENDENTCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGGEMMULTIMODAL, new ImmutablePair(GemMMConfig.class, MLConstants.GEMMULTIMODALCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGGEMSINGLE, new ImmutablePair(GemSConfig.class, MLConstants.GEMSINGLECONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR, new ImmutablePair(TensorflowLIRConfig.class, MLConstants.TENSORFLOWLIRCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP, new ImmutablePair(TensorflowMLPConfig.class, MLConstants.TENSORFLOWMLPCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN, new ImmutablePair(TensorflowRNNConfig.class, MLConstants.TENSORFLOWRNNCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU, new ImmutablePair(TensorflowGRUConfig.class, MLConstants.TENSORFLOWGRUCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, new ImmutablePair(TensorflowLSTMConfig.class, MLConstants.TENSORFLOWLSTMCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP, new ImmutablePair(PytorchMLPConfig.class, MLConstants.PYTORCHMLPCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN, new ImmutablePair(PytorchRNNConfig.class, MLConstants.PYTORCHRNNCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU, new ImmutablePair(PytorchGRUConfig.class, MLConstants.PYTORCHGRUCONFIG));
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM, new ImmutablePair(PytorchLSTMConfig.class, MLConstants.PYTORCHLSTMCONFIG));
        return map;
    }
    
    public Map<String, String> getAnotherConfigMap() {
        Map<String, String> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLMLPC, ConfigConstants.MACHINELEARNINGSPARKML);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLOR, ConfigConstants.MACHINELEARNINGSPARKML);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLOVR, ConfigConstants.MACHINELEARNINGSPARKML);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLSVC, ConfigConstants.MACHINELEARNINGSPARKML);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        //map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, ConfigConstants.MACHINELEARNINGTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHMLP, ConfigConstants.MACHINELEARNINGPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN, ConfigConstants.MACHINELEARNINGPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN2, ConfigConstants.MACHINELEARNINGPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHRNN, ConfigConstants.MACHINELEARNINGPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHGRU, ConfigConstants.MACHINELEARNINGPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHLSTM, ConfigConstants.MACHINELEARNINGPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGGEMEWC, ConfigConstants.MACHINELEARNINGGEM);
        map.put(ConfigConstants.MACHINELEARNINGGEMGEM, ConfigConstants.MACHINELEARNINGGEM);
        map.put(ConfigConstants.MACHINELEARNINGGEMICARL, ConfigConstants.MACHINELEARNINGGEM);
        map.put(ConfigConstants.MACHINELEARNINGGEMINDEPENDENT, ConfigConstants.MACHINELEARNINGGEM);
        map.put(ConfigConstants.MACHINELEARNINGGEMMULTIMODAL, ConfigConstants.MACHINELEARNINGGEM);
        map.put(ConfigConstants.MACHINELEARNINGGEMSINGLE, ConfigConstants.MACHINELEARNINGGEM);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOW);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCH);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCH);
        return map;
    }

    public Map<String, String> getConfigMap() {
        Map<String, String> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLMLPC, ConfigConstants.MACHINELEARNINGSPARKMLMLPCCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLOR, ConfigConstants.MACHINELEARNINGSPARKMLLORCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLOVR, ConfigConstants.MACHINELEARNINGSPARKMLOVRCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLSVC, ConfigConstants.MACHINELEARNINGSPARKMLLSVCCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, ConfigConstants.MACHINELEARNINGTENSORFLOWDNNCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC, ConfigConstants.MACHINELEARNINGTENSORFLOWLICCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP, ConfigConstants.MACHINELEARNINGTENSORFLOWMLPCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN, ConfigConstants.MACHINELEARNINGTENSORFLOWCNNCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2, ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2CONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN, ConfigConstants.MACHINELEARNINGTENSORFLOWRNNCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU, ConfigConstants.MACHINELEARNINGTENSORFLOWGRUCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM, ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG);
        //map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHMLP, ConfigConstants.MACHINELEARNINGPYTORCHMLPCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN, ConfigConstants.MACHINELEARNINGPYTORCHCNNCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN2, ConfigConstants.MACHINELEARNINGPYTORCHCNN2CONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHRNN, ConfigConstants.MACHINELEARNINGPYTORCHRNNCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHGRU, ConfigConstants.MACHINELEARNINGPYTORCHGRUCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHLSTM, ConfigConstants.MACHINELEARNINGPYTORCHLSTMCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGGEMEWC, ConfigConstants.MACHINELEARNINGGEMEWCCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGGEMGEM, ConfigConstants.MACHINELEARNINGGEMGEMCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGGEMICARL, ConfigConstants.MACHINELEARNINGGEMICARLCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGGEMINDEPENDENT, ConfigConstants.MACHINELEARNINGGEMINDEPENDENTCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGGEMMULTIMODAL, ConfigConstants.MACHINELEARNINGGEMMULTIMODALCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGGEMSINGLE, ConfigConstants.MACHINELEARNINGGEMSINGLECONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIRCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLPCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNNCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRUCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLPCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNNCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRUCONFIG);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM, ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTMCONFIG);
        return map;
    }

    public Map<String, String> getConfigMapRev() {
        Map<String, String> aMap = getConfigMap();
        Map<String, String> retMap = new HashMap<>();
        for (Entry<String, String> entry : aMap.entrySet()) {
            retMap.put(entry.getValue(), entry.getKey());
        }
        return retMap;
    }

    public NeuralNetConfig get(String key) {
        NeuralNetConfig nnconfig = null;
        Map<String, Pair<Class<NeuralNetConfig>, String>> map = getMap();
        Pair<Class<NeuralNetConfig>, String> nnstring = map.get(key);
        nnconfig = JsonUtil.convert(nnstring.getRight(), nnstring.getLeft(), mapper);
        return nnconfig;
    }

    public NeuralNetConfig get(String key, String config) {
        NeuralNetConfig nnconfig = null;
        Map<String, Pair<Class<NeuralNetConfig>, String>> map = getMap();
        Pair<Class<NeuralNetConfig>, String> nnstring = map.get(key);
        nnconfig = JsonUtil.convert(config, nnstring.getLeft(), mapper);
        return nnconfig;
    }

    public NeuralNetConfig getClass(String aclass) {
        NeuralNetConfig nnconfig = null;
        Map<String, Pair<Class<NeuralNetConfig>, String>> map = getMap();
        Collection<Pair<Class<NeuralNetConfig>, String>> values = map.values();
        for (Pair<Class<NeuralNetConfig>, String> nnstring : values) {
            if (aclass.equals(nnstring.getLeft().getSimpleName())) {
                nnconfig = JsonUtil.convert(nnstring.getRight(), nnstring.getLeft(), mapper);
                return nnconfig;                
            }
        }
        return null;
    }

    public Pair<String, String> getSubcomponent(String aclass) {
        NeuralNetConfig nnconfig = null;
        Map<String, Pair<Class<NeuralNetConfig>, String>> map = getMap();
        for (Entry<String, Pair<Class<NeuralNetConfig>, String>> entry : map.entrySet()) {
            Pair<Class<NeuralNetConfig>, String> nnstring = entry.getValue();
            if (aclass.equals(nnstring.getLeft().getSimpleName())) {
                String key = entry.getKey();
                Map<Pair<String, String>, String> mlmap = new MLMapsML().getMap();
                for (Entry<Pair<String, String>, String> mlentry : mlmap.entrySet()) {
                    if (key.equals(mlentry.getValue())) {
                        return mlentry.getKey();
                    }
                }
            }
        }
        return null;
    }

}

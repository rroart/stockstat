package roart.gene;

import roart.common.config.ConfigConstants;
import roart.common.ml.GemEWCConfig;
import roart.common.ml.GemGEMConfig;
import roart.common.ml.GemIConfig;
import roart.common.ml.GemIcarlConfig;
import roart.common.ml.GemMMConfig;
import roart.common.ml.GemSConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchCNN2Config;
import roart.common.ml.PytorchCNNConfig;
import roart.common.ml.PytorchGRUConfig;
import roart.common.ml.PytorchLSTMConfig;
import roart.common.ml.PytorchMLPConfig;
import roart.common.ml.PytorchRNNConfig;
import roart.common.ml.SparkLORConfig;
import roart.common.ml.SparkLSVCConfig;
import roart.common.ml.SparkMLPCConfig;
import roart.common.ml.SparkOVRConfig;
import roart.common.ml.TensorflowCNN2Config;
import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowGRUConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.common.ml.TensorflowLIRConfig;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.ml.TensorflowMLPConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.ml.TensorflowRNNConfig;
import roart.gene.ml.impl.GemEWCConfigGene;
import roart.gene.ml.impl.GemGEMConfigGene;
import roart.gene.ml.impl.GemIConfigGene;
import roart.gene.ml.impl.GemIcarlConfigGene;
import roart.gene.ml.impl.GemMMConfigGene;
import roart.gene.ml.impl.GemSConfigGene;
import roart.gene.ml.impl.PytorchCNN2ConfigGene;
import roart.gene.ml.impl.PytorchCNNConfigGene;
import roart.gene.ml.impl.PytorchGRUConfigGene;
import roart.gene.ml.impl.PytorchLSTMConfigGene;
import roart.gene.ml.impl.PytorchMLPConfigGene;
import roart.gene.ml.impl.PytorchRNNConfigGene;
import roart.gene.ml.impl.SparkLORConfigGene;
import roart.gene.ml.impl.SparkLSVCConfigGene;
import roart.gene.ml.impl.SparkMLPCConfigGene;
import roart.gene.ml.impl.SparkOVRConfigGene;
import roart.gene.ml.impl.TensorflowCNN2ConfigGene;
import roart.gene.ml.impl.TensorflowCNNConfigGene;
import roart.gene.ml.impl.TensorflowDNNConfigGene;
import roart.gene.ml.impl.TensorflowGRUConfigGene;
import roart.gene.ml.impl.TensorflowLICConfigGene;
import roart.gene.ml.impl.TensorflowLIRConfigGene;
import roart.gene.ml.impl.TensorflowLSTMConfigGene;
import roart.gene.ml.impl.TensorflowMLPConfigGene;
import roart.gene.ml.impl.TensorflowPredictorLSTMConfigGene;
import roart.gene.ml.impl.TensorflowRNNConfigGene;

public class NeuralNetConfigGeneFactory {
    protected boolean predictor;

    public NeuralNetConfigGeneFactory(boolean predictor) {
        super();
        this.predictor = predictor;
    }

    public NeuralNetConfigGene get(NeuralNetConfig config, String key) {
        switch (key) {
        case ConfigConstants.MACHINELEARNINGSPARKMLLOR:
            return get((SparkLORConfig) config);
        case ConfigConstants.MACHINELEARNINGSPARKMLMLPC:
            return get((SparkMLPCConfig) config);
        case ConfigConstants.MACHINELEARNINGSPARKMLOVR:
            return get((SparkOVRConfig) config);
        case ConfigConstants.MACHINELEARNINGSPARKMLLSVC:
            return get((SparkLSVCConfig) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWDNN:
            return get((TensorflowDNNConfig) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLIC:
            return get((TensorflowLICConfig) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWMLP:
            return get((TensorflowMLPConfig) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN:
            return get((TensorflowCNNConfig) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2:
            return get((TensorflowCNN2Config) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWRNN:
            return get((TensorflowRNNConfig) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWGRU:
            return get((TensorflowGRUConfig) config);
        case ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM:
            return get((TensorflowLSTMConfig) config);
        case ConfigConstants.MACHINELEARNINGPYTORCHMLP:
            return get((PytorchMLPConfig) config);
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN:
            return get((PytorchCNNConfig) config);
        case ConfigConstants.MACHINELEARNINGPYTORCHCNN2:
            return get((PytorchCNN2Config) config);
        case ConfigConstants.MACHINELEARNINGPYTORCHRNN:
            return get((PytorchRNNConfig) config);
        case ConfigConstants.MACHINELEARNINGPYTORCHGRU:
            return get((PytorchGRUConfig) config);
        case ConfigConstants.MACHINELEARNINGPYTORCHLSTM:
            return get((PytorchLSTMConfig) config);
	case ConfigConstants.MACHINELEARNINGGEMEWC:
	    return get((GemEWCConfig) config);
	case ConfigConstants.MACHINELEARNINGGEMGEM:
	    return get((GemGEMConfig) config);
	case ConfigConstants.MACHINELEARNINGGEMINDEPENDENT:
	    return get((GemIConfig) config);
	case ConfigConstants.MACHINELEARNINGGEMICARL:
	    return get((GemIcarlConfig) config);
	case ConfigConstants.MACHINELEARNINGGEMMULTIMODAL:
	    return get((GemMMConfig) config);
	case ConfigConstants.MACHINELEARNINGGEMSINGLE:
	    return get((GemSConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR:
            return get((TensorflowLIRConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP:
            return get((TensorflowMLPConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN:
            return get((TensorflowRNNConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU:
            return get((TensorflowGRUConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM:
            return get((TensorflowLSTMConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP:
            return get((PytorchMLPConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN:
            return get((PytorchRNNConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU:
            return get((PytorchGRUConfig) config);
        case ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM:
            return get((PytorchLSTMConfig) config);
        }
        return null;
    }
    
    public NeuralNetConfigGene get(SparkMLPCConfig config) {
        return new SparkMLPCConfigGene(config);
    }
    
    public NeuralNetConfigGene get(SparkLORConfig config) {
        return new SparkLORConfigGene(config);
    }
    
    public NeuralNetConfigGene get(SparkOVRConfig config) {
        return new SparkOVRConfigGene(config);
    }
    
    public NeuralNetConfigGene get(SparkLSVCConfig config) {
        return new SparkLSVCConfigGene(config);
    }
    
    public NeuralNetConfigGene get(TensorflowDNNConfig config) {
        return new TensorflowDNNConfigGene(config);
    }
    
    public NeuralNetConfigGene get(TensorflowLICConfig config) {
        return new TensorflowLICConfigGene(config);
    }
    
    public NeuralNetConfigGene get(TensorflowLIRConfig config) {
        return new TensorflowLIRConfigGene(config, predictor);
    }
     
    public NeuralNetConfigGene get(TensorflowMLPConfig config) {
        return new TensorflowMLPConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(TensorflowCNNConfig config) {
        return new TensorflowCNNConfigGene(config);
    }
    
    public NeuralNetConfigGene get(TensorflowCNN2Config config) {
        return new TensorflowCNN2ConfigGene(config);
    }
    
    public NeuralNetConfigGene get(TensorflowRNNConfig config) {
        return new TensorflowRNNConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(TensorflowGRUConfig config) {
        return new TensorflowGRUConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(TensorflowLSTMConfig config) {
        return new TensorflowLSTMConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(TensorflowPredictorLSTMConfig config) {
        return new TensorflowPredictorLSTMConfigGene(config);
    }
    
    public NeuralNetConfigGene get(PytorchMLPConfig config) {
        return new PytorchMLPConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(PytorchCNNConfig config) {
        return new PytorchCNNConfigGene(config);
    }
    
    public NeuralNetConfigGene get(PytorchCNN2Config config) {
        return new PytorchCNN2ConfigGene(config);
    }
    
    public NeuralNetConfigGene get(PytorchRNNConfig config) {
        return new PytorchRNNConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(PytorchGRUConfig config) {
        return new PytorchGRUConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(PytorchLSTMConfig config) {
        return new PytorchLSTMConfigGene(config, predictor);
    }
    
    public NeuralNetConfigGene get(GemEWCConfig config) {
        return new GemEWCConfigGene(config);
    }
    
    public NeuralNetConfigGene get(GemGEMConfig config) {
        return new GemGEMConfigGene(config);
    }
    
    public NeuralNetConfigGene get(GemIConfig config) {
        return new GemIConfigGene(config);
    }
    
    public NeuralNetConfigGene get(GemIcarlConfig config) {
        return new GemIcarlConfigGene(config);
    }
    
    public NeuralNetConfigGene get(GemMMConfig config) {
        return new GemMMConfigGene(config);
    }
    
    public NeuralNetConfigGene get(GemSConfig config) {
        return new GemSConfigGene(config);
    }
}

package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowRNNConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowRNNModel  extends MLClassifyTensorflowRecurrentModel {
    public MLClassifyTensorflowRNNModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 4;
    }
    
    @Override
    public String getName() {
        return MLConstants.RNN;
    }
    
    @Override
    public String getKey(boolean binary) {
        if (binary) {
            return ConfigConstants.MACHINELEARNINGTENSORFLOWRNNBINARYCONFIG;
        }
        return ConfigConstants.MACHINELEARNINGTENSORFLOWRNNCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        TensorflowRNNConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowRNNConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowRNNConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowRNNConfig.class, binary);
            }
        }
        param.setTensorflowRNNConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantTensorflowRNNPersist();
    }

}

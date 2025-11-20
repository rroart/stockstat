package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLSTMConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowLSTMModel  extends MLClassifyTensorflowRecurrentModel {
    public MLClassifyTensorflowLSTMModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 6;
    }
    
    @Override
    public String getName() {
        return MLConstants.LSTM;
    }
    
    @Override
    public String getKey(boolean binary) {
        if (binary) {
            return ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMBINARYCONFIG;
        }
        return ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        TensorflowLSTMConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowLSTMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowLSTMConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowLSTMConfig.class, binary);
            }
        }
        param.setTensorflowLSTMConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantTensorflowLSTMPersist();
    }

}

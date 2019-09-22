package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLSTMConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowLSTMModel  extends MLClassifyTensorflowRecurrentModel {
    public MLClassifyTensorflowLSTMModel(MyMyConfig conf) {
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
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowLSTMConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowLSTMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowLSTMConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowLSTMConfig.class);
            }
        }
        param.setTensorflowLSTMConfig(modelConf);
        return modelConf;
    }

}

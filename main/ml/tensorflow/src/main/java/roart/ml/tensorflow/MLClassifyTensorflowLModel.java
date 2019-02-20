package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowLModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowLModel(MyMyConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 2;
    }
    
    @Override
    public String getName() {
        return MLConstants.L;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWLCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowLConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowLConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowLConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowLConfig.class);
            }
        }
        param.setTensorflowLConfig(modelConf);
        return modelConf;
    }

}

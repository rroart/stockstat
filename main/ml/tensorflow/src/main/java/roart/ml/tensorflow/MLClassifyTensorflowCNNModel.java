package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowCNNConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowCNNModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowCNNModel(MyMyConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 5;
    }
    
    @Override
    public String getName() {
        return MLConstants.CNN;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWCNNCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowCNNConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowCNNConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowCNNConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowCNNConfig.class);
            }
        }
        param.setTensorflowCNNConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean isTwoDimensional() {
        return false;
    }

    @Override
    public boolean isThreeDimensional() {
        return true;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantTensorflowCNNPersist();
    }

}

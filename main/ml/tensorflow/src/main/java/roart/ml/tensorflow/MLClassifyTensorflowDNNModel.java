package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowLConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowDNNModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowDNNModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String getName() {
        return MLConstants.DNN;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWDNNCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowDNNConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowDNNConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowDNNConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowDNNConfig.class);
            }
        }
        param.setTensorflowDNNConfig(modelConf);
        return modelConf;
    }

}

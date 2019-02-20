package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowDNNLConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowDNNLModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowDNNLModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public String getName() {
        return MLConstants.DNNL;
    }

    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWDNNLCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowDNNLConfig modelConf = null;
        if (conf != null) {
            //modelConf = conf.getTensorflowDNNLConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowDNNLConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowDNNLConfig.class);
            }
        }
        param.setTensorflowDNNLConfig(modelConf);
        return modelConf;
    }

}

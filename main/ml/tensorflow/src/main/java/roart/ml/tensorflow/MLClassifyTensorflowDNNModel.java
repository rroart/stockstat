package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowDNNModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowDNNModel(IclijConfig conf) {
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
    public String getKey(boolean binary) {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWDNNCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        TensorflowDNNConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowDNNConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowDNNConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowDNNConfig.class, binary);
            }
        }
        param.setTensorflowDNNConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantTensorflowDNNPersist();
    }

}

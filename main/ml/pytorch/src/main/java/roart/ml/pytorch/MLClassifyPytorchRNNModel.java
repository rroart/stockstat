package roart.ml.pytorch;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchRNNConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyPytorchRNNModel  extends MLClassifyPytorchRecurrentModel {
    public MLClassifyPytorchRNNModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public String getName() {
        return MLConstants.RNN;
    }
    
    @Override
    public String getKey(boolean binary) {
        if (binary) {
            return ConfigConstants.MACHINELEARNINGPYTORCHRNNBINARYCONFIG;
        }
        return ConfigConstants.MACHINELEARNINGPYTORCHRNNCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        PytorchRNNConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchRNNConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchRNNConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(PytorchRNNConfig.class, binary);
            }
        }
        param.setPytorchRNNConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantPytorchRNNPersist();
    }

}

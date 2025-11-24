package roart.ml.pytorch;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchCNNConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyPytorchCNNModel  extends MLClassifyPytorchModel {
    public MLClassifyPytorchCNNModel(IclijConfig conf) {
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
    public String getKey(boolean binary) {
        if (binary) {
            return ConfigConstants.MACHINELEARNINGPYTORCHCNNBINARYCONFIG;
        }
        return ConfigConstants.MACHINELEARNINGPYTORCHCNNCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        PytorchCNNConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchCNNConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchCNNConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(PytorchCNNConfig.class, binary);
            }
        }
        param.setPytorchCNNConfig(modelConf);
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
        return getConf().wantPytorchCNNPersist();
    }

}

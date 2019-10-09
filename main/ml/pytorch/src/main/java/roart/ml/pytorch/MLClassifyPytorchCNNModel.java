package roart.ml.pytorch;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchCNNConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyPytorchCNNModel  extends MLClassifyPytorchModel {
    public MLClassifyPytorchCNNModel(MyMyConfig conf) {
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
        PytorchCNNConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchCNNConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchCNNConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(PytorchCNNConfig.class);
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

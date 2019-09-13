package roart.ml.pytorch;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchMLPConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyPytorchMLPModel  extends MLClassifyPytorchModel {
    public MLClassifyPytorchMLPModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String getName() {
        return MLConstants.MLP;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGPYTORCHMLPCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        PytorchMLPConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchMLPConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchMLPConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(PytorchMLPConfig.class);
            }
        }
        param.setPytorchMLPConfig(modelConf);
        return modelConf;
    }

}

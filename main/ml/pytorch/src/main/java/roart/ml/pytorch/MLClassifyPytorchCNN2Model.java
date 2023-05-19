package roart.ml.pytorch;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchCNN2Config;
import roart.ml.model.LearnTestClassify;

public class MLClassifyPytorchCNN2Model extends MLClassifyPytorchModel {
    public MLClassifyPytorchCNN2Model(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 6;
    }

    @Override
    public String getName() {
        return MLConstants.CNN2;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGPYTORCHCNN2CONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        PytorchCNN2Config modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchCNN2Config();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchCNN2Config.class);
            if (modelConf == null) {
                modelConf = getDefault(PytorchCNN2Config.class);
            }
        }
        param.setPytorchCNN2Config(modelConf);
        return modelConf;
    }

    @Override
    public boolean isTwoDimensional() {
        return false;
    }

    @Override
    public boolean isFourDimensional() {
        return true;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantPytorchCNN2Persist();
    }

}

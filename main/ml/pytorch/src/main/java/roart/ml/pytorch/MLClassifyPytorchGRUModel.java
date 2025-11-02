package roart.ml.pytorch;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchGRUConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyPytorchGRUModel  extends MLClassifyPytorchRecurrentModel {
    public MLClassifyPytorchGRUModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 4;
    }

    @Override
    public String getName() {
        return MLConstants.GRU;
    }
    
    @Override
    public String getKey(boolean binary) {
        if (binary) {
            return ConfigConstants.MACHINELEARNINGPYTORCHGRUBINARYCONFIG;
        }
        return ConfigConstants.MACHINELEARNINGPYTORCHGRUCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        PytorchGRUConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchGRUConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchGRUConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(PytorchGRUConfig.class, binary);
            }
        }
        param.setPytorchGRUConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantPytorchGRUPersist();
    }
}

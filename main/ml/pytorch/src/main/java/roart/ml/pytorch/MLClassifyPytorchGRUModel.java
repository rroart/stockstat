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
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGPYTORCHGRUCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        PytorchGRUConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchGRUConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchGRUConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(PytorchGRUConfig.class);
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

package roart.ml.pytorch;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.PytorchLSTMConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyPytorchLSTMModel  extends MLClassifyPytorchRecurrentModel {
    public MLClassifyPytorchLSTMModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public String getName() {
        return MLConstants.LSTM;
    }
    
    @Override
    public String getKey(boolean binary) {
        if (binary) {
            return ConfigConstants.MACHINELEARNINGPYTORCHLSTMBINARYCONFIG;
        }
        return ConfigConstants.MACHINELEARNINGPYTORCHLSTMCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        PytorchLSTMConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getPytorchConfig().getPytorchLSTMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(PytorchLSTMConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(PytorchLSTMConfig.class, binary);
            }
        }
        param.setPytorchLSTMConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantPytorchLSTMPersist();
    }

}

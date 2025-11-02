package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowMLPConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowMLPModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowMLPModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 3;
    }
    
    @Override
    public String getName() {
        return MLConstants.MLP;
    }
    
    @Override
    public String getKey(boolean binary) {
        if (binary) {
            return ConfigConstants.MACHINELEARNINGTENSORFLOWMLPBINARYCONFIG;
        }
        return ConfigConstants.MACHINELEARNINGTENSORFLOWMLPCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        TensorflowMLPConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowMLPConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowMLPConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowMLPConfig.class, binary);
            }
        }
        param.setTensorflowMLPConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantTensorflowMLPPersist();
    }

}

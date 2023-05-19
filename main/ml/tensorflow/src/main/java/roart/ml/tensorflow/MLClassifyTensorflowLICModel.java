package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLICConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowLICModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowLICModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 2;
    }
    
    @Override
    public String getName() {
        return MLConstants.LIC;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWLICCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowLICConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowLICConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowLICConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowLICConfig.class);
            }
        }
        param.setTensorflowLICConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantTensorflowLICPersist();
    }

}

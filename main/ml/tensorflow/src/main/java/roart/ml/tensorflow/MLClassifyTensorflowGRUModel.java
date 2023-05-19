package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowGRUConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowGRUModel  extends MLClassifyTensorflowRecurrentModel {
    public MLClassifyTensorflowGRUModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 7;
    }
    
    @Override
    public String getName() {
        return MLConstants.GRU;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWGRUCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowGRUConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowGRUConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowGRUConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowGRUConfig.class);
            }
        }
        param.setTensorflowGRUConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantTensorflowGRUPersist();
    }

}

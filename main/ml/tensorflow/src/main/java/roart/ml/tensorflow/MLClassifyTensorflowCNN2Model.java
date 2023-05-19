package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowCNN2Config;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowCNN2Model extends MLClassifyTensorflowModel{
    public MLClassifyTensorflowCNN2Model(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 9;
    }
    
    @Override
    public String getName() {
        return MLConstants.CNN2;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2CONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowCNN2Config modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowCNN2Config();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowCNN2Config.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowCNN2Config.class);
            }
        }
        param.setTensorflowCNN2Config(modelConf);
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
        return getConf().wantTensorflowCNN2Persist();
    }


}

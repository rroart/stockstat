package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLIRConfig;
import roart.ml.model.LearnTestClassify;

public class MLClassifyTensorflowLIRModel  extends MLClassifyTensorflowModel {
    public MLClassifyTensorflowLIRModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return 8;
    }
    
    @Override
    public String getName() {
        return MLConstants.LIR;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIRCONFIG;
    }
    
    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        TensorflowLIRConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowConfig().getTensorflowLIRConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowLIRConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowLIRConfig.class);
            }
        }
        param.setTensorflowLIRConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean isPredictorOnly() {
        return true;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantPredictorTensorflowLIRPersist();
    }

}

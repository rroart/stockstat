package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.ml.model.LearnTestPredict;

@Deprecated
public class MLPredictTensorflowLSTMModel extends MLPredictTensorflowModel {
    public MLPredictTensorflowLSTMModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 1;
    }
    
    @Override
    public String getName() {
        return "LSTM";
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTMCONFIG;
    }

   @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestPredict param) {
        TensorflowPredictorLSTMConfig modelConf = null;
        if (conf != null) {
            //modelConf = conf.getTensorflowConfig().getTensorflowPredictorLSTMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowPredictorLSTMConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowPredictorLSTMConfig.class);
            }
        }
        param.setTensorflowPredictorLSTMConfig(modelConf);
        return modelConf;
    }

}

package roart.ml.tensorflow;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowLSTMConfig;
import roart.ml.model.LearnTestPredict;

public class MLPredictTensorflowLSTMModel extends MLPredictTensorflowModel {
    public MLPredictTensorflowLSTMModel(MyMyConfig conf) {
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
        return ConfigConstants.MACHINELEARNINGTENSORFLOWLSTMCONFIG;
    }

   @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestPredict param) {
        TensorflowLSTMConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getTensorflowLSTMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(TensorflowLSTMConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(TensorflowLSTMConfig.class);
            }
        }
        param.setTensorflowLSTMConfig(modelConf);
        return modelConf;
    }

}

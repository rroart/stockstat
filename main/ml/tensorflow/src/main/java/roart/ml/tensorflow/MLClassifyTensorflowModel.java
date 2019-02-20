package roart.ml.tensorflow;

import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassify;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyTensorflowModel extends MLClassifyModel {
    public MLClassifyTensorflowModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
    
    @Override
    public int getSizes(Aggregator indicator) { 
        return 2 * super.getSizes(indicator);
    }

    @Override
    public int getReturnSize() {
        return 2;
    }
    
    public abstract NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param);
    
}

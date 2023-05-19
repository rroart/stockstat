package roart.ml.tensorflow;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassify;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyTensorflowModel extends MLClassifyModel {
    public MLClassifyTensorflowModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public String getEngineName() {
        return MLConstants.TENSORFLOW;
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
    
    @Override
    public String getPath() {
        return getConf().getTensorflowPath();
    }

    @Override
    public String getShortName() {
        return getName();
    }

}

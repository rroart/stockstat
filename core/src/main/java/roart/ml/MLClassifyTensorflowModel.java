package roart.ml;

import roart.aggregate.Aggregator;

public abstract class MLClassifyTensorflowModel extends MLClassifyModel {
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
}

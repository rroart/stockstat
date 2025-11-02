package roart.ml.common;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.pipeline.common.aggregate.Aggregator;

public abstract class MLClassifyRandomModel extends MLClassifyModel {
    public MLClassifyRandomModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public String getEngineName() {
        return MLConstants.RANDOM;
    }
    
    @Override
    public int getSizes(Aggregator indicator) { 
        return super.getSizes(indicator);
    }

    @Override
    public int getReturnSize() {
        return 1;
    }

    @Override
    public String getShortName() {
        return getName();
    }

    @Override
    public String getKey(boolean binary) {
        return "Random";
    }

    @Override
    public String getPath() {
        return null;
    }

    @Override
    public boolean wantPersist() {
        return false;
    }
    
}

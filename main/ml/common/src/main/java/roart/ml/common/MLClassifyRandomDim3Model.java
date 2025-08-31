package roart.ml.common;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;

public class MLClassifyRandomDim3Model extends MLClassifyRandomModel {

    public MLClassifyRandomDim3Model(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public String getName() {
        return MLConstants.RND + "3";
    }

    @Override
    public boolean isTwoDimensional() {
        return false;
    }
 
    @Override
    public boolean isThreeDimensional() {
        return true;
    }
 
}

package roart.ml.common;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;

public class MLClassifyRandomDim4Model extends MLClassifyRandomModel {

    public MLClassifyRandomDim4Model(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 4;
    }

    @Override
    public String getName() {
        return MLConstants.RND + "4";
    }

    @Override
    public boolean isTwoDimensional() {
        return false;
    }
 
    @Override
    public boolean isFourDimensional() {
        return true;
    }
 
}

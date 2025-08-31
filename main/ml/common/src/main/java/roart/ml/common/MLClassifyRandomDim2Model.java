package roart.ml.common;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;

public class MLClassifyRandomDim2Model extends MLClassifyRandomModel {

    public MLClassifyRandomDim2Model(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public String getName() {
        return MLConstants.RND + "2";
    }

}

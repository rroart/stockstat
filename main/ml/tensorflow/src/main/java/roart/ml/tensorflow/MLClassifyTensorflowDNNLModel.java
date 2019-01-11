package roart.ml.tensorflow;

import roart.common.config.MLConstants;

public class MLClassifyTensorflowDNNLModel  extends MLClassifyTensorflowModel {
    @Override
   public int getId() {
        return 3;
    }
    @Override
   public String getName() {
        return MLConstants.DNNL;
    }
}

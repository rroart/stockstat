package roart.ml;

import roart.config.MLConstants;

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

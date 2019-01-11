package roart.ml.tensorflow;

import roart.common.config.MLConstants;

public class MLClassifyTensorflowDNNModel  extends MLClassifyTensorflowModel {
    @Override
   public int getId() {
        return 1;
    }
    @Override
   public String getName() {
        return MLConstants.DNN;
    }
}

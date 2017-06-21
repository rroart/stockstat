package roart.ml;

public class MLClassifyTensorflowDNNModel  extends MLClassifyTensorflowModel {
    @Override
   public int getId() {
        return 1;
    }
    @Override
   public String getName() {
        return "DNN";
    }
}

package roart.ml;

public class MLPredictTensorflowLSTMModel  extends MLPredictTensorflowModel {
    @Override
   public int getId() {
        return 1;
    }
    @Override
   public String getName() {
        return "LSTM";
    }
}

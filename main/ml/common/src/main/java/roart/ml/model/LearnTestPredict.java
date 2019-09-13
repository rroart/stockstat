package roart.ml.model;

import java.util.List;

import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowLICConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;

public class LearnTestPredict {
    public List<Double[]> slides;
    //public List<Double> next;
    public Object[] array;
    public List<Object[]> arraylist;
    public Object[] cat;
    public List<List<Object>> listlist;
    public int modelInt;
    public int size;
    public String period;
    public String mapname;
    public int outcomes;
    public Double prob;

    private TensorflowPredictorLSTMConfig tensorflowLSTMConfig;

    public TensorflowPredictorLSTMConfig getTensorflowLSTMConfig() {
        return tensorflowLSTMConfig;
    }

    public void setTensorflowPredictorLSTMConfig(TensorflowPredictorLSTMConfig tensorflowLSTMConfig) {
        this.tensorflowLSTMConfig = tensorflowLSTMConfig;
    }
}

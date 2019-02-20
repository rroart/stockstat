package roart.ml.model;

import java.util.List;

import roart.common.ml.TensorflowDNNConfig;
import roart.common.ml.TensorflowLConfig;
import roart.common.ml.TensorflowLSTMConfig;

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

    private TensorflowLSTMConfig tensorflowLSTMConfig;

    public TensorflowLSTMConfig getTensorflowLSTMConfig() {
        return tensorflowLSTMConfig;
    }

    public void setTensorflowLSTMConfig(TensorflowLSTMConfig tensorflowLSTMConfig) {
        this.tensorflowLSTMConfig = tensorflowLSTMConfig;
    }
}

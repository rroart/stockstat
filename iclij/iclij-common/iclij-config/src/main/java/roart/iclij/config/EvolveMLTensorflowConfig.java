package roart.iclij.config;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.ConfigConstants;

public class EvolveMLTensorflowConfig {

    private EvolveMLConfig dnn;

    private EvolveMLConfig lic;

    private EvolveMLConfig lir;

    private EvolveMLConfig mlp;
    
    private EvolveMLConfig cnn;

    private EvolveMLConfig cnn2;

    private EvolveMLConfig rnn;
    
    private EvolveMLConfig gru;
    
    private EvolveMLConfig lstm;
    
    public EvolveMLConfig getDnn() {
        return dnn;
    }
    public void setDnn(EvolveMLConfig dnn) {
        this.dnn = dnn;
    }
    public EvolveMLConfig getLic() {
        return lic;
    }
    public void setLic(EvolveMLConfig lic) {
        this.lic = lic;
    }
    public EvolveMLConfig getLir() {
        return lir;
    }
    public void setLir(EvolveMLConfig lir) {
        this.lir = lir;
    }
    public EvolveMLConfig getMlp() {
        return mlp;
    }
    public void setMlp(EvolveMLConfig mlp) {
        this.mlp = mlp;
    }
    public EvolveMLConfig getCnn() {
        return cnn;
    }
    public void setCnn(EvolveMLConfig cnn) {
        this.cnn = cnn;
    }
    public EvolveMLConfig getCnn2() {
        return cnn2;
    }
    public void setCnn2(EvolveMLConfig cnn2) {
        this.cnn2 = cnn2;
    }
    public EvolveMLConfig getRnn() {
        return rnn;
    }
    public void setRnn(EvolveMLConfig rnn) {
        this.rnn = rnn;
    }
    public EvolveMLConfig getGru() {
        return gru;
    }
    public void setGru(EvolveMLConfig gru) {
        this.gru = gru;
    }
    public EvolveMLConfig getLstm() {
        return lstm;
    }
    public void setLstm(EvolveMLConfig lstm) {
        this.lstm = lstm;
    }
    public void merge(EvolveMLTensorflowConfig tensorflow) {
        if (tensorflow == null) {
            return;
        }
        dnn.merge(tensorflow.dnn);
        lic.merge(tensorflow.lic);
        lir.merge(tensorflow.lir);
        mlp.merge(tensorflow.mlp);
        cnn.merge(tensorflow.cnn);
        cnn2.merge(tensorflow.cnn2);
        rnn.merge(tensorflow.rnn);
        gru.merge(tensorflow.gru);
        lstm.merge(tensorflow.lstm);
    }
    
    public Map<? extends String, ? extends EvolveMLConfig> getAll() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, dnn);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC, lic);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP, mlp);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN, cnn);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN2, cnn2);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN, rnn);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU, gru);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM, lstm);
        return map;
    }

    public Map<? extends String, ? extends EvolveMLConfig> getAllPredictors() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLIR, lir);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWMLP, mlp);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWRNN, rnn);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWGRU, gru);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSTENSORFLOWLSTM, lstm);
        return map;
    }
}

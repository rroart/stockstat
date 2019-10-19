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
    
    private EvolveMLConfig rnn;
    
    private EvolveMLConfig gru;
    
    private EvolveMLConfig lstm;
    
    private EvolveMLConfig predictorlstm;
        
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
    public EvolveMLConfig getPredictorlstm() {
        return predictorlstm;
    }
    public void setPredictorlstm(EvolveMLConfig predictorlstm) {
        this.predictorlstm = predictorlstm;
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
        rnn.merge(tensorflow.rnn);
        gru.merge(tensorflow.gru);
        lstm.merge(tensorflow.lstm);
        predictorlstm.merge(tensorflow.predictorlstm);
    }
    public Map<? extends String, ? extends EvolveMLConfig> getAll() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, dnn);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIC, lic);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLIR, lir);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWMLP, mlp);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWCNN, cnn);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWRNN, rnn);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWGRU, gru);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM, lstm);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWPREDICTORLSTM, predictorlstm);
        return map;
    }
}

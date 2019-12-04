package roart.iclij.config;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.ConfigConstants;

public class EvolveMLPytorchConfig {

    private EvolveMLConfig mlp;
    
    private EvolveMLConfig cnn;
    
    private EvolveMLConfig cnn2;
    
    private EvolveMLConfig rnn;
    
    private EvolveMLConfig gru;
    
    private EvolveMLConfig lstm;
    
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
    public void merge(EvolveMLPytorchConfig pytorch) {
        if (pytorch == null) {
            return;
        }
        mlp.merge(pytorch.mlp);
        cnn.merge(pytorch.cnn);
        cnn2.merge(pytorch.cnn2);
        rnn.merge(pytorch.rnn);
        gru.merge(pytorch.gru);
        lstm.merge(pytorch.lstm);
    }

    public Map<? extends String, ? extends EvolveMLConfig> getAll() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHMLP, mlp);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN2, cnn2);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHCNN, cnn);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHRNN, rnn);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHGRU, gru);
        map.put(ConfigConstants.MACHINELEARNINGPYTORCHLSTM, lstm);
        return map;
    }
    
    public Map<? extends String, ? extends EvolveMLConfig> getAllPredictors() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHMLP, mlp);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHRNN, rnn);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHGRU, gru);
        map.put(ConfigConstants.MACHINELEARNINGPREDICTORSPYTORCHLSTM, lstm);
        return map;
    }
    
}

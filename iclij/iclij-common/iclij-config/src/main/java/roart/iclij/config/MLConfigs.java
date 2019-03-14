package roart.iclij.config;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.ConfigConstants;

public class MLConfigs {

    private EvolveMLConfig mcp;
    
    private EvolveMLConfig lr;
    
    private EvolveMLConfig ovr;
    
    private EvolveMLConfig dnn;
    
    private EvolveMLConfig dnnl;
    
    private EvolveMLConfig l;

    private EvolveMLConfig lstm;

    public EvolveMLConfig getMcp() {
        return mcp;
    }

    public void setMcp(EvolveMLConfig mcp) {
        this.mcp = mcp;
    }

    public EvolveMLConfig getLr() {
        return lr;
    }

    public void setLr(EvolveMLConfig lr) {
        this.lr = lr;
    }

    public EvolveMLConfig getOvr() {
        return ovr;
    }

    public void setOvr(EvolveMLConfig ovr) {
        this.ovr = ovr;
    }

    public EvolveMLConfig getDnn() {
        return dnn;
    }

    public void setDnn(EvolveMLConfig dnn) {
        this.dnn = dnn;
    }

    public EvolveMLConfig getDnnl() {
        return dnnl;
    }

    public void setDnnl(EvolveMLConfig dnnl) {
        this.dnnl = dnnl;
    }

    public EvolveMLConfig getL() {
        return l;
    }

    public void setL(EvolveMLConfig l) {
        this.l = l;
    }

    public EvolveMLConfig getLstm() {
        return lstm;
    }

    public void setLstm(EvolveMLConfig lstm) {
        this.lstm = lstm;
    }

    /**
     * 
     * Merge in other by override if not null
     * 
     * @param mlConfigs
     * @return
     */
    
    public void merge(MLConfigs mlConfigs) {
        if (mlConfigs == null) {
            return;
        }
        mcp.merge(mlConfigs.mcp);
        lr.merge(mlConfigs.lr);
        ovr.merge(mlConfigs.ovr);
        dnn.merge(mlConfigs.dnn);
        l.merge(mlConfigs.l);
        lstm.merge(mlConfigs.lstm);
    }

    public Map<String, EvolveMLConfig> getAll() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLMCP, mcp);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLR, lr);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLOVR, ovr);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWDNN, dnn);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWL, l);
        map.put(ConfigConstants.MACHINELEARNINGTENSORFLOWLSTM, lstm);
        return map;
    }
}

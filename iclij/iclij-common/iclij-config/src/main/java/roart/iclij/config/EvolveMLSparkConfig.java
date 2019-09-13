package roart.iclij.config;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.ConfigConstants;

public class EvolveMLSparkConfig {

    private EvolveMLConfig mlpc;
    private EvolveMLConfig lor;
    private EvolveMLConfig ovr;
    private EvolveMLConfig lsvc;
    public EvolveMLConfig getMlpc() {
        return mlpc;
    }
    public void setMlpc(EvolveMLConfig mlpc) {
        this.mlpc = mlpc;
    }
    public EvolveMLConfig getLor() {
        return lor;
    }
    public void setLor(EvolveMLConfig lor) {
        this.lor = lor;
    }
    public EvolveMLConfig getOvr() {
        return ovr;
    }
    public void setOvr(EvolveMLConfig ovr) {
        this.ovr = ovr;
    }
    public EvolveMLConfig getLsvc() {
        return lsvc;
    }
    public void setLsvc(EvolveMLConfig lsvc) {
        this.lsvc = lsvc;
    }
    
    public void merge(EvolveMLSparkConfig spark) {
        if (spark == null) {
            return;
        }
        mlpc.merge(spark.mlpc);
        lor.merge(spark.lor);
        ovr.merge(spark.ovr);
        lsvc.merge(spark.lsvc);
    }
    public Map<? extends String, ? extends EvolveMLConfig> getAll() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLMLPC, mlpc);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLOR, lor);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLOVR, ovr);
        map.put(ConfigConstants.MACHINELEARNINGSPARKMLLSVC, lsvc);
        return map;
    }
}

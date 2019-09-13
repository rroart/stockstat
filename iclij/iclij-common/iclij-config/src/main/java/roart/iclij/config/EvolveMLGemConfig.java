package roart.iclij.config;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.ConfigConstants;

public class EvolveMLGemConfig {

    private EvolveMLConfig ewc;
    
    private EvolveMLConfig gem;
    
    private EvolveMLConfig i;
    
    private EvolveMLConfig icarl;

    private EvolveMLConfig mm;
    
    private EvolveMLConfig s;

    public EvolveMLConfig getEwc() {
        return ewc;
    }

    public void setEwc(EvolveMLConfig ewc) {
        this.ewc = ewc;
    }

    public EvolveMLConfig getGem() {
        return gem;
    }

    public void setGem(EvolveMLConfig gem) {
        this.gem = gem;
    }

    public EvolveMLConfig getI() {
        return i;
    }

    public void setI(EvolveMLConfig i) {
        this.i = i;
    }

    public EvolveMLConfig getIcarld() {
        return icarl;
    }

    public void setIcarld(EvolveMLConfig icarld) {
        this.icarl = icarld;
    }

    public EvolveMLConfig getMm() {
        return mm;
    }

    public void setMm(EvolveMLConfig mm) {
        this.mm = mm;
    }

    public EvolveMLConfig getS() {
        return s;
    }

    public void setS(EvolveMLConfig s) {
        this.s = s;
    }

    public void merge(EvolveMLGemConfig gem) {
        ewc.merge(gem.ewc);
        this.gem.merge(gem.gem);
        i.merge(gem.i);
        icarl.merge(gem.icarl);
        mm.merge(gem.mm);
        s.merge(gem.s);
    }

    public Map<? extends String, ? extends EvolveMLConfig> getAll() {
        Map<String, EvolveMLConfig> map = new HashMap<>();
        map.put(ConfigConstants.MACHINELEARNINGGEMEWC, ewc);
        map.put(ConfigConstants.MACHINELEARNINGGEMGEM, gem);
        map.put(ConfigConstants.MACHINELEARNINGGEMINDEPENDENT, i);
        map.put(ConfigConstants.MACHINELEARNINGGEMICARL, icarl);
        map.put(ConfigConstants.MACHINELEARNINGGEMMULTIMODAL, mm);
        map.put(ConfigConstants.MACHINELEARNINGGEMSINGLE, s);
        return map;
    }
    
}

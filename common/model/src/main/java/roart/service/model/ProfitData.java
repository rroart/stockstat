package roart.service.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.model.IncDecDTO;
import roart.common.model.MemoryDTO;

public class ProfitData {
    private ProfitInputData inputdata;
    
    private IclijConfig conf;

    private List<Integer> positions;
    
    private Map<String, IncDecDTO> buys = new HashMap<>();
    
    private Map<String, IncDecDTO> sells = new HashMap<>();
    
    private Map<Object[], Double> confMap;
    
    private Map<Object[], List<MemoryDTO>> listMap;
    
    public ProfitData() {
        super();
    }
    
    public ProfitInputData getInputdata() {
        return inputdata;
    }

    public void setInputdata(ProfitInputData inputdata) {
        this.inputdata = inputdata;
    }

    public IclijConfig getConf() {
        return conf;
    }

    public void setConf(IclijConfig conf) {
        this.conf = conf;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    public Map<String, IncDecDTO> getBuys() {
        return buys;
    }

    public void setBuys(Map<String, IncDecDTO> buys) {
        this.buys = buys;
    }

    public Map<String, IncDecDTO> getSells() {
        return sells;
    }

    public void setSells(Map<String, IncDecDTO> sells) {
        this.sells = sells;
    }

}

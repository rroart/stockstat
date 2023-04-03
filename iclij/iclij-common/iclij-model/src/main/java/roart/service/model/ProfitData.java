package roart.service.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.model.IncDecItem;
import roart.common.model.MemoryItem;

public class ProfitData {
    private ProfitInputData inputdata;
    
    private MyMyConfig conf;

    private List<Integer> positions;
    
    private Map<String, IncDecItem> buys = new HashMap<>();
    
    private Map<String, IncDecItem> sells = new HashMap<>();
    
    private Map<Object[], Double> confMap;
    
    private Map<Object[], List<MemoryItem>> listMap;
    
    public ProfitData() {
        super();
    }
    
    public ProfitInputData getInputdata() {
        return inputdata;
    }

    public void setInputdata(ProfitInputData inputdata) {
        this.inputdata = inputdata;
    }

    public MyMyConfig getConf() {
        return conf;
    }

    public void setConf(MyMyConfig conf) {
        this.conf = conf;
    }

    public List<Integer> getPositions() {
        return positions;
    }

    public void setPositions(List<Integer> positions) {
        this.positions = positions;
    }

    public Map<String, IncDecItem> getBuys() {
        return buys;
    }

    public void setBuys(Map<String, IncDecItem> buys) {
        this.buys = buys;
    }

    public Map<String, IncDecItem> getSells() {
        return sells;
    }

    public void setSells(Map<String, IncDecItem> sells) {
        this.sells = sells;
    }

}

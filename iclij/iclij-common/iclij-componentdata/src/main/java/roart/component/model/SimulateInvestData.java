package roart.component.model;

import java.util.List;
import java.util.Map;

public class SimulateInvestData extends ComponentData {

    private List allIncDecs;
    
    private List allMemories;
    
    private List allMetas;
    
    private Map<String, List<List<Double>>> aCategoryValueMap;
    
    private List<String> stockDates;
    
    public SimulateInvestData(ComponentData componentparam) {
        super(componentparam);
    }

    public List getAllIncDecs() {
        return allIncDecs;
    }

    public void setAllIncDecs(List allIncDecs) {
        this.allIncDecs = allIncDecs;
    }

    public List getAllMemories() {
        return allMemories;
    }

    public void setAllMemories(List allMemories) {
        this.allMemories = allMemories;
    }

    public List getAllMetas() {
        return allMetas;
    }

    public void setAllMetas(List allMetas) {
        this.allMetas = allMetas;
    }

    public Map<String, List<List<Double>>> getaCategoryValueMap() {
        return aCategoryValueMap;
    }

    public void setaCategoryValueMap(Map<String, List<List<Double>>> aCategoryValueMap) {
        this.aCategoryValueMap = aCategoryValueMap;
    }

    public List<String> getStockDates() {
        return stockDates;
    }

    public void setStockDates(List<String> stockDates) {
        this.stockDates = stockDates;
    }

}

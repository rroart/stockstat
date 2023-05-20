package roart.component.model;

import java.util.List;
import java.util.Map;

import roart.iclij.config.SimulateInvestConfig;

public class SimulateInvestData extends ComponentData {

    private List allIncDecs;
    
    private List allMemories;
    
    private List allMetas;
    
    private Map<String, List<List<Double>>> categoryValueRebaseMap;
    
    private Map<String, Map<String, Object>> resultRebaseMaps;
    
    private SimulateInvestConfig config;
    
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

    public Map<String, List<List<Double>>> getCategoryValueRebaseMap() {
        return categoryValueRebaseMap;
    }

    public void setCategoryValueRebaseMap(Map<String, List<List<Double>>> categoryValueRebaseMap) {
        this.categoryValueRebaseMap = categoryValueRebaseMap;
    }

    public Map<String, Map<String, Object>> getResultRebaseMaps() {
        return resultRebaseMaps;
    }

    public void setResultRebaseMaps(Map<String, Map<String, Object>> resultRebaseMaps) {
        this.resultRebaseMaps = resultRebaseMaps;
    }

    public SimulateInvestConfig getSimConfig() {
        return config;
    }

    public void setConfig(SimulateInvestConfig config) {
        this.config = config;
    }

}

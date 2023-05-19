package roart.common.service;

import java.util.List;
import java.util.Set;

import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.result.model.GUISize;

public class ServiceParam {
    private IclijConfig config;
    
    private GUISize guiSize;
    
    private Set<String> ids;
    
    private String market;
    
    private boolean wantMaps;
    
    private List<String> confList;
    
    private String webpath;

    private NeuralNetCommand neuralnetcommand;
    
    public ServiceParam() {
        super();
    }

    public IclijConfig getConfig() {
        return config;
    }

    public void setConfig(IclijConfig config) {
        this.config = config;
    }

    public GUISize getGuiSize() {
        return guiSize;
    }

    public void setGuiSize(GUISize guiSize) {
        this.guiSize = guiSize;
    }

    public Set<String> getIds() {
        return ids;
    }

    public void setIds(Set<String> ids) {
        this.ids = ids;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public boolean isWantMaps() {
        return wantMaps;
    }

    public void setWantMaps(boolean wantMaps) {
        this.wantMaps = wantMaps;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    public String getWebpath() {
        return webpath;
    }

    public void setWebpath(String webpath) {
        this.webpath = webpath;
    }

    public NeuralNetCommand getNeuralnetcommand() {
        return neuralnetcommand;
    }

    public void setNeuralnetcommand(NeuralNetCommand neuralnetcommand) {
        this.neuralnetcommand = neuralnetcommand;
    }

}

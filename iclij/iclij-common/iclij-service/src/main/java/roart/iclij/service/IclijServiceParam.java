package roart.iclij.service;

import java.util.List;
import java.util.Set;

import roart.common.config.ConfigData;
import roart.common.ml.NeuralNetCommand;
import roart.iclij.config.IclijConfig;
import roart.result.model.GUISize;

public class IclijServiceParam {
    private String id;
    
    private Set<String> ids;
    
    private String market;
    
    private boolean wantMaps;
    
    private ConfigData configData;
    
    //private VerifyConfig verifyConfig;

    private String webpath;
    
    private Integer offset;

    private List<String> confList;
    
    private NeuralNetCommand neuralnetcommand;

    private GUISize guiSize;
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    /*
    public VerifyConfig getVerifyConfig() {
        return verifyConfig;
    }

    public void setVerifyConfig(VerifyConfig verifyConfig) {
        this.verifyConfig = verifyConfig;
    }
*/
    
    @Deprecated
    public void setConfig(IclijConfig iclijConfig) {
    }

    public ConfigData getConfigData() {
        return configData;
    }

    public void setConfigData(ConfigData configData) {
        this.configData = configData;
    }

    public String getWebpath() {
        return webpath;
    }

    public void setWebpath(String webpath) {
        this.webpath = webpath;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public List<String> getConfList() {
        return confList;
    }

    public void setConfList(List<String> confList) {
        this.confList = confList;
    }

    public NeuralNetCommand getNeuralnetcommand() {
        return neuralnetcommand;
    }

    public void setNeuralnetcommand(NeuralNetCommand neuralnetcommand) {
        this.neuralnetcommand = neuralnetcommand;
    }

    public GUISize getGuiSize() {
        return guiSize;
    }

    public void setGuiSize(GUISize guiSize) {
        this.guiSize = guiSize;
    }
    
}

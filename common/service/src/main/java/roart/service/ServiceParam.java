package roart.service;

import java.util.List;
import java.util.Set;

import roart.config.MyConfig;
import roart.model.GUISize;

public class ServiceParam {
    private MyConfig config;
    
    private GUISize guiSize;
    
    private Set<String> ids;
    
    private String market;
    
    private boolean wantMaps;
    
    private List<String> confList;
    
    private String webpath;

    public ServiceParam() {
        super();
    }

    public MyConfig getConfig() {
        return config;
    }

    public void setConfig(MyConfig config) {
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

}

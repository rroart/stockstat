package roart.service;

import java.util.Set;

import roart.config.VerifyConfig;

public class IclijServiceParam {
    private Set<String> ids;
    
    private String market;
    
    private boolean wantMaps;
    
    private VerifyConfig verifyConfig;

    private String webPath;
    
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

    public VerifyConfig getVerifyConfig() {
        return verifyConfig;
    }

    public void setVerifyConfig(VerifyConfig verifyConfig) {
        this.verifyConfig = verifyConfig;
    }

    public String getWebpath() {
        return webPath;
    }

    public void setWebpath(String webpath) {
        this.webPath = webpath;
    }
    
}

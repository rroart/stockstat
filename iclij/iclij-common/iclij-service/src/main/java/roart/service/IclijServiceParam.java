package roart.service;

import java.util.Set;

import roart.config.IclijConfig;
import roart.config.VerifyConfig;

public class IclijServiceParam {
    private Set<String> ids;
    
    private String market;
    
    private boolean wantMaps;
    
    private IclijConfig iclijConfig;
    
    //private VerifyConfig verifyConfig;

    private String webpath;
    
    private Integer offset;
    
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
    
    public IclijConfig getIclijConfig() {
        return iclijConfig;
    }

    public void setIclijConfig(IclijConfig iclijConfig) {
        this.iclijConfig = iclijConfig;
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
    
}

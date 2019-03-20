package roart.config;

import roart.iclij.config.MLConfigs;

public class Market {
    private MarketConfig config;
    
    private MarketFilter filter;
    
    private MLConfigs mlconfig;

    public MarketConfig getConfig() {
        return config;
    }

    public void setConfig(MarketConfig config) {
        this.config = config;
    }

    public MarketFilter getFilter() {
        return filter;
    }

    public void setFilter(MarketFilter filter) {
        this.filter = filter;
    }

    public MLConfigs getMlconfig() {
        return mlconfig;
    }

    public void setMlconfig(MLConfigs mlConfigs) {
        this.mlconfig = mlConfigs;
    }

}

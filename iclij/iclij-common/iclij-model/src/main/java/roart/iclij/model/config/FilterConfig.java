package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public class FilterConfig extends ImproveFilterActionComponentConfig {

    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveAbovebelowEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return null;
    }
}

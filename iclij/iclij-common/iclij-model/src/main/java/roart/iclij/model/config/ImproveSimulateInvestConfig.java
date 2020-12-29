package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public class ImproveSimulateInvestConfig extends ActionComponentConfigNoML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveSimulateInvestEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return null;
    }

}

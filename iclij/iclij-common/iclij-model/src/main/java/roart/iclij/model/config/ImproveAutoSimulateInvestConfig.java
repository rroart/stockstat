package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public class ImproveAutoSimulateInvestConfig extends ActionComponentConfigNoML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getImproveAutoSimulateInvestEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return null;
    }


}

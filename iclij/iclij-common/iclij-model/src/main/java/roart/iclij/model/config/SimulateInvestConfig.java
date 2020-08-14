package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public class SimulateInvestConfig extends ActionComponentConfigNoML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return null;
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return null;
    }

}

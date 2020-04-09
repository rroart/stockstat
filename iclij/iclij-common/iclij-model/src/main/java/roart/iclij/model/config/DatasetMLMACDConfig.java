package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetMLMACDConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetMLMACDEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetMLMACDMLConfig();
    }

}

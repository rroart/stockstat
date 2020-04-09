package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetMLRSIConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetMLRSIEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetMLRSIMLConfig();
    }

}

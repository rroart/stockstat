package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetMLCCIConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetMLCCIEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetMLCCIMLConfig();
    }

}

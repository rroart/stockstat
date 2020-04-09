package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetMLIndicatorConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetMLIndicatorEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetMLIndicatorMLConfig();
    }

}

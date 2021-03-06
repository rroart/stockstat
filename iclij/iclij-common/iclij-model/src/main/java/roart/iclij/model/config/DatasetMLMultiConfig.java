package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetMLMultiConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetMLMultiEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetMLMultiMLConfig();
    }

}

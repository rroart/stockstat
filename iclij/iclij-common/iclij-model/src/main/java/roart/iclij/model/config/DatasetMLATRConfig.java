package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetMLATRConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetMLATREvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetMLATRMLConfig();
    }

}

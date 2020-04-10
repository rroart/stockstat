package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetMLSTOCHConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetMLSTOCHEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetMLSTOCHMLConfig();
    }

}

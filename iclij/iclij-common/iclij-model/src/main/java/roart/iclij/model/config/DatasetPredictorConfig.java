package roart.iclij.model.config;

import roart.iclij.config.IclijConfig;

public abstract class DatasetPredictorConfig extends DatasetActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getDatasetPredictorEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getDatasetPredictorMLConfig();
    }

}

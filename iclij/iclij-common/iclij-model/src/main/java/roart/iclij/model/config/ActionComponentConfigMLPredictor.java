package roart.iclij.model.config;

import java.util.Map;

import roart.common.ml.MLMaps;
import roart.common.ml.MLMapsPredictor;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.MLConfigs;

public abstract class ActionComponentConfigMLPredictor extends ActionComponentConfigML {

    public MLMaps getMLMaps() {
        return new MLMapsPredictor();
    }

    @Override
    protected Map<String, EvolveMLConfig> getMLConfigs(MLConfigs mlConfig) {
        return mlConfig.getAllPredictors();
    }
    
}

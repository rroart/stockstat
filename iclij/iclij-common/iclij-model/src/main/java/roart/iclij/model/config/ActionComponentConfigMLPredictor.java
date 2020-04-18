package roart.iclij.model.config;

import roart.common.ml.MLMaps;
import roart.common.ml.MLMapsPredictor;

public abstract class ActionComponentConfigMLPredictor extends ActionComponentConfigML {

    public MLMaps getMLMaps() {
        return new MLMapsPredictor();
    }

}

package roart.predictor.impl;

import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;

public abstract class TensorflowPredictor extends Predictor {

    public TensorflowPredictor(MyMyConfig conf, String string, int category) {
        super(conf, string, category);
    }
    
    @Override
    public String getType() {
        return MLConstants.TENSORFLOW;
    }
}

package roart.ml;

import java.util.List;
import java.util.Map;

import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public abstract class MLPredictTensorflowModel extends MLPredictModel {
    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
}

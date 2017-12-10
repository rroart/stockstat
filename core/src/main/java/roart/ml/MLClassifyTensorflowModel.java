package roart.ml;

import java.util.List;
import java.util.Map;

import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public abstract class MLClassifyTensorflowModel extends MLClassifyModel {
    @Override
    public String getEngineName() {
        return "Tensorflow";
    }
}

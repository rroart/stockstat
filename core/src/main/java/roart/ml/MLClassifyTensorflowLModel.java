package roart.ml;

import java.util.List;
import java.util.Map;

import roart.indicator.IndicatorMACD;
import roart.util.Constants;

public class MLClassifyTensorflowLModel  extends MLClassifyTensorflowModel {
    @Override
    public int getId() {
        return 2;
    }
    @Override
    public String getName() {
        return "L";
    }
}

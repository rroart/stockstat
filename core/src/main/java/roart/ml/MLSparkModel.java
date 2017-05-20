package roart.ml;

import java.util.List;
import java.util.Map;

import roart.util.Constants;

public abstract class MLSparkModel extends MLModel {
    public String getEngineName() {
        return "Spark ML";
    }
}

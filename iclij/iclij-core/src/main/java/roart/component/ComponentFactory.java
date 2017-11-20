package roart.component;

import roart.config.ConfigConstants;
import roart.pipeline.PipelineConstants;

public class ComponentFactory {
    public static Component factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new ComponentRecommender();
        case ConfigConstants.PREDICTORS:
            return new ComponentPredictor();
        case PipelineConstants.MLMACD:
            return new ComponentMLMACD();
        case PipelineConstants.MLINDICATOR:
            return new ComponentMLIndicator();
        default:
            return null;
        }
    }
}

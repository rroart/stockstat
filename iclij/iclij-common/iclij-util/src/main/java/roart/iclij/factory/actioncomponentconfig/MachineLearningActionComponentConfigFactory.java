package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.MachineLearningMLATRConfig;
import roart.iclij.model.config.MachineLearningMLCCIConfig;
import roart.iclij.model.config.MachineLearningMLIndicatorConfig;
import roart.iclij.model.config.MachineLearningMLMACDConfig;
import roart.iclij.model.config.MachineLearningMLMultiConfig;
import roart.iclij.model.config.MachineLearningMLRSIConfig;
import roart.iclij.model.config.MachineLearningMLSTOCHConfig;
import roart.iclij.model.config.MachineLearningPredictorConfig;
import roart.iclij.model.config.MachineLearningRecommenderConfig;
import roart.iclij.model.config.MachineLearningMLATRConfig;
import roart.iclij.model.config.MachineLearningMLCCIConfig;
import roart.iclij.model.config.MachineLearningMLIndicatorConfig;
import roart.iclij.model.config.MachineLearningMLMACDConfig;
import roart.iclij.model.config.MachineLearningMLMultiConfig;
import roart.iclij.model.config.MachineLearningMLRSIConfig;
import roart.iclij.model.config.MachineLearningMLSTOCHConfig;

public class MachineLearningActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new MachineLearningRecommenderConfig();
        case PipelineConstants.PREDICTOR:
            return new MachineLearningPredictorConfig();
        case PipelineConstants.MLMACD:
            return new MachineLearningMLMACDConfig();
        case PipelineConstants.MLRSI:
            return new MachineLearningMLRSIConfig();
        case PipelineConstants.MLATR:
            return new MachineLearningMLATRConfig();
        case PipelineConstants.MLCCI:
            return new MachineLearningMLCCIConfig();
        case PipelineConstants.MLSTOCH:
            return new MachineLearningMLSTOCHConfig();
        case PipelineConstants.MLMULTI:
            return new MachineLearningMLMultiConfig();
        case PipelineConstants.MLINDICATOR:
            return new MachineLearningMLIndicatorConfig();
        default:
            return null;
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new MachineLearningRecommenderConfig());
        list.add(new MachineLearningPredictorConfig());
        list.add(new MachineLearningMLMACDConfig());
        list.add(new MachineLearningMLRSIConfig());
        list.add(new MachineLearningMLATRConfig());
        list.add(new MachineLearningMLCCIConfig());
        list.add(new MachineLearningMLSTOCHConfig());
        list.add(new MachineLearningMLMultiConfig());
        list.add(new MachineLearningMLIndicatorConfig());
        return list;
    }


}

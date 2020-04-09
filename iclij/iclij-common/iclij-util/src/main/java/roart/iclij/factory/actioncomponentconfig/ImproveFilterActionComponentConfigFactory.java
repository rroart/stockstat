package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.ImproveFilterMLATRConfig;
import roart.iclij.model.config.ImproveFilterMLCCIConfig;
import roart.iclij.model.config.ImproveFilterMLIndicatorConfig;
import roart.iclij.model.config.ImproveFilterMLMACDConfig;
import roart.iclij.model.config.ImproveFilterMLMultiConfig;
import roart.iclij.model.config.ImproveFilterMLRSIConfig;
import roart.iclij.model.config.ImproveFilterMLSTOCHConfig;
import roart.iclij.model.config.ImproveFilterPredictorConfig;
import roart.iclij.model.config.ImproveFilterRecommenderConfig;

public class ImproveFilterActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new ImproveFilterRecommenderConfig();
        case PipelineConstants.PREDICTOR:
            return new ImproveFilterPredictorConfig();
        case PipelineConstants.MLMACD:
            return new ImproveFilterMLMACDConfig();
        case PipelineConstants.MLRSI:
            return new ImproveFilterMLRSIConfig();
        case PipelineConstants.MLATR:
            return new ImproveFilterMLATRConfig();
        case PipelineConstants.MLCCI:
            return new ImproveFilterMLCCIConfig();
        case PipelineConstants.MLSTOCH:
            return new ImproveFilterMLSTOCHConfig();
        case PipelineConstants.MLMULTI:
            return new ImproveFilterMLMultiConfig();
        case PipelineConstants.MLINDICATOR:
            return new ImproveFilterMLIndicatorConfig();
        default:
            return null;
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new ImproveFilterRecommenderConfig());
        list.add(new ImproveFilterPredictorConfig());
        list.add(new ImproveFilterMLMACDConfig());
        list.add(new ImproveFilterMLRSIConfig());
        list.add(new ImproveFilterMLATRConfig());
        list.add(new ImproveFilterMLCCIConfig());
        list.add(new ImproveFilterMLSTOCHConfig());
        list.add(new ImproveFilterMLMultiConfig());
        list.add(new ImproveFilterMLIndicatorConfig());
        return list;
    }


}

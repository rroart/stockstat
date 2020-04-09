package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.CrosstestMLATRConfig;
import roart.iclij.model.config.CrosstestMLCCIConfig;
import roart.iclij.model.config.CrosstestMLIndicatorConfig;
import roart.iclij.model.config.CrosstestMLMACDConfig;
import roart.iclij.model.config.CrosstestMLMultiConfig;
import roart.iclij.model.config.CrosstestMLRSIConfig;
import roart.iclij.model.config.CrosstestMLSTOCHConfig;
import roart.iclij.model.config.CrosstestPredictorConfig;
import roart.iclij.model.config.CrosstestRecommenderConfig;

public class CrossTestActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new CrosstestRecommenderConfig();
        case PipelineConstants.PREDICTOR:
            return new CrosstestPredictorConfig();
        case PipelineConstants.MLMACD:
            return new CrosstestMLMACDConfig();
        case PipelineConstants.MLRSI:
            return new CrosstestMLRSIConfig();
        case PipelineConstants.MLATR:
            return new CrosstestMLATRConfig();
        case PipelineConstants.MLCCI:
            return new CrosstestMLCCIConfig();
        case PipelineConstants.MLSTOCH:
            return new CrosstestMLSTOCHConfig();
        case PipelineConstants.MLMULTI:
            return new CrosstestMLMultiConfig();
        case PipelineConstants.MLINDICATOR:
            return new CrosstestMLIndicatorConfig();
        default:
            return null;
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new CrosstestRecommenderConfig());
        list.add(new CrosstestPredictorConfig());
        list.add(new CrosstestMLMACDConfig());
        list.add(new CrosstestMLRSIConfig());
        list.add(new CrosstestMLATRConfig());
        list.add(new CrosstestMLCCIConfig());
        list.add(new CrosstestMLSTOCHConfig());
        list.add(new CrosstestMLMultiConfig());
        list.add(new CrosstestMLIndicatorConfig());
        return list;
    }

}

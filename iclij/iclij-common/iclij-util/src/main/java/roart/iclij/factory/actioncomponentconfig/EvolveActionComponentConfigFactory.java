package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.EvolveMLATRConfig;
import roart.iclij.model.config.EvolveMLCCIConfig;
import roart.iclij.model.config.EvolveMLIndicatorConfig;
import roart.iclij.model.config.EvolveMLMACDConfig;
import roart.iclij.model.config.EvolveMLMultiConfig;
import roart.iclij.model.config.EvolveMLRSIConfig;
import roart.iclij.model.config.EvolveMLSTOCHConfig;
import roart.iclij.model.config.EvolvePredictorConfig;
import roart.iclij.model.config.EvolveRecommenderConfig;
import roart.iclij.model.config.EvolveMLATRConfig;
import roart.iclij.model.config.EvolveMLCCIConfig;
import roart.iclij.model.config.EvolveMLIndicatorConfig;
import roart.iclij.model.config.EvolveMLMACDConfig;
import roart.iclij.model.config.EvolveMLMultiConfig;
import roart.iclij.model.config.EvolveMLRSIConfig;
import roart.iclij.model.config.EvolveMLSTOCHConfig;

public class EvolveActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new EvolveRecommenderConfig();
        case PipelineConstants.PREDICTOR:
            return new EvolvePredictorConfig();
        case PipelineConstants.MLMACD:
            return new EvolveMLMACDConfig();
        case PipelineConstants.MLRSI:
            return new EvolveMLRSIConfig();
        case PipelineConstants.MLATR:
            return new EvolveMLATRConfig();
        case PipelineConstants.MLCCI:
            return new EvolveMLCCIConfig();
        case PipelineConstants.MLSTOCH:
            return new EvolveMLSTOCHConfig();
        case PipelineConstants.MLMULTI:
            return new EvolveMLMultiConfig();
        case PipelineConstants.MLINDICATOR:
            return new EvolveMLIndicatorConfig();
        default:
            return null;
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new EvolveRecommenderConfig());
        list.add(new EvolvePredictorConfig());
        list.add(new EvolveMLMACDConfig());
        list.add(new EvolveMLRSIConfig());
        list.add(new EvolveMLATRConfig());
        list.add(new EvolveMLCCIConfig());
        list.add(new EvolveMLSTOCHConfig());
        list.add(new EvolveMLMultiConfig());
        list.add(new EvolveMLIndicatorConfig());
        return list;
    }


}

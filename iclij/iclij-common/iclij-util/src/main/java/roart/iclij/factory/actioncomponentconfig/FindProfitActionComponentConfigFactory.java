package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.FindProfitMLATRConfig;
import roart.iclij.model.config.FindProfitMLCCIConfig;
import roart.iclij.model.config.FindProfitMLIndicatorConfig;
import roart.iclij.model.config.FindProfitMLMACDConfig;
import roart.iclij.model.config.FindProfitMLMultiConfig;
import roart.iclij.model.config.FindProfitMLRSIConfig;
import roart.iclij.model.config.FindProfitMLSTOCHConfig;
import roart.iclij.model.config.FindProfitPredictorConfig;
import roart.iclij.model.config.FindProfitRecommenderConfig;
import roart.iclij.model.config.FindProfitMLATRConfig;
import roart.iclij.model.config.FindProfitMLCCIConfig;
import roart.iclij.model.config.FindProfitMLIndicatorConfig;
import roart.iclij.model.config.FindProfitMLMACDConfig;
import roart.iclij.model.config.FindProfitMLMultiConfig;
import roart.iclij.model.config.FindProfitMLRSIConfig;
import roart.iclij.model.config.FindProfitMLSTOCHConfig;

public class FindProfitActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new FindProfitRecommenderConfig();
        case PipelineConstants.PREDICTOR:
            return new FindProfitPredictorConfig();
        case PipelineConstants.MLMACD:
            return new FindProfitMLMACDConfig();
        case PipelineConstants.MLRSI:
            return new FindProfitMLRSIConfig();
        case PipelineConstants.MLATR:
            return new FindProfitMLATRConfig();
        case PipelineConstants.MLCCI:
            return new FindProfitMLCCIConfig();
        case PipelineConstants.MLSTOCH:
            return new FindProfitMLSTOCHConfig();
        case PipelineConstants.MLMULTI:
            return new FindProfitMLMultiConfig();
        case PipelineConstants.MLINDICATOR:
            return new FindProfitMLIndicatorConfig();
        default:
            return null;
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new FindProfitRecommenderConfig());
        list.add(new FindProfitPredictorConfig());
        list.add(new FindProfitMLMACDConfig());
        list.add(new FindProfitMLRSIConfig());
        list.add(new FindProfitMLATRConfig());
        list.add(new FindProfitMLCCIConfig());
        list.add(new FindProfitMLSTOCHConfig());
        list.add(new FindProfitMLMultiConfig());
        list.add(new FindProfitMLIndicatorConfig());
        return list;
    }


}

package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.ImproveProfitMLATRConfig;
import roart.iclij.model.config.ImproveProfitMLCCIConfig;
import roart.iclij.model.config.ImproveProfitMLIndicatorConfig;
import roart.iclij.model.config.ImproveProfitMLMACDConfig;
import roart.iclij.model.config.ImproveProfitMLMultiConfig;
import roart.iclij.model.config.ImproveProfitMLRSIConfig;
import roart.iclij.model.config.ImproveProfitMLSTOCHConfig;
import roart.iclij.model.config.ImproveProfitPredictorConfig;
import roart.iclij.model.config.ImproveProfitRecommenderConfig;
import roart.iclij.model.config.ImproveProfitMLATRConfig;
import roart.iclij.model.config.ImproveProfitMLCCIConfig;
import roart.iclij.model.config.ImproveProfitMLIndicatorConfig;
import roart.iclij.model.config.ImproveProfitMLMACDConfig;
import roart.iclij.model.config.ImproveProfitMLMultiConfig;
import roart.iclij.model.config.ImproveProfitMLRSIConfig;
import roart.iclij.model.config.ImproveProfitMLSTOCHConfig;

public class ImproveProfitActionComponentConfigFactory extends ActionComponentConfigFactory{
    @Override
    public ActionComponentConfig factory(String component) {
        switch (component) {
        case PipelineConstants.AGGREGATORRECOMMENDERINDICATOR:
            return new ImproveProfitRecommenderConfig();
        case PipelineConstants.PREDICTOR:
            return new ImproveProfitPredictorConfig();
        case PipelineConstants.MLMACD:
            return new ImproveProfitMLMACDConfig();
        case PipelineConstants.MLRSI:
            return new ImproveProfitMLRSIConfig();
        case PipelineConstants.MLATR:
            return new ImproveProfitMLATRConfig();
        case PipelineConstants.MLCCI:
            return new ImproveProfitMLCCIConfig();
        case PipelineConstants.MLSTOCH:
            return new ImproveProfitMLSTOCHConfig();
        case PipelineConstants.MLMULTI:
            return new ImproveProfitMLMultiConfig();
        case PipelineConstants.MLINDICATOR:
            return new ImproveProfitMLIndicatorConfig();
        default:
            return null;
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new ImproveProfitRecommenderConfig());
        list.add(new ImproveProfitPredictorConfig());
        list.add(new ImproveProfitMLMACDConfig());
        list.add(new ImproveProfitMLRSIConfig());
        list.add(new ImproveProfitMLATRConfig());
        list.add(new ImproveProfitMLCCIConfig());
        list.add(new ImproveProfitMLSTOCHConfig());
        list.add(new ImproveProfitMLMultiConfig());
        list.add(new ImproveProfitMLIndicatorConfig());
        return list;
    }


}

package roart.iclij.factory.actioncomponentconfig;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.model.config.FilterConfig;
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
        default:
            return new FilterConfig();
        }
    }

    @Override
    public List<ActionComponentConfig> getAllComponents() {
        List<ActionComponentConfig> list = new ArrayList<>();
        list.add(new FilterConfig());
        return list;
    }


}

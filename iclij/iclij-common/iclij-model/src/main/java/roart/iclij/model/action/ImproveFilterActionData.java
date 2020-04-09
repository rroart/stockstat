package roart.iclij.model.action;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;

public class ImproveFilterActionData extends MarketActionData {

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getFiltertime();
    }
    
    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { false, true };
    }
    
    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getFindProfitThreshold();
    }

    @Override
    public String getName() {
        return IclijConstants.IMPROVEFILTER;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsImproveFilterRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsImproveFilterPredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsImproveFilterMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsImproveFilterMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsImproveFilterMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsImproveFilterMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsImproveFilterMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsImproveFilterMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
       if (config.wantsImproveFilterMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }
    
}

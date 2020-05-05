package roart.iclij.model.action;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class ImproveProfitActionData extends MarketActionData {

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getImprovetime();
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
        return IclijConstants.IMPROVEPROFIT;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsImproveProfitRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsImproveProfitPredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsImproveProfitMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsImproveProfitMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsImproveProfitMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsImproveProfitMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsImproveProfitMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsImproveProfitMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
       if (config.wantsImproveProfitMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }
    
    @Override
    public String getPriority() {
        return IclijConfigConstants.IMPROVEPROFIT;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
    }

}

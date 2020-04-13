package roart.iclij.model.action;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;

public class FindProfitActionData extends MarketActionData {

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getFindtime();
    }
    
    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { true, false };
    }
    
    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getFindProfitThreshold();
    }

    @Override
    public String getName() {
        return IclijConstants.FINDPROFIT;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsFindProfitRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsFindProfitPredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsFindProfitMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsFindProfitMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsFindProfitMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsFindProfitMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsFindProfitMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsFindProfitMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsFindProfitMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

}
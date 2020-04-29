package roart.iclij.model.action;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class EvolveActionData extends MarketActionData {

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getEvolvetime();
    }

    @Override
    public Boolean[] getBooleans() {
        //return new Boolean[] { false, true };
        return new Boolean[] { null };
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getEvolveThreshold();
    }
    
    @Override
    public String getName() {
        return IclijConstants.EVOLVE;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        if (config.wantsEvolveRecommender()) {
            components.add(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        }
        if (config.wantsEvolvePredictor()) {
            components.add(PipelineConstants.PREDICTOR);
        }
        if (config.wantsEvolveMLMACD()) {
            components.add(PipelineConstants.MLMACD);
        }
        if (config.wantsEvolveMLRSI()) {
            components.add(PipelineConstants.MLRSI);
        }
        if (wantThree) {
        if (config.wantsEvolveMLATR()) {
            components.add(PipelineConstants.MLATR);
        }
        if (config.wantsEvolveMLCCI()) {
            components.add(PipelineConstants.MLCCI);
        }
        if (config.wantsEvolveMLSTOCH()) {
            components.add(PipelineConstants.MLSTOCH);
        }
        }
        if (config.wantsEvolveMLMulti()) {
            components.add(PipelineConstants.MLMULTI);
        }
        if (config.wantsEvolveMLIndicator()) {
            components.add(PipelineConstants.MLINDICATOR);
        }
        return components;
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.EVOLVE;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getEvolveFuturedays();
    }

}

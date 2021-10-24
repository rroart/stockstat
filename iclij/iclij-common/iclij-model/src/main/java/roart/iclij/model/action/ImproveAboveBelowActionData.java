package roart.iclij.model.action;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class ImproveAboveBelowActionData extends MarketActionData {

    @Override
    public String getName() {
        return IclijConstants.IMPROVEABOVEBELOW;
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getFindProfitThreshold();
    }

    @Override
    public Short getTime(Market market) {
        Short time = market.getConfig().getAbovebelowtime();
        if (time != null) {
            return time;
        } else {
            return market.getConfig().getFindtime();
        }
    }

    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { null };
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        components.add(PipelineConstants.ABOVEBELOW);
        return components;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.IMPROVEABOVEBELOW;
    }

    @Override
    public boolean wantsUpdate(IclijConfig config) {
        return true;
    }
    
    @Override
    public String getEvolutionConfig(IclijConfig config) {
        return config.getImproveAbovebelowEvolutionConfig();
    }
	
    @Override
	public boolean isEvolving() {
		return true;
	}
	
    @Override
	public boolean doSaveTiming() {
		return false;
	}
}

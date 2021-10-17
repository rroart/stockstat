package roart.iclij.model.action;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class DatasetActionData extends MarketActionData {

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getDatasettime();
    }

    @Override
    public Boolean[] getBooleans() {
        //return new Boolean[] { false, true };
        return new Boolean[] { null };
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return "[ null ]";
    }

    @Override
    public boolean isDataset() {
        return true;
    }
    
    @Override
    public String getName() {
        return IclijConstants.DATASET;
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        components.add(PipelineConstants.DATASET);
        return components;
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.DATASET;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return "[ null ]";
    }

    @Override
    public boolean wantsUpdate(IclijConfig config) {
        return config.wantsDatasetUpdate();
    }

    @Override
	public String getEvolutionConfig(IclijConfig config) {
		return config.getDatasetEvolutionConfig();
	}

    @Override
	public String getMLConfig(IclijConfig config) {
		return config.getDatasetMLConfig();
	}
}

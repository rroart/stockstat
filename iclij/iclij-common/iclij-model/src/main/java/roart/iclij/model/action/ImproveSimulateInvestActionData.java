package roart.iclij.model.action;

import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class ImproveSimulateInvestActionData extends MarketActionData {

    @Override
    public String getName() {
        return IclijConstants.IMPROVESIMULATEINVEST;
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getFindProfitThreshold();
    }

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getFindtime();
    }

    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { null };
    }

    @Override
    public List<String> getComponents(IclijConfig config, boolean wantThree) {
        List<String> components = new ArrayList<>();
        components.add(PipelineConstants.IMPROVESIMULATEINVEST);
        return components;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.IMPROVESIMULATEINVEST;
    }

}

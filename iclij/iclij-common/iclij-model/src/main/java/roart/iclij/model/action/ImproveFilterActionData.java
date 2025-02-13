package roart.iclij.model.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class ImproveFilterActionData extends MarketActionData {

    public ImproveFilterActionData(IclijConfig iclijConfig) {
        super(iclijConfig);
    }

    @Override
    public Short getTime(Market market) {
        Short time = market.getConfig().getFiltertime();
        if (time != null) {
            return time;
        } else {
            return market.getConfig().getFindtime();
        }
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
        components.add(PipelineConstants.FILTER);
        return components;
    }
    
    @Override
    public String getPriority() {
        return IclijConfigConstants.IMPROVEFILTER;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
    }

    @Override
    public String getParamDateFromConfig(Market market, List<String> stockDates) {
        String date = getMlDate(market, stockDates);
        if (date == null) {
            date = getMlDays(market, stockDates);
        }
        return date;
    }

    public String getMlDate(Market market, List<String> stockDates) {
        String date = market.getConfig().getMldate();
        if (date != null) {
            LocalDate adate = TimeUtil.getEqualBefore(stockDates, date);
            date = TimeUtil.convertDate2(adate);
        }
        return date;
    }

    public String getMlDays(Market market, List<String> stockDates) {
        Short days = market.getConfig().getMldays();
        if (days != null) {
            return stockDates.get(stockDates.size() - 1 - days);
        }
        return null;
    }

    @Override
    public boolean wantsUpdate(IclijConfig config) {
        return false;
    }

    @Override
    public String getEvolutionConfig(IclijConfig config) {
        return config.getImproveAbovebelowEvolutionConfig();
    }
    
    @Override
	public boolean doHandleMLMeta() {
		return true;
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

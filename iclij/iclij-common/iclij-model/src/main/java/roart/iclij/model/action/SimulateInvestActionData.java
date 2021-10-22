package roart.iclij.model.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;

public class SimulateInvestActionData extends MarketActionData {

    @Override
    public String getName() {
        return IclijConstants.SIMULATEINVEST;
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
        components.add(PipelineConstants.SIMULATEINVEST);
        return components;
    }

    @Override
    public String getFuturedays(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
    }

    @Override
    public String getPriority() {
        return IclijConfigConstants.SIMULATEINVEST;
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
    public Object[] getScoreDescription(Object[] accuracy, Map<String, Object> scoreMap) {
        Double score = null;
        String description = null;
        try {
            score = scoreMap
                    .values()
                    .stream()
                    .mapToDouble(e -> (Double) e)
                    .max()
                    .orElse(-1);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new Object[] { score, description };
    }
}

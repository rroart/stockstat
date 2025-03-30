package roart.iclij.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import roart.common.util.JsonUtil;

public class SimulateInvestUtils {
    public static SimulateInvestConfig getSimConfig(IclijConfig config) {
        if (config.getConfigData().getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTDELAY) == null || (int) config.getConfigData().getConfigValueMap().get(IclijConfigConstants.SIMULATEINVESTDELAY) == 0) {
            return null;
        }
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        simConfig.setAdviser(config.getSimulateInvestAdviser());
        simConfig.setBuyweight(config.wantsSimulateInvestBuyweight());
        simConfig.setConfidence(config.wantsSimulateInvestConfidence());
        simConfig.setConfidenceValue(config.getSimulateInvestConfidenceValue());
        simConfig.setConfidenceFindTimes(config.getSimulateInvestConfidenceFindtimes());
        simConfig.setAbovebelow(config.getSimulateInvestAboveBelow());
        simConfig.setConfidenceholdincrease(config.wantsSimulateInvestConfidenceHoldIncrease());
        simConfig.setNoconfidenceholdincrease(config.wantsSimulateInvestNoConfidenceHoldIncrease());
        simConfig.setConfidencetrendincrease(config.wantsSimulateInvestConfidenceTrendIncrease());
        simConfig.setConfidencetrendincreaseTimes(config.wantsSimulateInvestConfidenceTrendIncreaseTimes());
        simConfig.setNoconfidencetrenddecrease(config.wantsSimulateInvestNoConfidenceTrendDecrease());
        simConfig.setNoconfidencetrenddecreaseTimes(config.wantsSimulateInvestNoConfidenceTrendDecreaseTimes());
        try {
        simConfig.setImproveFilters(config.getSimulateInvestImproveFilters());
        } catch (Exception e) {
            int jj = 0;
        }
        simConfig.setInterval(config.getSimulateInvestInterval());
        simConfig.setIndicatorPure(config.wantsSimulateInvestIndicatorPure());
        simConfig.setIndicatorRebase(config.wantsSimulateInvestIndicatorRebase());
        simConfig.setIndicatorReverse(config.wantsSimulateInvestIndicatorReverse());
        simConfig.setIndicatorDirection(config.wantsSimulateInvestIndicatorDirection());
        simConfig.setIndicatorDirectionUp(config.wantsSimulateInvestIndicatorDirectionUp());
        simConfig.setMldate(config.wantsSimulateInvestMLDate());
        try {
            simConfig.setPeriod(config.getSimulateInvestPeriod());
        } catch (Exception e) {
            int jj = 0;
        }
        simConfig.setStoploss(config.wantsSimulateInvestStoploss());
        simConfig.setStoplossValue(config.getSimulateInvestStoplossValue());
        simConfig.setIntervalStoploss(config.wantsSimulateInvestIntervalStoploss());
        simConfig.setIntervalStoplossValue(config.getSimulateInvestIntervalStoplossValue());
        simConfig.setStocks(config.getSimulateInvestStocks());
        simConfig.setInterpolate(config.wantsSimulateInvestInterpolate());
        simConfig.setDay(config.getSimulateInvestDay());
        simConfig.setDelay(config.getSimulateInvestDelay());
        try {
        simConfig.setFuturecount(config.getSimulateInvestFutureCount());
        simConfig.setFuturetime(config.getSimulateInvestFutureTime());
        } catch (Exception e) {

        }
        Map<String, Double> map = JsonUtil.convert(config.getSimulateInvestVolumelimits(), Map.class);
        simConfig.setVolumelimits(map);

        SimulateFilter[] array = JsonUtil.convert(config.getSimulateInvestFilters(), SimulateFilter[].class);
        List<SimulateFilter> list = null;
        if (array != null) {
            list = Arrays.asList(array);
        }
        simConfig.setFilters(list);
        try {
            simConfig.setEnddate(config.getSimulateInvestEnddate());
        } catch (Exception e) {

        }
        try {
            simConfig.setStartdate(config.getSimulateInvestStartdate());
        } catch (Exception e) {

        }
        return simConfig;
    }

 
}

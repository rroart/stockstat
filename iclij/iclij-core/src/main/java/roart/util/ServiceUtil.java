package roart.util;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.FindProfitAction;
import roart.action.ImproveAboveBelowAction;
import roart.action.ImproveFilterAction;
import roart.action.ImproveProfitAction;
import roart.action.ImproveSimulateInvestAction;
import roart.action.MarketAction;
import roart.action.SimulateInvestAction;
import roart.common.constants.Constants;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.iclij.config.Market;
import roart.iclij.model.TimingItem;
import roart.iclij.model.WebData;
import roart.iclij.model.WebDataJson;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceResult;
import roart.iclij.util.MarketUtil;

public class ServiceUtil {
    private static Logger log = LoggerFactory.getLogger(ServiceUtil.class);

    public static IclijServiceResult getVerify(ComponentInput componentInput) {
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            int verificationdays = componentInput.getConfig().verificationDays();
            param = ComponentData.getParam(componentInput, verificationdays);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        FindProfitAction findProfitAction = new FindProfitAction();
        Market market = new MarketUtil().findMarket(param.getInput().getMarket());
        WebData webData = findProfitAction.getVerifyMarket(componentInput, param, market, false, componentInput.getConfig().verificationDays());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    private static WebDataJson convert(WebData webData) {
        WebDataJson webDataJson = new WebDataJson();
        webDataJson.setDecs(webData.getDecs());
        webDataJson.setIncs(webData.getIncs());
        webDataJson.setMemoryItems(webData.getMemoryItems());
        webDataJson.setTimingMap(webData.getTimingMap());
        webDataJson.setUpdateMap(webData.getUpdateMap());
        return webDataJson;
    }

    public static IclijServiceResult getFindProfit(ComponentInput componentInput, List<TimingItem> timingList) {
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction findProfitAction = new FindProfitAction();
        Market market = new MarketUtil().findMarket(param.getInput().getMarket());
        WebData webData = findProfitAction.getMarket(null, param, market, null, null, timingList);        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveAboveBelow(ComponentInput componentInput) {
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction improveAboveBelowAction = new ImproveAboveBelowAction();
        Market market = new MarketUtil().findMarket(param.getInput().getMarket());
        WebData webData = improveAboveBelowAction.getMarket(null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveFilter(ComponentInput componentInput) {
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction improveFilterAction = new ImproveFilterAction();
        Market market = new MarketUtil().findMarket(param.getInput().getMarket());
        WebData webData = improveFilterAction.getMarket(null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveProfit(ComponentInput componentInput) {
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction improveProfitAction = new ImproveProfitAction();
        Market market = new MarketUtil().findMarket(param.getInput().getMarket());
        WebData webData = improveProfitAction.getMarket(null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getSimulateInvest(ComponentInput componentInput) {
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction simulateInvestAction = new SimulateInvestAction();
        Market market = new MarketUtil().findMarket(param.getInput().getMarket());
        WebData webData = simulateInvestAction.getMarket(null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveSimulateInvest(ComponentInput componentInput) {
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(componentInput, 0);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction simulateInvestAction = new ImproveSimulateInvestAction();
        Market market = new MarketUtil().findMarket(param.getInput().getMarket());
        WebData webData = simulateInvestAction.getMarket(null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

}

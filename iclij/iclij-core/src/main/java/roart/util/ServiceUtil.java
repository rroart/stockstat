package roart.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.action.ActionThread;
import roart.action.FindProfitAction;
import roart.action.ImproveAboveBelowAction;
import roart.action.ImproveAutoSimulateInvestAction;
import roart.action.ImproveFilterAction;
import roart.action.ImproveProfitAction;
import roart.action.ImproveSimulateInvestAction;
import roart.action.MarketAction;
import roart.action.SimulateInvest2Action;
import roart.action.SimulateInvestAction;
import roart.common.constants.Constants;
import roart.common.model.ActionComponentItem;
import roart.common.model.MetaItem;
import roart.common.model.TimingItem;
import roart.common.model.util.MetaUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.model.WebData;
import roart.iclij.model.WebDataJson;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceResult;
import roart.iclij.service.util.MarketUtil;
import roart.model.io.IO;

public class ServiceUtil {
    private static Logger log = LoggerFactory.getLogger(ServiceUtil.class);

    @Deprecated
    public static IclijServiceResult getVerify(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            int verificationdays = iclijConfig.verificationDays();
            param = ComponentData.getParam(iclijConfig, componentInput, verificationdays, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        FindProfitAction findProfitAction = new FindProfitAction(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        // TODO getcontent
        WebData webData = findProfitAction.getVerifyMarket(componentInput, param, market, false, iclijConfig.verificationDays());        
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

    public static IclijServiceResult getFindProfit(ComponentInput componentInput, List<TimingItem> timingList, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction findProfitAction = new FindProfitAction(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        WebData webData = findProfitAction.getMarket(iclijConfig, null, param, market, null, null, timingList);        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveAboveBelow(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction improveAboveBelowAction = new ImproveAboveBelowAction(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        WebData webData = improveAboveBelowAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveFilter(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction improveFilterAction = new ImproveFilterAction(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        WebData webData = improveFilterAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveProfit(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction improveProfitAction = new ImproveProfitAction(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        WebData webData = improveProfitAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getSimulateInvest(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction simulateInvestAction = new SimulateInvestAction(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        LocalDate date = null;
        try {
            if (param.getConfig().getSimulateInvestEnddate() != null) {
                date = TimeUtil.convertDate(TimeUtil.replace(param.getConfig().getSimulateInvestEnddate()));
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            
        }
        //param.getService().conf.setdate(date);
        param.getInput().setEnddate(date);
        WebData webData = simulateInvestAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getSimulateInvest2(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction simulateInvestAction = new SimulateInvest2Action(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        LocalDate date = null;
        try {
            if (param.getConfig().getSimulateInvestEnddate() != null) {
                date = TimeUtil.convertDate(TimeUtil.replace(param.getConfig().getSimulateInvestEnddate()));
            }
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            
        }
        //param.getService().conf.setdate(date);
        param.getInput().setEnddate(date);
        WebData webData = simulateInvestAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getAutoSimulateInvest(IclijConfig iclijConfig, ComponentInput componentInput, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction simulateInvestAction = new SimulateInvestAction(iclijConfig);
        Market market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        param.setMarket(market);
        WebData webData = simulateInvestAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveSimulateInvest(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction simulateInvestAction = new ImproveSimulateInvestAction(iclijConfig);
        Market market = null;
        if (param.getInput().getMarket() != null) {
            market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        }
        param.setMarket(market);
        WebData webData = simulateInvestAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    public static IclijServiceResult getImproveAutoSimulateInvest(ComponentInput componentInput, IclijConfig iclijConfig, IO io) {
        actionThreadReady();
        IclijServiceResult result = new IclijServiceResult();
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            return result;
        }

        MarketAction simulateInvestAction = new ImproveAutoSimulateInvestAction(iclijConfig);
        Market market = null;
        if (param.getInput().getMarket() != null) {
            market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        }
        param.setMarket(market);
        WebData webData = simulateInvestAction.getMarket(iclijConfig, null, param, market, null, null, new ArrayList<>());        
        //Inmemory inmemory = io.getInmemoryFactory().get(iclijConfig.getInmemoryServer(), iclijConfig.getInmemoryHazelcast(), iclijConfig.getInmemoryRedis());
        //new PipelineThreadUtils(iclijConfig, inmemory, io.getCuratorClient()).cleanPipeline(param.getService().id, param.getId());
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    private static void actionThreadReady() {
        while (ActionThread.isUpdateDb()) {
            try {
                TimeUnit.SECONDS.sleep(60);
            } catch (InterruptedException e) {
                log.error(Constants.EXCEPTION, e);
                Thread.currentThread().interrupt();
            }
        }
    }

    public static IclijServiceResult getImproveAutoSimulateInvestNotYet(IclijConfig iclijConfig, ComponentInput componentInput, IO io) throws Exception {
        IclijServiceResult result = new IclijServiceResult();
        MarketAction simulateInvestAction = new ImproveAutoSimulateInvestAction(iclijConfig);
        BlockingQueue<WebData> queue = handleInputToActionThreadQueue(iclijConfig, componentInput, simulateInvestAction, io);
        WebData webData = queue.take();
        WebDataJson webDataJson = convert(webData);
        result.setWebdatajson(webDataJson);
        return result;
    }

    // not yet
    private static BlockingQueue<WebData> handleInputToActionThreadQueue(IclijConfig iclijConfig,
            ComponentInput componentInput, MarketAction action, IO io) throws Exception {
        ComponentData param = null;
        try {
            param = ComponentData.getParam(iclijConfig, componentInput, 0, io);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
            throw new Exception(e);
        }
        Market market = null;
        if (param.getInput().getMarket() != null) {
            market = new MarketUtil().findMarket(param.getInput().getMarket(), iclijConfig);
        }
        param.setMarket(market);
        String marketName = componentInput.getMarket();
        List<MetaItem> metas = param.getService().getMetas();
        MetaItem meta = new MetaUtil().findMeta(metas, market.getConfig().getMarket());
        boolean wantThree = meta != null && Boolean.TRUE.equals(meta.isLhc());
        List<String> components = action.getActionData().getComponents(iclijConfig, wantThree);
        BlockingQueue<WebData> queue = null;
        // currently only suited and tested for actions with one component
        for (String component : components) {
            ActionComponentItem marketTime = new ActionComponentItem();
            queue = new ArrayBlockingQueue<>(1);
            marketTime.setMarket(market.getConfig().getMarket());
            marketTime.setAction(action.getActionData().getName());
            marketTime.setComponent(component);
            marketTime.setPriority(-20);
            List<ActionComponentItem> marketTimes = new ArrayList<>();
            marketTimes.add(marketTime);
            marketTime.setResult(queue);
            ActionThread.queue.addAll(marketTimes);
        }
        return queue;
    }
    
}

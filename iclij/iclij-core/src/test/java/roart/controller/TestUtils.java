package roart.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import roart.common.cache.MyCache;
import roart.common.inmemory.model.Inmemory;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.iclij.config.AutoSimulateInvestConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.SimulateInvestConfig;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.IclijServiceResult;
import roart.model.io.IO;
import roart.util.ServiceUtil;

public class TestUtils {

    private IclijConfig iconf;
    private IO io;

    public TestUtils(IclijConfig iconf, IO io) {
        super();
        this.iconf = iconf;
        this.io = io;
    }

    IclijServiceResult getSimulateInvestMarket(SimulateInvestConfig simConfig, String market) {
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iconf.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        return ServiceUtil.getSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), myConfig, io);
    }

    IclijServiceResult getSimulateInvestMarketDbid(SimulateInvestConfig simConfig, String market, String dbid) {
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iconf.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTDBID, dbid);
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTDELAY, 1);
        return ServiceUtil.getSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), myConfig, io);
    }

    IclijServiceResult getSimulateInvestRunMarket(SimulateInvestConfig simConfig, String market) {
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iconf.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        return ServiceUtil.getSimulateInvestRun(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), myConfig, io);
    }

    public IclijServiceResult getImproveSimulateInvest(String market, SimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iconf.copy();
        Map<String, Object> map = simConfig.asValuedMap();
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTSTARTDATE, simConfig.getStartdate());
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTENDDATE, simConfig.getEnddate());
        map.remove(IclijConfigConstants.SIMULATEINVESTSTARTDATE);
        map.remove(IclijConfigConstants.SIMULATEINVESTENDDATE);
        if (simConfig.getGa() != null) {
            int ga = simConfig.getGa();
            simConfig.setGa(null);
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        /*
        config.getConfigValueMap().putAll(map);
        if (simConfig.getAdviser() != null) {
            int adviser = simConfig.getAdviser();
            simConfig.setAdviser(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTADVISER, adviser);
        }
        if (simConfig.getIndicatorPure() != null) {
            boolean adviser = simConfig.getIndicatorPure();
            simConfig.setIndicatorPure(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTINDICATORPURE, adviser);
        }
         */
        return ServiceUtil.getImproveSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), map), myConfig, io);
    }

    public IclijServiceResult getAutoSimulateInvestMarket(String market, AutoSimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        Map<String, Object> map = simConfig.asMap();
        IclijConfig myConfig = iconf.copy();
        myConfig.getConfigData().getConfigValueMap().putAll(map);
        return ServiceUtil.getAutoSimulateInvest(myConfig, new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), new HashMap<>()), io);
    }

    public IclijServiceResult getImproveAutoSimulateInvest(String market, AutoSimulateInvestConfig simConfig)
            throws Exception {
        //MainAction.goals.add(new ImproveProfitAction());
        //int result = new ImproveProfitAction().goal(param.getIclijConfig(), );
        if ("null".equals(market) || "None".equals(market)) {
            market = null;
        }
        IclijConfig myConfig = iconf.copy();
        Map<String, Object> map = simConfig.asValuedMap();
        //myConfig.getConfigData().unmute();
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE, simConfig.getStartdate());
        myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE, simConfig.getEnddate());
        map.remove(IclijConfigConstants.AUTOSIMULATEINVESTSTARTDATE);
        map.remove(IclijConfigConstants.AUTOSIMULATEINVESTENDDATE);
        if (simConfig.getGa() != null) {
            int ga = simConfig.getGa();
            simConfig.setGa(null);
            myConfig.getConfigData().getConfigValueMap().put(IclijConfigConstants.EVOLVEGA, ga);
        }
        //myConfig.getConfigData().mute();
        /*
        config.getConfigValueMap().putAll(map);
        if (simConfig.getAdviser() != null) {
            int adviser = simConfig.getAdviser();
            simConfig.setAdviser(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTADVISER, adviser);
        }
        if (simConfig.getIndicatorPure() != null) {
            boolean adviser = simConfig.getIndicatorPure();
            simConfig.setIndicatorPure(null);
            config.getConfigValueMap().put(IclijConfigConstants.SIMULATEINVESTINDICATORPURE, adviser);
        }
         */
        return ServiceUtil.getImproveAutoSimulateInvest(new ComponentInput(myConfig.getConfigData(), null, market, null, null, false, false, new ArrayList<>(), map), myConfig, io);
    }

     SimulateInvestConfig getSimConfigDefault() {
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        simConfig.setConfidence(false);
        simConfig.setConfidenceValue(0.7);
        simConfig.setConfidenceFindTimes(4);
        simConfig.setStoploss(true);
        simConfig.setStoplossValue(0.9);
        simConfig.setIndicatorPure(false);
        simConfig.setIndicatorRebase(false);
        simConfig.setIndicatorReverse(false);
        simConfig.setMldate(false);
        simConfig.setStocks(5);
        simConfig.setBuyweight(false);
        simConfig.setInterval(7);
        simConfig.setAdviser(1);
        simConfig.setPeriod(0);
        simConfig.setInterpolate(false);
        simConfig.setIntervalStoploss(true);
        simConfig.setIntervalStoplossValue(0.9);
        simConfig.setDay(1);
        simConfig.setDelay(1);
        simConfig.setIntervalwhole(false);
        simConfig.setConfidenceholdincrease(false);
        simConfig.setNoconfidenceholdincrease(true);
        simConfig.setNoconfidencetrenddecrease(false);
        simConfig.setNoconfidencetrenddecreaseTimes(1);
        simConfig.setConfidencetrendincrease(false);
        simConfig.setConfidencetrendincreaseTimes(1);
        simConfig.setIndicatorDirection(false);
        simConfig.setIndicatorDirectionUp(true);
        simConfig.setVolumelimits(null);
        simConfig.setAbovebelow(false);
        return simConfig;
    }

    SimulateInvestConfig getImproveSimConfigDefault() {
        SimulateInvestConfig simConfig = new SimulateInvestConfig();
        simConfig.setGa(0);
        simConfig.setIndicatorPure(null);
        simConfig.setIndicatorReverse(null);
        simConfig.setStocks(null);
        simConfig.setBuyweight(false);
        simConfig.setInterval(null);
        simConfig.setAdviser(null);
        simConfig.setPeriod(0);
        simConfig.setDelay(1);
        simConfig.setIntervalwhole(true);
        simConfig.setVolumelimits(null);
        simConfig.setFuturecount(0);
        simConfig.setFuturetime(0);
        simConfig.setImproveFilters(false);
        return simConfig;
    }

    AutoSimulateInvestConfig getAutoSimConfigDefault() {
        AutoSimulateInvestConfig simConfig = new AutoSimulateInvestConfig();
        simConfig.setInterval(1);
        simConfig.setPeriod(0);
        simConfig.setLastcount(5);
        simConfig.setDellimit(0.5);
        simConfig.setScorelimit(1.0);
        simConfig.setAutoscorelimit(0.0);
        simConfig.setIntervalwhole(false);        
        simConfig.setFilters(null);
        simConfig.setVolumelimits(null);
        simConfig.setVote(false);
        simConfig.setKeepAdviser(false);
        simConfig.setKeepAdviserLimit(0.0);
        return simConfig;
    }

    AutoSimulateInvestConfig getImproveAutoSimConfigDefault() {
        AutoSimulateInvestConfig simConfig = new AutoSimulateInvestConfig();
        // TODO simConfig.setStocks(5);
        simConfig.setIntervalwhole(false);
        
        simConfig.setGa(0);
        simConfig.setFuturecount(0);
        simConfig.setFuturetime(0);
        simConfig.setImproveFilters(false);
        simConfig.setFilters(null);
        simConfig.setVolumelimits(null);
        simConfig.setVote(false);
        return simConfig;
    }
    
    public void cacheinvalidate()
            throws Exception {
        MyCache.getInstance().invalidate();          
    }

    public void deletepipeline(String controlId)
            throws Exception {
        Inmemory inmemory = io.getInmemoryFactory().get(iconf.getInmemoryServer(), iconf.getInmemoryHazelcast(), iconf.getInmemoryRedis());
        for (String id : MyCache.getInstance().pipeline()) {
            new PipelineThreadUtils(iconf, inmemory, io.getCuratorClient()).cleanPipeline(controlId, id);            
        }
    }
}

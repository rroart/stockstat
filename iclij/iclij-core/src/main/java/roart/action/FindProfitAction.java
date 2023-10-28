package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.model.ActionComponentItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.iclij.component.Component;
import roart.component.model.ComponentData;
import roart.component.model.ComponentTimeUtil;
import roart.db.dao.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.FindProfitActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.util.MarketUtil;
import roart.iclij.util.MiscUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class FindProfitAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public FindProfitAction(IclijConfig iclijConfig, IclijDbDao dbDao) {
        setActionData(new FindProfitActionData(iclijConfig, dbDao));
    }
    
    //@Override
    public List<MemoryItem> getMarketMemory2(Market market) {
        return new ArrayList<>();
    }

    //@Override
    public List<MemoryItem> filterKeepRecent2(List<MemoryItem> marketMemory, LocalDate date, int days) {
        return marketMemory;
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String componentName = entry.getKey();
            Component component = componentMap.get(componentName);
            if (component == null) {
                continue;
            }
            Memories positions = listComponent;
            /*
            param = dataMap.get(componentName);
            if (param == null) {
                log.error("Component {} not found", componentName);
                continue;
            }
            */
            // not yet, will need to use memoryitem data without pos and md
            //component.enableDisable(param, positions, param.getConfigValueMap(), buy);

            String mlmarket = market.getConfig().getMlmarket();
            param.getService().conf.getConfigData().setMlmarket(mlmarket);

            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCROSS, false);

            String key = component.getThreshold();
            aMap.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            aMap.put(key2, parameters.getFuturedays());
            
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(getActionData(), market, param, profitdata, positions, evolve, aMap, subcomponent, null, parameters, getParent() != null);
            component.calculateIncDec(componentData, profitdata, positions, buy, mlTests, parameters);
            if (param.getInput().isDoSave()) {
                IncDecItem myitem = null;
                try {
                    for (IncDecItem item : profitdata.getBuys().values()) {
                        myitem = item;
                        getActionData().getDbDao().save(item);
                        System.out.println(item);
                    }
                    for (IncDecItem item : profitdata.getSells().values()) {
                        myitem = item;
                        getActionData().getDbDao().save(item);
                        System.out.println(item);
                    }
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                    log.error("Could not save {}", myitem);
                }
            }
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }
        myData.getIncs().addAll(profitdata.getBuys().values());
        myData.getDecs().addAll(profitdata.getSells().values());
    }

    public List<MemoryItem> findAllMarketComponentsToCheck(WebData myData, ComponentData param, int days, IclijConfig config, ActionComponentItem marketTime, boolean evolve, Map<String, ComponentData> dataMap, Map<String, Component> componentMap) {
        List<MemoryItem> allMemories = new ArrayList<>();
        Market market = new MarketUtil().findMarket(marketTime.getMarket(), getActionData().getIclijConfig());
        Short startOffset = market.getConfig().getStartoffset();
        if (startOffset != null) {
            System.out.println("Using offset " + startOffset);
            log.info("Using offset {}", startOffset);
            days += startOffset;
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            long time0 = System.currentTimeMillis();
            //Market market = FindProfitAction.findMarket(componentparam);
            ProfitData profitdata = new ProfitData();
            evolve = false; // param.getInput().getConfig().wantEvolveML();
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCROSS, false);

            Parameters parameters = JsonUtil.convert(marketTime.getParameters(), Parameters.class);
            String key = component.getThreshold();
            aMap.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            aMap.put(key2, parameters.getFuturedays());
                        
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(getActionData(), market, param, profitdata, new Memories(market), evolve, aMap, marketTime.getSubcomponent(), null, parameters, getParent() != null);
            dataMap.put(entry.getKey(), componentData);
            componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(getActionData(), componentData, parameters);
                allMemories.addAll(memories);
           } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
         }
        return allMemories;
    }

    @Override
    protected List<IncDecItem> getIncDecItems() {
        List<IncDecItem> incdecitems = null;
        try {
            incdecitems = getActionData().getDbDao().getAllIncDecs();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return incdecitems;
    }
    
    @Override
    protected List getAnArray() {
        return new ArrayList<>();
    }
    
    @Override
    protected Boolean getBool() {
        return true;
    }
    
    @Override
    protected boolean getEvolve(Component component, ComponentData param) {
        return component.wantEvolve(param.getConfig());
    }
    
    @Override
    protected List<MemoryItem> getMemItems(ActionComponentItem marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        Market market = new MarketUtil().findMarket(marketTime.getMarket(), getActionData().getIclijConfig());
        
        try {
            evolve = false;
            //List<MemoryItem> newMemories = findAllMarketComponentsToCheck(myData, param, 0, config, marketTime, evolve, dataMap, componentMap);
            LocalDate prevdate = getPrevDate(param, market);
            LocalDate olddate = prevdate.minusDays(((int) MarketAction.AVERAGE_SIZE) * getActionData().getTime(market));
            List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(market, getName(), marketTime.getComponent(), marketTime.getSubcomponent(), JsonUtil.convert(marketTime.getParameters()), olddate, prevdate, getActionData().getDbDao());
            return marketMemory;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }
    
    @Override
    public LocalDate getPrevDate(ComponentData param, Market market) {
        return param.getInput().getEnddate();
    }
    
    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetCategoryValueMap();
    }

    public WebData getVerifyMarket(ComponentInput componentInput, ComponentData param,
            Market market, boolean evolve, int verificationdays) {
        WebData myData;
        myData = new WebData();
        myData.setIncs(new ArrayList<>());
        myData.setDecs(new ArrayList<>());
        myData.setUpdateMap(new HashMap<>());
        myData.setTimingMap(new HashMap<>());
        myData.setUpdateMap2(new HashMap<>());
        myData.setTimingMap2(new HashMap<>());
        myData.setMemoryItems(new ArrayList<>());
        /*
        MarketComponentTime marketTime = new MarketComponentTime();
        marketTime.market = market;
        marketTime.componentName = null;
        marketTime.component = null;
        marketTime.subcomponent = null;
        marketTime.parameters = null;
        //marketTime.timings = timingToDo;
        marketTime.buy = null;
        */
        Map<String, ComponentData> dataMap = new HashMap<>();
        Memories listComponentMap = new Memories(market);
        myData.setMemoryItems(new ArrayList<>());
        LocalDate prevdate = getPrevDate(param, market);
        int offset = new ComponentTimeUtil().getFindProfitOffset(market, param.getInput());
        short startoffset = new MarketUtil().getStartoffset(market);
        prevdate = prevdate.plusDays(offset);
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        prevdate = TimeUtil.getBackEqualBefore2(prevdate, verificationdays + startoffset, stockDates);
        //prevdate = prevdate.minusDays(verificationdays + startoffset);
        LocalDate olddate = prevdate.minusDays(getActionData().getTime(market));
        ProfitData profitdata = new ProfitData();
        ProfitInputData inputdata = new ProfitInputData();
        getListComponents(myData, param, param.getConfig(), null, evolve, market, dataMap, listComponentMap, olddate, prevdate);
        profitdata.setInputdata(inputdata);
        myData.setProfitData(profitdata);

        List<IncDecItem> incdecitems = null;
        try {
            incdecitems = getActionData().getDbDao().getAllIncDecs(market.getConfig().getMarket(), olddate, prevdate, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        new MarketUtil().fillProfitdata(profitdata, incdecitems);
        
        setValMap(param);
        PipelineData[] maps = param.getResultMaps();
        new MarketUtil().filterIncDecs(param, market, profitdata, maps, true, null);
        new MarketUtil().filterIncDecs(param, market, profitdata, maps, false, null);
        myData.getIncs().addAll(profitdata.getBuys().values());
        myData.getDecs().addAll(profitdata.getSells().values());
        return myData;
    }

    @Override
    protected boolean getSkipComponent(List<MLMetricsItem> mltests, Double confidence, String componentName) {
        mltests = filterMetrics(mltests, componentName, null);
        Map<Pair<String, String>, List<MLMetricsItem>> metricsMap = getMLMetrics2(mltests, null);
        List<MLMetricsItem> metricsList = metricsMap.get(new ImmutablePair(componentName, null));
        if (metricsList == null) {
            return true;
        }
        boolean skipComponent = metricsList.stream().allMatch(e -> e.getTestAccuracy() < confidence);
        return skipComponent;
    }

    @Override
    protected boolean getSkipSubComponent(List<MLMetricsItem> mltests, Double confidence, String componentName,
            String subComponent) {
        mltests = filterMetrics(mltests, componentName, subComponent);
        Map<Pair<String, String>, List<MLMetricsItem>> metricsMap2 = getMLMetrics(mltests, null);
        List<MLMetricsItem> metricsList2 = metricsMap2.get(new ImmutablePair(componentName, subComponent));
        if (metricsList2 == null) {
            return true;
        }        
        boolean skipSubcomponent = metricsList2.stream().allMatch(e -> e.getTestAccuracy() < confidence);
        return skipSubcomponent;
    }

    private List<MLMetricsItem> filterMetrics(List<MLMetricsItem> items, String component, String subcomponent) {
        List<MLMetricsItem> retList = new ArrayList<>();
        return items.stream()
                .filter(e -> (component.equals(e.getComponent()) && (subcomponent == null || subcomponent.equals(e.getSubcomponent()))))
                .collect(Collectors.toList());
    }
}

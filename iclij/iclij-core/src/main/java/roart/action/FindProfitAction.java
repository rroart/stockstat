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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.Memories;
import roart.component.model.ComponentData;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.FindProfitActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.util.MarketUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class FindProfitAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public FindProfitAction() {
        setActionData(new FindProfitActionData());
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
    public Map[] filterMemoryListMapsWithConfidence(Market market, Map<Triple<String, String, String>,List<MemoryItem>> listMap, IclijConfig config) {
        Map<Triple<String, String, String>, List<MemoryItem>> okListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveThresholdMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryItem>> aboveOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveThresholdAboveMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryItem>> belowOkListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> aboveThresholdBelowMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryItem>> badListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowThresholdMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryItem>> aboveBadListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowThresholdAboveMap = new HashMap<>();
        Map<Triple<String, String, String>, List<MemoryItem>> belowBadListMap = new HashMap<>();
        Map<Triple<String, String, String>, Double> belowThresholdBelowMap = new HashMap<>();
        for(Entry<Triple<String, String, String>, List<MemoryItem>> entry : listMap.entrySet()) {
            Triple<String, String, String> keys = entry.getKey();
            List<MemoryItem> memoryList = entry.getValue();
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            List<Double> aboveConfidenceList = new ArrayList<>();
            List<Double> belowConfidenceList = new ArrayList<>();
            if (true) {
            for (MemoryItem memory : memoryList) {
                Long above = memory.getAbovepositives();
                Long below = memory.getBelowpositives();
                Long abovesize = memory.getAbovesize();
                Long belowsize = memory.getBelowsize();
                if (above != null && abovesize !=null && above > 0 && abovesize > 0) {
                    Double aboveConfidence = ( (double ) above) / abovesize;
                    aboveConfidenceList.add(aboveConfidence);
                }
                if (below != null && belowsize !=null && below > 0 && belowsize > 0) {
                    Double belowConfidence = ( (double ) below) / belowsize;
                    belowConfidenceList.add(belowConfidence);
                }
            }
            } else {
                confidences = new ArrayList<>();
                
                Long positives = memoryList.stream().map(MemoryItem::getPositives).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long size = memoryList.stream().map(MemoryItem::getSize).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long above = memoryList.stream().map(MemoryItem::getAbovepositives).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long below = memoryList.stream().map(MemoryItem::getAbovesize).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long abovesize = memoryList.stream().map(MemoryItem::getBelowpositives).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                Long belowsize = memoryList.stream().map(MemoryItem::getBelowsize).filter(Objects::nonNull).collect(Collectors.summingLong(Long::longValue));
                if (size != null) {
                    Double confidence = ( (double ) positives) / size;
                    confidences.add(confidence);                    
                }
                if (abovesize != null) {
                    Double aboveConfidence = ( (double ) above) / abovesize;
                    aboveConfidenceList.add(aboveConfidence);                    
                }
                if (belowsize != null) {
                    Double belowConfidence = ( (double ) below) / belowsize;
                    belowConfidenceList.add(belowConfidence);                    
                }
            }
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            Optional<Double> aboveMinOpt = aboveConfidenceList.parallelStream().reduce(Double::min);
            Optional<Double> belowMinOpt = belowConfidenceList.parallelStream().reduce(Double::min);
            handleMin(market, aboveThresholdMap, belowThresholdMap, keys, minOpt);
            handleMin(market, aboveThresholdAboveMap, belowThresholdAboveMap, keys, aboveMinOpt);
            handleMin(market, aboveThresholdBelowMap, belowThresholdBelowMap, keys, belowMinOpt);
        }
        return new Map[] { aboveThresholdMap, belowThresholdMap, aboveThresholdAboveMap, belowThresholdAboveMap, aboveThresholdBelowMap, belowThresholdBelowMap };
    }

    private void handleMin(Market market, Map<Triple<String, String, String>, Double> aboveThresholdMap,
            Map<Triple<String, String, String>, Double> belowThresholdMap, Triple<String, String, String> keys,
            Optional<Double> valOpt) {
        if (valOpt.isPresent()) {
            Double val = valOpt.get();
            if (val >= market.getFilter().getConfidence()) {
                aboveThresholdMap.put(keys, val);
            } else {
                belowThresholdMap.put(keys, val);               
            }
        }
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
            param.getService().conf.setMLmarket(mlmarket);

            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, config.wantsFindProfitMLDynamic());
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, config.wantsFindProfitMLDynamic());

            String key = component.getThreshold();
            aMap.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            aMap.put(key2, parameters.getFuturedays());
            
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(this, market, param, profitdata, positions, evolve, aMap, subcomponent, null, parameters);
            component.calculateIncDec(componentData, profitdata, positions, buy, mlTests, parameters);
            if (param.getInput().isDoSave()) {
                IncDecItem myitem = null;
                try {
                    for (IncDecItem item : profitdata.getBuys().values()) {
                        myitem = item;
                        item.save();
                        System.out.println(item);
                    }
                    for (IncDecItem item : profitdata.getSells().values()) {
                        myitem = item;
                        item.save();
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

    public List<MemoryItem> findAllMarketComponentsToCheck(WebData myData, ComponentData param, int days, IclijConfig config, MarketComponentTime marketTime, boolean evolve, Map<String, ComponentData> dataMap, Map<String, Component> componentMap) {
        List<MemoryItem> allMemories = new ArrayList<>();
        Short startOffset = marketTime.market.getConfig().getStartoffset();
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

            Parameters parameters = marketTime.parameters;
            String key = component.getThreshold();
            aMap.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            aMap.put(key2, parameters.getFuturedays());
                        
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);
            
            ComponentData componentData = component.handle(this, marketTime.market, param, profitdata, new Memories(marketTime.market), evolve, aMap, marketTime.subcomponent, null, marketTime.parameters);
            dataMap.put(entry.getKey(), componentData);
            componentData.setUsedsec(time0);
            myData.getUpdateMap().putAll(componentData.getUpdateMap());
            List<MemoryItem> memories;
            try {
                memories = component.calculateMemory(componentData, marketTime.parameters);
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
            incdecitems = IclijDbDao.getAllIncDecs();
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
        return component.wantEvolve(param.getInput().getConfig());
    }
    
    @Override
    protected List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        Map<String, Component> componentMap = new HashMap<>();
        componentMap.put(marketTime.componentName, marketTime.component);
        
        try {
            evolve = false;
            //List<MemoryItem> newMemories = findAllMarketComponentsToCheck(myData, param, 0, config, marketTime, evolve, dataMap, componentMap);
            LocalDate prevdate = getPrevDate(param, marketTime.market);
            LocalDate olddate = prevdate.minusDays(((int) MarketAction.AVERAGE_SIZE) * getActionData().getTime(marketTime.market));
            List<MemoryItem> marketMemory = new MarketUtil().getMarketMemory(marketTime.market, getName(), marketTime.componentName, marketTime.subcomponent, JsonUtil.convert(marketTime.parameters), olddate, prevdate);
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
        MarketComponentTime marketTime = new MarketComponentTime();
        marketTime.market = market;
        marketTime.componentName = null;
        marketTime.component = null;
        marketTime.subcomponent = null;
        marketTime.parameters = null;
        //marketTime.timings = timingToDo;
        marketTime.buy = null;
        Map<String, ComponentData> dataMap = new HashMap<>();
        Memories listComponentMap = new Memories(market);
        myData.setMemoryItems(new ArrayList<>());
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        LocalDate prevdate = getPrevDate(param, market);
        String prevdateString = TimeUtil.convertDate2(prevdate);
        int prevdateIndex = TimeUtil.getIndexEqualBefore(stockDates, prevdateString);
        prevdateIndex = prevdateIndex - param.getLoopoffset();
        Short startoffset = market.getConfig().getStartoffset();
        startoffset = startoffset != null ? startoffset : 0;
        prevdateIndex = prevdateIndex - verificationdays - startoffset;
        prevdateString = stockDates.get(prevdateIndex);
        String olddateString = stockDates.get(prevdateIndex - getActionData().getTime(market));
        LocalDate olddate = null;
        try {
            prevdate = TimeUtil.convertDate(prevdateString);
            olddate = TimeUtil.convertDate(olddateString);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        ProfitData profitdata = new ProfitData();
        ProfitInputData inputdata = new ProfitInputData();
        getListComponents(myData, param, componentInput.getConfig(), marketTime, evolve, market, dataMap, listComponentMap, prevdate, olddate);
        profitdata.setInputdata(inputdata);
        myData.setProfitData(profitdata);

        List<IncDecItem> incdecitems = null;
        try {
            incdecitems = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), olddate, prevdate, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        for (IncDecItem item : incdecitems) {
            String id = item.getId() + item.getDate().toString();
            if (item.isIncrease()) {
                profitdata.getBuys().put(id, item);
            } else {
                profitdata.getSells().put(id, item);
            }
        }
        
        setValMap(param);
        Map<String, Map<String, Object>> maps = param.getResultMaps();
        filterIncDecs(param, market, profitdata, maps, true);
        filterIncDecs(param, market, profitdata, maps, false);
        myData.getIncs().addAll(profitdata.getBuys().values());
        myData.getDecs().addAll(profitdata.getSells().values());
        return myData;
    }

}

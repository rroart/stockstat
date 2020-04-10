package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.FindProfitActionData;
import roart.iclij.model.component.ComponentInput;
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
    protected ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Pair<String, Integer>, List<MemoryItem>> listMap) {
        Map<Pair<String, Integer>, List<MemoryItem>> okListMap = new HashMap<>();
        Map<Pair<String, Integer>, Double> okConfMap = new HashMap<>();
        Map<Pair<String, Integer>, List<MemoryItem>> aboveOkListMap = new HashMap<>();
        Map<Pair<String, Integer>, Double> aboveOkConfMap = new HashMap<>();
        Map<Pair<String, Integer>, List<MemoryItem>> belowOkListMap = new HashMap<>();
        Map<Pair<String, Integer>, Double> belowOkConfMap = new HashMap<>();
        for(Entry<Pair<String, Integer>, List<MemoryItem>> entry : listMap.entrySet()) {
            Pair<String, Integer> keys = entry.getKey();
            List<MemoryItem> memoryList = entry.getValue();
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            List<Double> aboveConfidenceList = new ArrayList<>();
            List<Double> belowConfidenceList = new ArrayList<>();
            for (MemoryItem memory : memoryList) {
                Double tpConf = memory.getTpConf();
                Double tnConf = memory.getTnConf();
                Double fpConf = memory.getFpConf();
                Double fnConf = memory.getFnConf();
                Long tpSize = memory.getTpSize();
                Long tnSize = memory.getTnSize();
                Long fpSize = memory.getFpSize();
                Long fnSize = memory.getFnSize();
                Double goodTp = tpConf != null ? tpConf * tpSize : 0;
                Double goodTn = tnConf != null ? tnConf * tnSize : 0;
                Double goodFp = fpConf != null ? fpConf * fpSize : 0;
                Double goodFn = fnConf != null ? fnConf * fnSize : 0;
                Double above = goodTp + goodFn;
                Double below = goodTn + goodFp;
                if (above == null || tpSize == null || fnSize == null) {
                    int jj = 0;
                }
                if (tpSize != null && fnSize != null && above > 0 && (tpSize + fnSize) > 0) {
                    Double aboveConfidence = above / (tpSize + fnSize);
                    aboveConfidenceList.add(aboveConfidence);
                }
                if (tnSize != null && fpSize != null && below > 0 && (tnSize + fpSize) > 0) {
                    Double belowConfidence = below / (tnSize + fpSize);
                    belowConfidenceList.add(belowConfidence);
                }
            }
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            Optional<Double> aboveMinOpt = aboveConfidenceList.parallelStream().reduce(Double::min);
            Optional<Double> belowMinOpt = belowConfidenceList.parallelStream().reduce(Double::min);
            handleMin(market, okListMap, okConfMap, keys, memoryList, minOpt);
            handleMin(market, aboveOkListMap, aboveOkConfMap, keys, memoryList, aboveMinOpt);
            handleMin(market, belowOkListMap, belowOkConfMap, keys, memoryList, belowMinOpt);
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(okConfMap);
        input.setListMap(okListMap);
        input.setAboveConfMap(aboveOkConfMap);
        input.setAboveListMap(aboveOkListMap);
        input.setBelowConfMap(belowOkConfMap);
        input.setBelowListMap(belowOkListMap);
        return input;
    }

    //@Override
    public ProfitInputData filterMemoryListMapsWithConfidence2(Market market, Map<Pair<String, Integer>, List<MemoryItem>> listMap) {
        Map<Pair<String, Integer>, List<MemoryItem>> badListMap = new HashMap<>();
        Map<Pair<String, Integer>, Double> badConfMap = new HashMap<>();
        for(Pair<String, Integer> key : listMap.keySet()) {
            List<MemoryItem> memoryList = listMap.get(key);
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            if (confidences.isEmpty()) {
                int jj = 0;
                //continue;
            }
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            if (!minOpt.isPresent()) {
                int jj = 0;
                //continue;
            }
            Double min = 0.0;
            if (minOpt.isPresent()) {
                min = minOpt.get();
            }
            // do the bad ones
            // do not yet improve on the good enough ones
            if (false /*min >= market.getConfidence()*/) {
                continue;
            }
            //Optional<Double> maxOpt = confidences.parallelStream().reduce(Double::max);
            //Double max = maxOpt.get();
            //System.out.println("Mark " + market.getConfig().getMarket() + " " + keys[0] + " " + min + " " + max );
            //Double conf = market.getConfidence();
            //System.out.println(conf);
            badListMap.put(key, listMap.get(key));
            badConfMap.put(key, min);
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(badConfMap);
        input.setListMap(badListMap);
        input.setAboveConfMap(badConfMap);
        input.setAboveListMap(badListMap);
        input.setBelowConfMap(badConfMap);
        input.setBelowListMap(badListMap);
        return input;
    }

    private void handleMin(Market market, Map<Pair<String, Integer>, List<MemoryItem>> okListMap,
            Map<Pair<String, Integer>, Double> okConfMap, Pair<String, Integer> keys, List<MemoryItem> memoryList,
            Optional<Double> minOpt) {
        if (minOpt.isPresent()) {
            Double min = minOpt.get();
            if (min >= market.getFilter().getConfidence()) {
                okListMap.put(keys, memoryList);
                okConfMap.put(keys, min);
            }
        }
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Map<String, List<Integer>> listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters, boolean wantThree) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String componentName = entry.getKey();
            Component component = componentMap.get(componentName);
            if (component == null) {
                continue;
            }
            List<Integer> positions = listComponent.get(componentName);
            param = dataMap.get(componentName);
            if (param == null) {
                log.error("Component {} not found", componentName);
                continue;
            }
            component.enableDisable(param, positions, param.getConfigValueMap());

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
            component.calculateIncDec(componentData, profitdata, positions, buy);
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
            
            ComponentData componentData = component.handle(this, marketTime.market, param, profitdata, new ArrayList<>(), evolve, aMap, marketTime.subcomponent, null, marketTime.parameters);
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
            List<MemoryItem> newMemories = findAllMarketComponentsToCheck(myData, param, 0, config, marketTime, evolve, dataMap, componentMap);
            return newMemories;
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

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.FINDPROFIT);
    }

    @Override
    protected String getFuturedays0(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
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
        Map<Boolean, Map<String, List<Integer>>> listComponentMap = new HashMap<>();
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
        ProfitInputData inputdata = getListComponents(myData, param, componentInput.getConfig(), marketTime, evolve, market, dataMap, listComponentMap, prevdate, olddate);
        profitdata.setInputdata(inputdata);
        myData.setProfitData(profitdata);

        List<IncDecItem> incdecitems = null;
        try {
            incdecitems = IclijDbDao.getAllIncDecs(market.getConfig().getMarket(), olddate, prevdate);
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

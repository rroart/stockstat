package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.FindProfitComponentFactory;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.db.IclijDbDao;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.config.IclijConfig;
import roart.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class FindProfitAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    //@Override
    public List<MemoryItem> getMarketMemory2(Market market) {
        return new ArrayList<>();
    }

    @Override
    public List<MemoryItem> filterKeepRecent(List<MemoryItem> marketMemory, LocalDate date, int days) {
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
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Map<String, List<Integer>> listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters) {
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
            component.enableDisable(param, positions, param.getConfigValueMap());

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
        myData.incs.addAll(profitdata.getBuys().values());
        myData.decs.addAll(profitdata.getSells().values());
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
            myData.updateMap.putAll(componentData.getUpdateMap());
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

    public void getVerifyProfit(int days, LocalDate date, ControlService srv,
            LocalDate oldDate, List<IncDecItem> listInc, List<IncDecItem> listDec, List<IncDecItem> listIncDec, int startoffset, Double threshold) {
        log.info("Verify compare date {} with {}", oldDate, date);
        LocalDate futureDate = date;
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        Component.disabler(srv.conf.getConfigValueMap());
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        //Set<String> i = resultMaps.keySet();
        Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("Price").get(PipelineConstants.LIST);
        //Set<String> j2 = resultMaps.get("" + category).keySet();
    
        VerifyProfit verify = new VerifyProfit();
        verify.doVerify(listInc, days, true, categoryValueMap, oldDate, startoffset, threshold);
        verify.doVerify(listDec, days, false, categoryValueMap, oldDate, startoffset, threshold);
        verify.doVerify(listIncDec, days, false, categoryValueMap, oldDate, startoffset, threshold);
        //return verify.getTrend(days, categoryValueMap);
    }

    public Trend getTrend(int days, LocalDate date, ControlService srv, int startoffset) {
        //log.info("Verify compare date {} with {}", oldDate, date);
        LocalDate futureDate = date;
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        Component.disabler(srv.conf.getConfigValueMap());
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        //Set<String> i = resultMaps.keySet();
        Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("Price").get(PipelineConstants.LIST);
        //Set<String> j2 = resultMaps.get("" + category).keySet();
    
        VerifyProfit verify = new VerifyProfit();
        //verify.doVerify(listInc, days, true, categoryValueMap, oldDate);
        //verify.doVerify(listDec, days, false, categoryValueMap, oldDate);
        return verify.getTrend(days, categoryValueMap, startoffset);
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
    public String getName() {
        return IclijConstants.FINDPROFIT;
    }

    @Override
    protected List<String> getProfitComponents(IclijConfig config, String marketName) {
        return ServiceUtil.getFindProfitComponents(config, marketName);
    }
    
    @Override
    public Short getTime(Market market) {
        return market.getConfig().getFindtime();
    }
    
    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { true, false };
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
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        return param.getInput().getEnddate();
    }
    
    @Override
    protected void setValMap(ComponentData param) {
        param.getAndSetCategoryValueMap();
    }

    @Override
    public ComponentFactory getComponentFactory() {
        return new FindProfitComponentFactory();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.FINDPROFIT);
    }

    @Override
    protected String getFuturedays0(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getFindProfitThreshold();
    }

}

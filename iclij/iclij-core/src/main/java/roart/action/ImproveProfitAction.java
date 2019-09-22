package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.model.ComponentData;
import roart.config.IclijXMLConfig;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class ImproveProfitAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    private List<Market> getMarkets(IclijConfig instance) {
        List<Market> markets = null;
        try { 
            markets = IclijXMLConfig.getMarkets(instance);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return markets;
    }

    @Override
    public ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Object[], List<MemoryItem>> listMap) {
        Map<Object[], List<MemoryItem>> badListMap = new HashMap<>();
        Map<Object[], Double> badConfMap = new HashMap<>();
        for(Object[] keys : listMap.keySet()) {
            List<MemoryItem> memoryList = listMap.get(keys);
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
            badListMap.put(keys, listMap.get(keys));
            badConfMap.put(keys, min);
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(badConfMap);
        input.setListMap(badListMap);
        return input;
    }

    @Override
    protected void handleComponent(Market market, ProfitData profitdata, ComponentData param, Map<String, List<Integer>> listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
            ComponentData componentData = component.improve(param, market, profitdata, null, buy, subcomponent);
            Map<String, Object> updateMap = componentData.getUpdateMap();
            if (updateMap != null) {
                param.getUpdateMap().putAll(updateMap);
            }
            //component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }
    }

    @Override
    protected List<IncDecItem> getIncDecItems() {
        return null;
    }
    
    @Override
    protected List getAnArray() {
        return null;
    }
    
    @Override
    protected Boolean getBool() {
        return null;
    }
    
    @Override 
    protected String getName() {
        return IclijConstants.IMPROVEPROFIT;
    }
    
    @Override
    protected List<String> getProfitComponents(IclijConfig config, String marketName) {
        return ServiceUtil.getImproveProfitComponents(config, marketName);
    }

    @Override
    protected Short getTime(Market market) {
        return market.getConfig().getImprovetime();
    }
    
    @Override
    protected Boolean[] getBooleans() {
        return new Boolean[] { false, true };
    }
    
    @Override 
    protected boolean getEvolve(Component component, ComponentData param) {
        return true;
    }
    
    @Override
    protected List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        LocalDate prevdate = param.getInput().getEnddate();
        return prevdate.minusDays(market.getConfig().getImprovetime());
    }
    
    @Override
    protected void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap();
    }
    
    @Override
    protected ComponentFactory getComponentFactory() {
        return new ImproveProfitComponentFactory();
    }

}


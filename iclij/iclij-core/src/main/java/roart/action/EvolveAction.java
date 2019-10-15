package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import roart.common.config.ConfigConstants;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.EvolveComponentFactory;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class EvolveAction extends MarketAction {

    @Override
    protected ProfitInputData filterMemoryListMapsWithConfidence(Market market,
            Map<Object[], List<MemoryItem>> listMap) {
        Map<Object[], List<MemoryItem>> badListMap = new HashMap<>();
        Map<Object[], Double> badConfMap = new HashMap<>();
        for(Object[] keys : listMap.keySet()) {
            List<MemoryItem> memoryList = listMap.get(keys);
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            Double min = 0.0;
            if (minOpt.isPresent()) {
                min = minOpt.get();
            }
            badListMap.put(keys, listMap.get(keys));
            badConfMap.put(keys, min);
        }
        ProfitInputData input = new ProfitInputData();
        input.setConfMap(badConfMap);
        input.setListMap(badListMap);
        return input;
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Map<String, List<Integer>> listComponent, Map<String, Component> componentMap,
            Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            boolean evolve = true; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            List<Integer> positions = null;
            ComponentData componentData = component.handle(this, market, param, profitdata, positions, evolve, aMap, subcomponent);
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
    public String getName() {
        return IclijConstants.EVOLVE;
    }

    @Override
    protected List<String> getProfitComponents(IclijConfig config, String marketName) {
        return ServiceUtil.getEvolveComponents(config, marketName);
    }

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getEvolvetime();
    }

    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { false, true };
    }

    @Override
    protected boolean getEvolve(Component component, ComponentData param) {
        return true;
    }

    @Override
    protected List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param,
            IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        LocalDate prevdate = param.getInput().getEnddate();
        return prevdate.minusDays(market.getConfig().getEvolvetime());
    }

    @Override
    protected void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap();
    }

    @Override
    public ComponentFactory getComponentFactory() {
        return new EvolveComponentFactory();
    }

}

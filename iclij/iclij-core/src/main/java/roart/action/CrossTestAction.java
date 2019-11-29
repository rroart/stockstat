package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Pair;

import roart.common.config.ConfigConstants;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.CrosstestComponentFactory;
import roart.component.EvolveComponentFactory;
import roart.component.model.ComponentData;
import roart.config.Market;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;

public class CrossTestAction extends MarketAction {

    @Override
    protected ProfitInputData filterMemoryListMapsWithConfidence(Market market,
            Map<Pair<String, Integer>, List<MemoryItem>> listMap) {
        Map<Pair<String, Integer>, List<MemoryItem>> badListMap = new HashMap<>();
        Map<Pair<String, Integer>, Double> badConfMap = new HashMap<>();
        for(Pair<String, Integer> key : listMap.keySet()) {
            List<MemoryItem> memoryList = listMap.get(key);
            List<Double> confidences = memoryList.stream().map(MemoryItem::getConfidence).collect(Collectors.toList());
            confidences = confidences.stream().filter(m -> m != null && !m.isNaN()).collect(Collectors.toList());
            Optional<Double> minOpt = confidences.parallelStream().reduce(Double::min);
            Double min = 0.0;
            if (minOpt.isPresent()) {
                min = minOpt.get();
            }
            badListMap.put(key, listMap.get(key));
            badConfMap.put(key, min);
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
            for (String mlmarket : market.getConfig().getMlmarkets()) {
                param.getService().conf.setMLmarket(mlmarket);
                boolean evolve = true; // param.getInput().getConfig().wantEvolveML();
                //component.set(market, param, profitdata, positions, evolve);
                //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
                // 0 ok?
                param.getConfigValueMap().put(ConfigConstants.MISCMYTABLEDAYS, 0);
                param.getConfigValueMap().put(ConfigConstants.MISCMYDAYS, 0);
                param.getConfigValueMap().put(IclijConfigConstants.FINDPROFITMLDYNAMIC, Boolean.TRUE);
                Map<String, Object> aMap = new HashMap<>();
                // don't need these both here and in getevolveml?
                aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, false);
                aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
                aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, false);
                aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
                aMap.put(ConfigConstants.MISCMYDAYS, 0);
                List<Integer> positions = null;
                ComponentData componentData = component.handle(this, market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket);
                Map<String, Object> updateMap = componentData.getUpdateMap();
                if (updateMap != null) {
                    param.getUpdateMap().putAll(updateMap);
                }
            }
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
        return IclijConstants.CROSSTEST;
    }

    @Override
    protected List<String> getProfitComponents(IclijConfig config, String marketName) {
        return ServiceUtil.getCrosstestComponents(config, marketName);
    }

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getCrosstime();
    }

    @Override
    public Boolean[] getBooleans() {
        //return new Boolean[] { false, true };
        return new Boolean[] { null };
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
        return prevdate.minusDays(market.getConfig().getCrosstime());
    }

    @Override
    protected void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap();
    }

    @Override
    public ComponentFactory getComponentFactory() {
        return new CrosstestComponentFactory();
    }

}

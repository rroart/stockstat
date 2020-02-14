package roart.action;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.component.Component;
import roart.component.ComponentFactory;
import roart.component.MachineLearningComponentFactory;
import roart.component.model.ComponentData;
import roart.component.model.MLIndicatorData;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.result.model.ResultMeta;
import roart.iclij.config.IclijConfig;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.util.ServiceUtil;
import roart.util.ServiceUtilConstants;

public class MachineLearningAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

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
        input.setAboveConfMap(badConfMap);
        input.setAboveListMap(badListMap);
        input.setBelowConfMap(badConfMap);
        input.setBelowListMap(badListMap);
        return input;
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Map<String, List<Integer>> listComponent, Map<String, Component> componentMap,
            Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters) {
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            String componentName = entry.getKey();
            Component component = componentMap.get(componentName);
            if (component == null) {
                continue;
            }
            List<Integer> positions = listComponent.get(componentName);
            //if (dataMap.containsKey(componentName)) {
            //    param = dataMap.get(componentName);
            //}
            //component.enableDisable(param, positions, param.getConfigValueMap());

            // 0 ok?
            param.getConfigValueMap().put(ConfigConstants.MISCMYTABLEDAYS, 0);
            param.getConfigValueMap().put(ConfigConstants.MISCMYDAYS, 0);
            param.getConfigValueMap().put(IclijConfigConstants.FINDPROFITMLDYNAMIC, Boolean.FALSE);
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, false);
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, false);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
            aMap.put(ConfigConstants.MISCMYDAYS, 0);

            String key = component.getThreshold();
            aMap.put(key, "[" + parameters.getThreshold() + "]");
            String key2 = component.getFuturedays();
            aMap.put(key2, parameters.getFuturedays());
            
            aMap.put(ConfigConstants.MISCTHRESHOLD, null);
            
            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            ComponentData componentData = component.handle(action, market, param, profitdata, positions, evolve, aMap, subcomponent, null, parameters);
        }
    }

    @Override
    protected List<IncDecItem> getIncDecItems() {
        return null;
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
        return IclijConstants.MACHINELEARNING;
    }

    @Override
    protected List<String> getProfitComponents(IclijConfig config, String marketName) {
        return ServiceUtil.getMachineLearningComponents(config, marketName);
    }

    @Override
    public Short getTime(Market market) {
        return market.getConfig().getPersisttime();
    }

    @Override
    public Boolean[] getBooleans() {
        return new Boolean[] { null };
    }

    @Override
    protected boolean getEvolve(Component component, ComponentData param) {
        return component.wantEvolve(param.getInput().getConfig());
    }

    @Override
    protected List<MemoryItem> getMemItems(MarketComponentTime marketTime, WebData myData, ComponentData param,
            IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        return param.getInput().getEnddate();
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetCategoryValueMap();
    }
    
    @Override
    public ComponentFactory getComponentFactory() {
        return new MachineLearningComponentFactory();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.MACHINELEARNING);
    }

    @Override
    protected String getFuturedays0(IclijConfig conf) {
        return conf.getMachineLearningFuturedays();
    }

    @Override
    public String getThreshold(IclijConfig conf) {
        return conf.getMachineLearningThreshold();
    }

}

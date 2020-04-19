package roart.action;

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
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ImproveProfitActionData;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class ImproveProfitAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public ImproveProfitAction() {
        setActionData(new ImproveProfitActionData());
    }
    
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
    public ProfitInputData filterMemoryListMapsWithConfidence(Market market, Map<Pair<String, Integer>, List<MemoryItem>> listMap, IclijConfig config) {
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

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Map<String, List<Integer>> listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        param.getInput().setDoSave(false);
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //component.set(market, param, profitdata, positions, evolve);
            //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
            // 0 ok?
            param.getConfigValueMap().put(ConfigConstants.MISCMYTABLEDAYS, 0);
            param.getConfigValueMap().put(ConfigConstants.MISCMYDAYS, 0);
            param.getConfigValueMap().put(IclijConfigConstants.FINDPROFITMLDYNAMIC, Boolean.TRUE);
            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MACHINELEARNINGMLDYNAMIC, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLCLASSIFY, true);
            aMap.put(ConfigConstants.MACHINELEARNINGMLLEARN, true);
            aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
            aMap.put(ConfigConstants.MISCMYDAYS, 0);
            ComponentData componentData = component.improve(action, param, market, profitdata, null, buy, subcomponent, parameters, wantThree, mlTests);
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
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap();
    }
    
    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.IMPROVEPROFIT);
    }

    @Override
    protected String getFuturedays0(IclijConfig conf) {
        return conf.getFindProfitFuturedays();
    }

}


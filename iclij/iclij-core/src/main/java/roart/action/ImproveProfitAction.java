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
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
import roart.common.constants.ServiceConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.model.ComponentData;
import roart.evolution.chromosome.AbstractChromosome;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.MLMetricsItem;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.Parameters;
import roart.iclij.model.TimingItem;
import roart.iclij.model.WebData;
import roart.iclij.model.action.EvolveActionData;
import roart.iclij.model.action.ImproveProfitActionData;
import roart.iclij.util.MiscUtil;
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
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param, Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters, boolean wantThree, List<MLMetricsItem> mlTests) {
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
            aMap.put(ConfigConstants.MACHINELEARNINGMLCROSS, false);
            aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
            aMap.put(ConfigConstants.MISCMYDAYS, 0);
            ComponentData componentData = component.improve(action, param, market, profitdata, null, buy, subcomponent, parameters, wantThree, mlTests);
            Map<String, Object> updateMap = componentData.getUpdateMap();
            if (updateMap != null) {
                param.getUpdateMap().putAll(updateMap);
            }
            List<String> confList = component.getConflist();
            Map<String, Object> myConfig = componentData.getService().conf.getDeflt();
            Map<String, Object> defaults = new HashMap<>();
            for (String key : confList) {
                Object value = myConfig.get(key);
                defaults.put(key, value);
            }
            Map<String, Object> results = componentData.getResultMap();
            results.put(EvolveConstants.DEFAULT, defaults);
            componentData.getService().send(ServiceConstants.EVOLVEFILTERPROFIT, results, param.getInput().getConfig());
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
    protected List<TimingItem> getCurrentTimings(LocalDate olddate, List<TimingItem> timings, Market market, String name,
            Short time, boolean b, List<String> stockDates) {
        String mldate = ((ImproveProfitActionData) getActionData()).getMlDate(market, stockDates);
        String mldaysdate = ((ImproveProfitActionData) getActionData()).getMlDays(market, stockDates);
        if (mldate == null && mldaysdate == null) {
            return new MiscUtil().getCurrentTimings(olddate, timings, market, getName(), time, false);
        }
        if (mldate != null) {
            try {
                olddate = TimeUtil.convertDate(mldate);
            } catch (ParseException e) {
                log.error(Constants.EXCEPTION, e);
            }
            return new MiscUtil().getCurrentTimings(olddate, timings, market, getName());
        }
        try {
            olddate = TimeUtil.convertDate(mldaysdate);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new MiscUtil().getCurrentTimings(olddate, timings, market, getName(), time, false);
    }
    
    @Override
    public Integer[] getFuturedays(IclijConfig conf) {
        return new Integer[1];
    }

}


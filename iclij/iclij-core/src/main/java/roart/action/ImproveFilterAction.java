package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.ActionComponentItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.model.TimingItem;
import roart.common.pipeline.data.PipelineData;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.iclij.component.Component;
import roart.component.model.ComponentData;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.evolve.Evolve;
import roart.iclij.evolve.FilterEvolveFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ImproveFilterActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;

public class ImproveFilterAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public ImproveFilterAction(IclijConfig iclijConfig) {
        setActionData(new ImproveFilterActionData(iclijConfig));
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
        List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
        int verificationdays = param.getConfig().verificationDays();
        //param.getInput().setDoSave(false);
        try {
            param.setFuturedays(0);
            param.setOffset(0);
            param.setDates(null, null, action.getActionData(), market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }

            List<IncDecItem> allIncDecs = null;
            LocalDate date = param.getFutureDate();
            date = TimeUtil.getBackEqualBefore2(date, verificationdays, stockDates);
            LocalDate prevDate = date.minusDays(market.getConfig().getFindtime());
            try {
                allIncDecs = param.getService().getIo().getIdbDao().getAllIncDecs(market.getConfig().getMarket(), prevDate, date, null);
            } catch (Exception e) {
                log.error(Constants.EXCEPTION, e);
            }
            List<IncDecItem> incdecs = allIncDecs; // new MiscUtil().getCurrentIncDecs(date, allIncDecs, market, market.getConfig().getFindtime(), false);
            List<String> parametersList = new MiscUtil().getParameters(incdecs);
            for (String aParameter : parametersList) {
                List<IncDecItem> incdecsP = new MiscUtil().getCurrentIncDecs(incdecs, aParameter);              

            
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

            int ga = param.getConfig().getEvolveGA();
            Evolve evolve = FilterEvolveFactory.factory(ga);
            String evolutionConfigString = param.getConfig().getImproveAbovebelowEvolutionConfig();
            EvolutionConfig evolutionConfig = JsonUtil.convert(evolutionConfigString, EvolutionConfig.class);

            Map<String, Object> confMap = new HashMap<>();
            List<String> confList = new ArrayList<>();
            ComponentData componentData = evolve.evolve(getActionData(), param, market, profitdata, buy, subcomponent, parameters, mlTests, confMap , evolutionConfig, component.getPipeline(), component, confList );
       
            Map<String, Object> updateMap = componentData.getUpdateMap();
            if (updateMap != null) {
                param.getUpdateMap().putAll(updateMap);
            }
            PipelineData results = componentData.getResultMap();
            log.info("Content {}", JsonUtil.convert(results));
            QueueElement element = new QueueElement();
            InmemoryMessage msg = inmemory.send(ServiceConstants.EVOLVEFILTERFILTER + UUID.randomUUID(), results, null);
            element.setOpid(ServiceConstants.EVOLVE);
            element.setMessage(msg);
            componentData.getService().send(ServiceConstants.EVOLVEFILTERFILTER, element, param.getConfig());
            //component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
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
    protected boolean getEvolve(Component component, ComponentData param) {
        return true;
    }
    
    @Override
    protected List<MemoryItem> getMemItems(ActionComponentItem marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        LocalDate prevdate = param.getInput().getEnddate();
        return prevdate.minusDays(getActionData().getTime(market));
    }
    
    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap(false);
    }
    
    @Override
    protected List<TimingItem> getCurrentTimings(LocalDate olddate, List<TimingItem> timings, Market market, String name,
            Short time, boolean b, List<String> stockDates) {
        String mldate = ((ImproveFilterActionData) getActionData()).getMlDate(market, stockDates);
        String mldaysdate = ((ImproveFilterActionData) getActionData()).getMlDays(market, stockDates);
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

}


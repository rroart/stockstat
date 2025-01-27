package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.constants.EvolveConstants;
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
import roart.common.pipeline.data.SerialMap;
import roart.common.queue.QueueElement;
import roart.common.util.TimeUtil;
import roart.iclij.component.Component;
import roart.component.model.ComponentData;
import roart.db.dao.IclijDbDao;
import roart.evolution.chromosome.AbstractChromosome;
import roart.evolution.fitness.Fitness;
import roart.gene.impl.ConfigMapGene;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijXMLConfig;
import roart.iclij.config.Market;
import roart.iclij.evolution.chromosome.impl.ConfigMapChromosome2;
import roart.iclij.evolution.chromosome.winner.ConfigMapChromosomeWinner;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.EvolveActionData;
import roart.iclij.model.action.ImproveProfitActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;
import roart.iclij.evolution.fitness.impl.FitnessConfigMap;

public class ImproveProfitAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    
    public ImproveProfitAction(IclijDbDao dbDao, IclijConfig iclijConfig) {
        setActionData(new ImproveProfitActionData(iclijConfig, dbDao));
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

            List<String> stockDates = param.getService().getDates(market.getConfig().getMarket());
            ConfigMapGene gene = new ConfigMapGene(component.getConflist(), param.getService().conf);
            Fitness fitness = new FitnessConfigMap(action.getActionData(), param, profitdata, market, null, component.getPipeline(), buy, subcomponent, parameters, gene, stockDates);
            ComponentData componentData = component.improve(action.getActionData(), param, market, profitdata, null, buy, subcomponent, parameters, wantThree, mlTests, fitness, action.getParent() != null);
            Map<String, Object> updateMap = componentData.getUpdateMap();
            if (updateMap != null) {
                param.getUpdateMap().putAll(updateMap);
            }
            List<String> confList = component.getConflist();
            Map<String, Object> myConfig = componentData.getService().conf.getConfigData().getConfigMaps().deflt;
            Map<String, Object> defaults = new HashMap<>();
            for (String key : confList) {
                Object value = myConfig.get(key);
                defaults.put(key, value);
            }
            PipelineData results = componentData.getResultMap();
            // if not interrupted
            if (results != null) {
                // TODO TODO
            	results.put(EvolveConstants.DEFAULT, new SerialMap(defaults));
            	// TODO?
                Inmemory inmemory = InmemoryFactory.get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
                QueueElement element = new QueueElement();
                InmemoryMessage msg = inmemory.send(ServiceConstants.EVOLVEFILTERPROFIT + UUID.randomUUID(), results, null);
                element.setOpid(ServiceConstants.EVOLVE);
                element.setMessage(msg);
            	componentData.getService().send(ServiceConstants.EVOLVEFILTERPROFIT, element, param.getConfig());
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
    protected List<MemoryItem> getMemItems(ActionComponentItem marketTime, WebData myData, ComponentData param, IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        LocalDate prevdate = param.getInput().getEnddate();
        return prevdate.minusDays(market.getConfig().getImprovetime());
    }
    
    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap(true);
    }
    
    @Override
    protected List<TimingItem> getCurrentTimings(LocalDate olddate, List<TimingItem> timings, Market market, String name,
            Short time, boolean b, List<String> stockDates) {
        String mldate = ((ImproveProfitActionData) getActionData()).getMlDate(market, stockDates);
        String mldaysdate = ((ImproveProfitActionData) getActionData()).getMlDays(market, stockDates);
        if (mldate == null && mldaysdate == null) {
            return new MiscUtil().getCurrentTimings(olddate, timings, market, getName(), time, false);
        }
        return new MiscUtil().getCurrentTimingsRecord(olddate, timings, market, getName(), (int) time, false);
    }
    
    @Override
    public Integer[] getFuturedays(IclijConfig conf) {
        return new Integer[1];
    }

    /*
     * ComponentMLAggregator:
        FitnessConfigMap fit = new FitnessConfigMap(action, param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene, stockDates);
        FitnessConfigMap fit = new FitnessConfigMap(action, param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene, stockDates);
        FitnessConfigMap fit = new FitnessConfigMap(action, param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene, stockDates);
        FitnessConfigMap fit = new FitnessConfigMap(action, param, profitdata, market, null, getPipeline(), buy, subcomponent, parameters, gene, stockDates);
*/        
}


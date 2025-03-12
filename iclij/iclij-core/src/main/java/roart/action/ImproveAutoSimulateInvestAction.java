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

import roart.common.constants.Constants;
import roart.common.constants.ServiceConstants;
import roart.common.inmemory.factory.InmemoryFactory;
import roart.common.inmemory.model.Inmemory;
import roart.common.inmemory.model.InmemoryMessage;
import roart.common.model.ActionComponentItem;
import roart.common.model.IncDecItem;
import roart.common.model.MLMetricsItem;
import roart.common.model.MemoryItem;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.queue.QueueElement;
import roart.common.util.JsonUtil;
import roart.iclij.component.Component;
import roart.iclij.component.ImproveAutoSimulateInvestComponent;
import roart.component.model.ComponentData;
import roart.component.model.SimulateInvestData;
import roart.constants.SimConstants;
import roart.evolution.config.EvolutionConfig;
import roart.evolution.fitness.Fitness;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;
import roart.iclij.evolve.Evolve;
import roart.iclij.evolve.SimulateInvestEvolveFactory;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.ImproveAutoSimulateInvestActionData;
import roart.iclij.model.action.ImproveSimulateInvestActionData;
import roart.service.model.ProfitData;

public class ImproveAutoSimulateInvestAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public ImproveAutoSimulateInvestAction(IclijConfig iclijConfig) {
        setActionData(new ImproveAutoSimulateInvestActionData(iclijConfig));
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
    protected List<MemoryItem> getMemItems(ActionComponentItem marketTime, WebData myData, ComponentData param,
            IclijConfig config, Boolean evolve, Map<String, ComponentData> dataMap) {
        return new ArrayList<>();
    }

    @Override
    protected LocalDate getPrevDate(ComponentData param, Market market) {
        return param.getInput().getEnddate();
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap(false);
    }

    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData componentparam,
            Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap,
            Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters,
            boolean wantThree, List<MLMetricsItem> mlTests) {
        if (componentparam.getUpdateMap() == null) {
        	componentparam.setUpdateMap(new HashMap<>());
        }
        componentparam.getInput().setDoSave(false);

        try {
        	componentparam.setFuturedays(0);
        	componentparam.setOffset(0);
        	componentparam.setDates(null, null, action.getActionData(), market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        //List<MemoryItem> memories = findAllMarketComponentsToCheckNew(myData, param, 0, config, false, dataMap, componentMap, subcomponent, parameters, market);
        
        Inmemory inmemory = componentparam.getService().getIo().getInmemoryFactory().get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
        
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            SimulateInvestData param = new SimulateInvestData(componentparam);
            // TODO
            param.setAllIncDecs(getAllIncDecs(market, null, null));
            param.setAllMetas(((ImproveAutoSimulateInvestComponent)component).getAllMetas(componentparam));
            // TODO getcontent
            ((ImproveAutoSimulateInvestComponent)component).getResultMaps(param, market);
            List<String> confList = component.getConflist();

            int ga = param.getConfig().getEvolveGA();
            Evolve evolve = SimulateInvestEvolveFactory.factory(ga);
            String evolutionConfigString = param.getConfig().getImproveAutoSimulateInvestEvolutionConfig();
            EvolutionConfig evolutionConfig = JsonUtil.convert(evolutionConfigString, EvolutionConfig.class);
            Map<String, Object> confMap = new HashMap<>();
            ComponentData e = evolve.evolve(action.getActionData(), param, market, profitdata, buy, subcomponent, parameters, mlTests, confMap , evolutionConfig, component.getPipeline(), component, confList);
            PipelineData results = e.getResultMap();
            Object filters = param.getConfigValueMap().remove(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS);
            // filters is already a serialized string
            filters = param.getInput().getValuemap().get(IclijConfigConstants.AUTOSIMULATEINVESTFILTERS);
            results.put(SimConstants.FILTER, filters);
            QueueElement element = new QueueElement();
            InmemoryMessage msg = inmemory.send(ServiceConstants.SIMAUTO + UUID.randomUUID(), results, null);
            element.setOpid(ServiceConstants.SIM);
            element.setMessage(msg);
            e.getService().send(ServiceConstants.SIMAUTO, element, param.getConfig());
            Map<String, Object> updateMap = e.getUpdateMap();
            if (updateMap != null) {
                param.getUpdateMap().putAll(updateMap);
            }
        }

    }
    
    @Deprecated // ?
    public List<IncDecItem> getAllIncDecs(Market market, LocalDate investStart,
            LocalDate investEnd) {
        try {
            return null; //param.getService().getIo().getIdbDao().getAllIncDecs(market.getConfig().getMarket(), investStart, investEnd, null);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return new ArrayList<>();
    }

}

package roart.action;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.ActionComponentDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.pipeline.util.PipelineUtils;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.iclij.component.Component;
import roart.component.model.ComponentData;
import roart.constants.IclijConstants;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.Trend;
import roart.iclij.model.WebData;
import roart.iclij.model.action.SimulateInvestActionData;
import roart.iclij.service.util.MarketUtil;
import roart.iclij.service.util.MiscUtil;
import roart.iclij.verifyprofit.TrendUtil;
import roart.iclij.verifyprofit.VerifyProfitUtil;
import roart.service.model.ProfitData;

public class SimulateInvestAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public SimulateInvestAction(IclijConfig iclijConfig) {
        setActionData(new SimulateInvestActionData(iclijConfig));
    }
    
    @Override
    protected List<IncDecDTO> getIncDecDTOs() {
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
    protected List<MemoryDTO> getMemDTOs(ActionComponentDTO marketTime, WebData myData, ComponentData param,
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
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Memories listComponent, Map<String, Component> componentMap, Map<String, ComponentData> dataMap,
            Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters,
            boolean wantThree, List<MLMetricsDTO> mlTests) {
        log.info("Param id {}", param.getId());
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        //param.getInput().setDoSave(false);

        try {
            param.setFuturedays(0);
            param.setOffset(0);
            param.setDates(null, null, action.getActionData(), market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        //List<MemoryDTO> memories = findAllMarketComponentsToCheckNew(myData, param, 0, config, false, dataMap, componentMap, subcomponent, parameters, market);
        
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }

            boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
            //param.getAndSetCategoryValueMap();
            //component.set(market, param, profitdata, positions, evolve);
            //ComponentData componentData = component.handle(market, param, profitdata, positions, evolve, new HashMap<>());
            // 0 ok?
            param.getConfigValueMap().put(ConfigConstants.MISCMYTABLEDAYS, 0);
            param.getConfigValueMap().put(ConfigConstants.MISCMYDAYS, 0);

            Map<String, Object> aMap = new HashMap<>();
            aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
            aMap.put(ConfigConstants.MISCMYDAYS, 0);
             //valueMap.put(ConfigConstants.MACHINELEARNING, false);
            aMap.put(ConfigConstants.AGGREGATORS, false);
            aMap.put(ConfigConstants.INDICATORSRSIRECOMMEND, false);
            aMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, false);
            aMap.put(ConfigConstants.AGGREGATORS, false);
            // TODO
            // TODO getName() gives null
            param.getResultMap(getName(), aMap, false, param.isKeepPipeline()); // TODO cache
            Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(param.getService().getIclijConfig());
            //PipelineData metaData = PipelineUtils.getPipeline(param.getResultMaps(), PipelineConstants.META, inmemory);
            //SerialMeta meta = PipelineUtils.getMeta(metaData);
            //String catName = new MetaUtil().getCategory(meta,  cat);
            PipelineData pipelineDatum = PipelineUtils.getPipeline(param.getResultMaps(), PipelineConstants.META, inmemory);
            Integer cat = PipelineUtils.getWantedcat(pipelineDatum);
            String catName = PipelineUtils.getMetaCat(pipelineDatum);
            log.info("cats {} {}", cat, catName);
            param.setCategory(cat);
            param.setCategoryTitle(catName);
            param.getAndSetCategoryValueMapAlt();

            
            ComponentData componentData = component.handle(getActionData(), market, param, profitdata, listComponent, evolve, aMap, subcomponent, null, null, getParent() != null);
            
            Map<String, Object> updateMap = componentData.getUpdateMap();
            if (updateMap != null) {
                param.getUpdateMap().putAll(updateMap);
            }
            
             //component.calculateIncDec(componentData, profitdata, positions);
            //System.out.println("Buys: " + market.getMarket() + buys);
            //System.out.println("Sells: " + market.getMarket() + sells);           
        }

    }
}

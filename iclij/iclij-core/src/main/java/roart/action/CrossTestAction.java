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

import roart.common.cache.MyCache;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.ActionComponentDTO;
import roart.common.model.IncDecDTO;
import roart.common.model.MLMetricsDTO;
import roart.common.model.MemoryDTO;
import roart.common.model.TimingDTO;
import roart.common.pipeline.util.PipelineThreadUtils;
import roart.common.util.TimeUtil;
import roart.iclij.component.Component;
import roart.component.model.ComponentData;
import roart.iclij.config.IclijConfig;
import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.Market;
import roart.iclij.filter.Memories;
import roart.iclij.model.Parameters;
import roart.iclij.model.WebData;
import roart.iclij.model.action.CrossTestActionData;
import roart.iclij.service.util.MiscUtil;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class CrossTestAction extends MarketAction {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public CrossTestAction(IclijConfig iclijConfig) {
        setActionData(new CrossTestActionData(iclijConfig));
    }
    
    @Override
    protected void handleComponent(MarketAction action, Market market, ProfitData profitdata, ComponentData param,
            Memories listComponent, Map<String, Component> componentMap,
            Map<String, ComponentData> dataMap, Boolean buy, String subcomponent, WebData myData, IclijConfig config, Parameters parameters, boolean wantThree, List<MLMetricsDTO> mlTests) {
        if (param.getUpdateMap() == null) {
            param.setUpdateMap(new HashMap<>());
        }
        for (Entry<String, Component> entry : componentMap.entrySet()) {
            Component component = entry.getValue();
            if (component == null) {
                continue;
            }
            if (market.getConfig().getMlmarkets() == null) {
                continue;
            }
            for (String mlmarket : market.getConfig().getMlmarkets()) {
                param.getService().coremlconf.getConfigData().setMlmarket(mlmarket);
                boolean evolve = false; // param.getInput().getConfig().wantEvolveML();
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
                aMap.put(ConfigConstants.MACHINELEARNINGMLCROSS, true);

                String key = component.getThreshold();
                aMap.put(key, "[" + parameters.getThreshold() + "]");
                String key2 = component.getFuturedays();
                aMap.put(key2, parameters.getFuturedays());
                
                aMap.put(ConfigConstants.MISCTHRESHOLD, null);
                
                aMap.put(ConfigConstants.MISCMYTABLEDAYS, 0);
                aMap.put(ConfigConstants.MISCMYDAYS, 0);
                Memories positions = null;
                param.getService().coremlconf.getConfigData().setDate(param.getFutureDate());
                ComponentData componentData = component.handle(getActionData(), market, param, profitdata, positions, evolve, aMap, subcomponent, mlmarket, parameters, getParent() != null);
                Inmemory inmemory = param.getService().getIo().getInmemoryFactory().get(config.getInmemoryServer(), config.getInmemoryHazelcast(), config.getInmemoryRedis());
                new PipelineThreadUtils(config, inmemory, param.getService().getIo().getCuratorClient()).cleanPipeline(param.getService().id, param.getId());
                MyCache.getInstance().invalidate(param.getId());
                Map<String, Object> updateMap = componentData.getUpdateMap();
                if (updateMap != null) {
                    param.getUpdateMap().putAll(updateMap);
                }
            }
        }
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
        LocalDate prevdate = param.getInput().getEnddate();
        return prevdate.minusDays(market.getConfig().getCrosstime());
    }

    @Override
    public void setValMap(ComponentData param) {
        param.getAndSetWantedCategoryValueMap(false);
    }

    @Override
    protected List<TimingDTO> getCurrentTimings(LocalDate olddate, List<TimingDTO> timings, Market market, String name,
            Short time, boolean b, List<String> stockDates) {
        String mldate = ((CrossTestActionData) getActionData()).getMlDate(market, stockDates);
        String mldaysdate = ((CrossTestActionData) getActionData()).getMlDays(market, stockDates);
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

package roart.component;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.JsonUtil;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.component.model.MLIndicatorData;
import roart.component.model.PredictorData;
import roart.config.Market;
import roart.evolution.config.EvolutionConfig;
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.MemoryItem;
import roart.iclij.model.TimingItem;
import roart.result.model.ResultMeta;
import roart.service.ControlService;
import roart.service.model.ProfitData;

public abstract class Component {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    /*
    private Market market;

    private ComponentData param;
    
    private ProfitData profitdata;
    
    private List<Integer> positions;
    
    private Map<String, Object> valueMap;
    
    private boolean evolve;
  */  
    
    private Map<String, Object> evolveMap = new HashMap<>();
    
    public abstract void enable(Map<String, Object> valueMap);
    
    public abstract void disable(Map<String, Object> valueMap);
    
    public static void disabler(Map<String, Object> valueMap) {
        valueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);
        valueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        valueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        valueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);        
    }
    
    public abstract ComponentData handle(Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve);
    
    public abstract Map<String, String> improve(Market market, MyMyConfig conf, ProfitData profitdata, List<Integer> positions);

    public void handle2(Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, boolean evolve) {
        try {
            param.setDates(0, 0, TimeUtil.convertDate2(param.getInput().getEnddate()));
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        } catch (IndexOutOfBoundsException e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<String, Object> valueMap = new HashMap<>();
        Component.disabler(valueMap);
        this.enable(valueMap);
        try {
            Map<String, Object> loadValues = mlLoads(param, null, market);
            valueMap.putAll(loadValues);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        String pipeline = getPipeline();
        param.getService().conf.getConfigValueMap().putAll(valueMap);
        if (evolve) {   
            long time0 = System.currentTimeMillis();
            evolveMap = handleEvolve(market, pipeline, evolve, param);
            saveTiming(param, evolve, time0);
        }
        valueMap.putAll(evolveMap);
        long time0 = System.currentTimeMillis();
        Map<String, Object> resultMaps = param.getResultMap(pipeline, valueMap);
        param.setCategory(resultMaps);
        param.getAndSetCategoryValueMap();
        saveTiming(param, false, time0);
    }

    private void saveTiming(ComponentData param, boolean evolve, long time0) {
        TimingItem timing = new TimingItem();
        timing.setAction(param.getAction());
        timing.setMarket(param.getInput().getMarket());
        timing.setEvolve(evolve);
        timing.setComponent(getPipeline());
        timing.setTime(time0);
        timing.setRecord(LocalDate.now());
        timing.setDate(param.getFutureDate());
        try {
            timing.save();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
    }
    
    protected abstract Map<String, Object> handleEvolve(Market market, String pipeline, boolean evolve, ComponentData param);

    public abstract EvolutionConfig getEvolutionConfig(ComponentData componentdata);
    
    public abstract EvolutionConfig getLocalEvolutionConfig(ComponentData componentdata);
    
    public abstract Map<String, EvolveMLConfig> getMLConfig(Market market, ComponentData componentdata);

    public abstract String getLocalMLConfig(ComponentData componentdata);

    public abstract MLConfigs getOverrideMLConfig(ComponentData componentdata);
    
    public void set(Market market, ComponentData param, ProfitData profitdata, List<Integer> positions,
            boolean evolve) {
        /*
        this.market = market;
        this.param = param;
        this.profitdata = profitdata;
        this.positions = positions;
        this.valueMap = valueMap;
        this.evolve = evolve;
        */
    }

    public abstract void calculateIncDec(ComponentData param, ProfitData profitdata, List<Integer> positions);

    public abstract List<MemoryItem> calculateMemory(ComponentData param) throws Exception;

    public abstract String getPipeline();
    
    protected abstract Map<String, Object> mlLoads(ComponentData param, Map<String, Object> anUpdateMap, Market market) throws Exception;

}


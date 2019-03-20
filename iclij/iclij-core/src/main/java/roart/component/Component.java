package roart.component;

import java.text.ParseException;
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
import roart.iclij.config.EvolveMLConfig;
import roart.iclij.config.MLConfigs;
import roart.iclij.model.MemoryItem;
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

    public void handle2(Market market, ComponentData param, ProfitData profitdata, List<Integer> positions, String pipeline, String localMl, MLConfigs overrideLSTM, boolean evolve, String localEvolve) {
        try {
            param.setDates(0, 0, TimeUtil.convertDate2(param.getInput().getEnddate()));
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        Map<String, Object> valueMap = new HashMap<>();
        Component.disabler(valueMap);
        this.enable(valueMap);
        param.getService().conf.getConfigValueMap().putAll(valueMap);
        Map<String, Object> evolveMap = handleEvolve(market, pipeline, localMl, overrideLSTM, evolve, param, localEvolve);
        valueMap.putAll(evolveMap);
        Map<String, Object> resultMaps = param.getResultMap(pipeline, valueMap);
        param.setCategory(resultMaps);
        param.getAndSetCategoryValueMap();

    }
    
    protected abstract Map<String, Object> handleEvolve(Market market, String pipeline, String localMl, MLConfigs overrideLSTM, boolean evolve, ComponentData param, String localEvolve);

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

    public abstract void calculate(ComponentData param, ProfitData profitdata, List<Integer> positions);

    public abstract List<MemoryItem> calculate2(ComponentData param) throws Exception;

}


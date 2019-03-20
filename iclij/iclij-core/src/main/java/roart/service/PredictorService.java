package roart.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.action.FindProfitAction;
import roart.common.config.ConfigConstants;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentPredictor;
import roart.component.ComponentRecommender;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.component.model.PredictorData;
import roart.config.Market;
import roart.iclij.model.MemoryItem;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PredictorService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void doPredict(ComponentInput componentInput) throws Exception {
        doPredict(componentInput, new HashMap<>());
    }

    public List<MemoryItem> doPredict(ComponentInput componentInput, Map<String, Object> configValueMap) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(componentInput.getMarket());
        srv.conf.getConfigValueMap().putAll(configValueMap);
        ComponentData param = new ComponentData(componentInput);
        param.setBaseDate(componentInput.getEnddate());
        param.setService(srv);
        return doPredict(param);
    }

    public List<MemoryItem> doPredict(ComponentData componentparam) throws Exception {
        long time0 = System.currentTimeMillis();

        Market market = FindProfitAction.findMarket(componentparam);
        ProfitData profitdata = new ProfitData();

        /*
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.TRUE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        setValueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);
        setValueMap.put(ConfigConstants.INDICATORSRSI, Boolean.FALSE);
        */
        
        Component component = new ComponentPredictor();
        ComponentData componentData = component.handle(market, componentparam, profitdata, new ArrayList<>(), false);
        componentData.setUsedsec(time0);
        return component.calculate2(componentData);

        //PredictorData param = new PredictorData(componentparam);
        /*
        param.setMarket(market);
        param.setDoPrint(doPrint);
        param.setDoSave(doSave);
        srv.conf.setMarket(market);
        */
        /*
        String lstmConf = param.getService().conf.getLSTMConfig();
        int futuredays = 0;
        try { 
            TensorflowLSTMConfig lstm = new ObjectMapper().readValue(lstmConf, TensorflowLSTMConfig.class);
            futuredays = lstm.getHorizon();
        } catch (Exception e) {
            log.error("Exception", e);
            return new ArrayList<>();
        }
        param.setFuturedays(futuredays);
        param.setDates(param.getService(), futuredays, param.getLoopoffset(), TimeUtil.convertDate2(param.getFutureDate()));
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.TRUE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        setValueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);
        setValueMap.put(ConfigConstants.INDICATORSRSI, Boolean.FALSE);
        
        Map map = (Map) param.getCategoryResultMap(param.getService(), PipelineConstants.LSTM, setValueMap);
        if (map == null) {
            return null;
        }
        param.setCategory(map);
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) map.get(PipelineConstants.RESULT);
        param.getAndSetCategoryValueMap();
        */
    }

}

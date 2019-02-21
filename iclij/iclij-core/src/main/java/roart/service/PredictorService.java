package roart.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import roart.common.config.ConfigConstants;
import roart.common.ml.TensorflowLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.ComponentPredictor;
import roart.component.model.PredictorParam;
import roart.iclij.model.MemoryItem;
import roart.util.ServiceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class PredictorService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public List<MemoryItem> doPredict(String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doPredict(srv, market, offset, aDate, doSave, doPrint);
    }

    public List<MemoryItem> doPredict(ControlService srv, String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws Exception {
        long time0 = System.currentTimeMillis();
        PredictorParam param = new PredictorParam();
        param.setMarket(market);
        param.setDoPrint(doPrint);
        param.setDoSave(doSave);
        srv.conf.setMarket(market);
        String lstmConf = srv.conf.getLSTMConfig();
        int futuredays = 0;
        try { 
            TensorflowLSTMConfig lstm = new ObjectMapper().readValue(lstmConf, TensorflowLSTMConfig.class);
            futuredays = lstm.getHorizon();
        } catch (Exception e) {
            log.error("Exception", e);
            return new ArrayList<>();
        }
        param.setFuturedays(futuredays);
        param.setDates(srv, futuredays, offset, aDate);
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.TRUE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        setValueMap.put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);
        
        Map map = (Map) param.getCategoryResultMap(srv, PipelineConstants.LSTM, setValueMap);
        if (map == null) {
            return null;
        }
        param.setCategory(map);
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) map.get(PipelineConstants.RESULT);
        param.setResultMap(resultMap);
        param.getAndSetCategoryValueMap(srv);
        param.setUsedsec(time0);
        return new ComponentPredictor().calculatePredictor(param);
    }

}

package roart.service;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.ComponentMLIndicator;
import roart.component.ComponentMLMACD;
import roart.component.model.MLIndicatorParam;
import roart.component.model.MLMACDParam;
import roart.iclij.model.MemoryItem;
import roart.result.model.ResultMeta;
import roart.util.ServiceUtil;

public class MLService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public List<MemoryItem> doMLMACD(String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doMLMACD(srv, market, offset, aDate, doSave, doPrint);
    }

    public List<MemoryItem> doMLMACD(ControlService srv, String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        long time0 = System.currentTimeMillis();
        MLMACDParam param = new MLMACDParam();
        param.setMarket(market);
        param.setDoPrint(doPrint);
        param.setDoSave(doSave);
        srv.conf.setMarket(market);
        int daysafterzero = (int) srv.conf.getMACDDaysAfterZero();
        param.setDaysafterzero(daysafterzero);
        param.setDatesAndOffset(srv, daysafterzero, offset, aDate);
    
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        setValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);
        Map mlMACDMaps = (Map) param.getResultMap(srv, PipelineConstants.MLMACD, setValueMap);
        param.setCategory(mlMACDMaps);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlMACDMaps.get(PipelineConstants.RESULT);
        if (resultMap == null) {
            return null;
        }
        param.setResultMap(resultMap);
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMACDMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlMACDMaps.get(PipelineConstants.RESULTMETAARRAY);
        param.setResultMetaArray(resultMetaArray);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        param.setResultMeta(resultMeta);
        param.getAndSetCategoryValueMap(srv);
        try {
            // TODO add more offset
            // TODO verify dates and offsets
            param.setUsedsec(time0);
            return new ComponentMLMACD().calculateMLMACD(param);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }

    public List<MemoryItem> doMLIndicator(String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doMLIndicator(srv, market, offset, aDate, doSave, doPrint);
    }

    public List<MemoryItem> doMLIndicator(ControlService srv, String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws ParseException {
        long time0 = System.currentTimeMillis();
        MLIndicatorParam param = new MLIndicatorParam();
        param.setMarket(market);
        param.setDoPrint(doPrint);
        param.setDoSave(doSave);
        srv.conf.setMarket(market);
        int futuredays = (int) srv.conf.getAggregatorsIndicatorFuturedays();
        param.setFuturedays(futuredays);
        double threshold = srv.conf.getAggregatorsIndicatorThreshold();
        param.setThreshold(threshold);
        param.setDates(srv, futuredays, offset, aDate);
        //srv.getTestRecommender(true);
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        Map mlIndicatorMaps = (Map) param.getResultMap(srv, PipelineConstants.MLINDICATOR, setValueMap);
        param.setCategory(mlIndicatorMaps);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlIndicatorMaps.get(PipelineConstants.RESULT);
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlIndicatorMaps.get(PipelineConstants.PROBABILITY);
        List<Object[]> resultMetaArray = (List<Object[]>) mlIndicatorMaps.get(PipelineConstants.RESULTMETAARRAY);
        List<Object> objectList = (List<Object>) mlIndicatorMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        param.setResultMeta(resultMeta);
        //System.out.println("m4 " + resultMap.keySet());
        //System.out.println("m4 " + probabilityMap.keySet());
        if (resultMap == null) {
            return null;
        }
        param.setResultMap(resultMap);
        //int size = resultMap.values().iterator().next().size();
        /*
        Map<String, Object>[] aMap = new HashMap[size];
        for (int i = 0; i < size; i++) {
            aMap[i] = new HashMap<>();
        }
        for (String key : resultMap.keySet()) {
            List<Object> list = resultMap.get(key);
            for (int i = 0; i < size; i++) {
                if (list.get(i) != null) {
                    aMap[i].put(key, list.get(i));    
                }
            }
        }
        */
        param.getAndSetCategoryValueMap(srv);
        try {
            param.setUsedsec(time0);
            return new ComponentMLIndicator().calculateMLindicator(param);
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }


}

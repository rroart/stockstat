package roart.service;

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

import roart.action.FindProfitAction;
import roart.common.config.ConfigConstants;
import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.Component;
import roart.component.ComponentMLIndicator;
import roart.component.ComponentMLMACD;
import roart.component.ComponentRecommender;
import roart.component.model.ComponentInput;
import roart.component.model.ComponentData;
import roart.component.model.MLIndicatorData;
import roart.component.model.MLMACDData;
import roart.config.Market;
import roart.iclij.model.MemoryItem;
import roart.result.model.ResultMeta;
import roart.service.model.ProfitData;
import roart.util.ServiceUtil;

public class MLService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void doMLMACD(ComponentInput componentInput) throws Exception {
        doMLMACD(componentInput, new HashMap<>());
    }

    public List<MemoryItem> doMLMACD(ComponentInput input, Map<String, Object> configValueMap) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(input.getMarket());
        srv.conf.getConfigValueMap().putAll(configValueMap);
        //ComponentInput input = new ComponentInput(market, TimeUtil.convertDate(aDate), null, 0, doSave, true);
        ComponentData param = new ComponentData(input);
        param.setService(srv);
        //param.setFutureDate(TimeUtil.convertDate(aDate));

        return doMLMACD(param);
    }

    public List<MemoryItem> doMLMACD(ComponentData componentparam) throws Exception {
        long time0 = System.currentTimeMillis();
        Market market = FindProfitAction.findMarket(componentparam);
        ProfitData profitdata = new ProfitData();
        
        /*
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        setValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);

        new ComponentMLMACD().handle(market, componentparam, profitdata, new ArrayList<>(), setValueMap, false);
*/
        Component component = new ComponentMLMACD();
        ComponentData componentData = component.handle(market, componentparam, profitdata, new ArrayList<>(), false);
        componentData.setUsedsec(time0);
        return component.calculate2(componentData);

        /*
        MLMACDData param = new MLMACDData(componentparam);
        param.getService().conf.setMarket(param.getMarket());
        int daysafterzero = (int) param.getService().conf.getMACDDaysAfterZero();
        param.setDaysafterzero(daysafterzero);
        //param.setDatesAndOffset(param.getService(), daysafterzero, param.getOffset(), aDate);
    
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        setValueMap.put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);
        Map mlMACDMaps = (Map) param.getResultMap(PipelineConstants.MLMACD, setValueMap);
        param.setCategory(mlMACDMaps);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlMACDMaps.get(PipelineConstants.RESULT);
        if (resultMap == null) {
            return null;
        }
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMACDMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlMACDMaps.get(PipelineConstants.RESULTMETAARRAY);
        param.setResultMetaArray(resultMetaArray);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        param.setResultMeta(resultMeta);
        param.getAndSetCategoryValueMap();
        */
        /*
        try {
            // TODO add more offset
            // TODO verify dates and offsets
            componentparam.setUsedsec(time0);
            return new ComponentMLMACD().calculate2(componentparam);
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
        */
    }

    public void doMLIndicator(ComponentInput componentInput) throws Exception {
        doMLIndicator(componentInput, new HashMap<>());
    }

    public List<MemoryItem> doMLIndicator(ComponentInput input, Map<String, Object> configValueMap) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        srv.conf.setMarket(input.getMarket());
        srv.conf.getConfigValueMap().putAll(configValueMap);
        //ComponentInput input = new ComponentInput(market, TimeUtil.convertDate(aDate), null, 0, doSave, true);
        ComponentData param = new ComponentData(input);
        param.setService(srv);
        //param.setFutureDate(TimeUtil.convertDate(aDate));

        return doMLIndicator(param);
    }

    public List<MemoryItem> doMLIndicator(ComponentData componentparam) throws Exception {
        long time0 = System.currentTimeMillis();
        Market market = FindProfitAction.findMarket(componentparam);
        ProfitData profitdata = new ProfitData();

        /*
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);

        new ComponentMLIndicator().handle(market, componentparam, profitdata, new ArrayList<>(), setValueMap, false);
        */
        
        Component component = new ComponentMLIndicator();
        ComponentData componentData = component.handle(market, componentparam, profitdata, new ArrayList<>(), false);
        componentData.setUsedsec(time0);
        return component.calculate2(componentData);

        /*
        MLIndicatorData param = new MLIndicatorData(componentparam);
        param.getService().conf.setMarket(param.getMarket());
        int futuredays = (int) param.getService().conf.getAggregatorsIndicatorFuturedays();
        param.setFuturedays(futuredays);
        double threshold = param.getService().conf.getAggregatorsIndicatorThreshold();
        param.setThreshold(threshold);
        param.setDates(param.getService(), futuredays, param.getLoopoffset(), TimeUtil.convertDate2(param.getFutureDate()));
        //srv.getTestRecommender(true);
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        setValueMap.put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        Map mlIndicatorMaps = (Map) param.getResultMap(PipelineConstants.MLINDICATOR, setValueMap);
        if (mlIndicatorMaps == null) {
            int jj = 0;
        }
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
        */
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
        /*
        param.getAndSetCategoryValueMap();
        */
        /*
        try {
            componentparam.setUsedsec(time0);
            return new ComponentMLIndicator().calculate2(componentparam);
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
        */
    }

}

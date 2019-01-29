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
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        }
        int daysafterzero = (int) srv.conf.getMACDDaysAfterZero();
        String baseDateStr = stocks.get(stocks.size() - 1 - 1 * daysafterzero - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - 0 * daysafterzero - offset);
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        LocalDate baseDate = TimeUtil.convertDate(baseDateStr);
        LocalDate futureDate = TimeUtil.convertDate(futureDateStr);
    
        srv.conf.setdate(TimeUtil.convertDate(baseDate));
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSMLMACD, Boolean.TRUE);
        Map<String, Map<String, Object>> result0 = srv.getContent();
    
        Map<String, Map<String, Object>> maps = result0;
        //System.out.println("mapkey " + maps.keySet());
        //System.out.println(maps.get("-1").keySet());
        //System.out.println(maps.get("-2").keySet());
        //System.out.println(maps.get("Index").keySet());
        Map mlMACDMaps = (Map) maps.get(PipelineConstants.MLMACD);
        //System.out.println("mlm " + mlMACDMaps.keySet());
        Integer category = (Integer) mlMACDMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) mlMACDMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlMACDMaps.get(PipelineConstants.RESULT);
        if (resultMap == null) {
            return null;
        }
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlMACDMaps.get(PipelineConstants.PROBABILITY);
        List<List> resultMetaArray = (List<List>) mlMACDMaps.get(PipelineConstants.RESULTMETAARRAY);
        //List<ResultMeta> resultMeta = (List<ResultMeta>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<Object> objectList = (List<Object>) mlMACDMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        //System.out.println("m4 " + resultMetaArray);
        //System.out.println("m4 " + resultMap.keySet());
        //System.out.println("m4 " + probabilityMap.keySet());
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        srv.conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        try {
            // TODO add more offset
            // TODO verify dates and offsets
            int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
            return new ComponentMLMACD().calculateMLMACD(market, daysafterzero, baseDate, futureDate, categoryTitle, resultMap, resultMetaArray,
                    categoryValueMap, resultMeta, offset, usedsec, doSave, doPrint);
            //result.config = MyPropertyConfig.instance();
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
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        }
        int futuredays = (int) srv.conf.getAggregatorsIndicatorFuturedays();
        double threshold = srv.conf.getAggregatorsIndicatorThreshold();
        String baseDateStr = stocks.get(stocks.size() - 1 - futuredays - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - offset);
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        LocalDate baseDate = TimeUtil.convertDate(baseDateStr);
        LocalDate futureDate = TimeUtil.convertDate(futureDateStr);
        srv.conf.setdate(TimeUtil.convertDate(baseDate));
        //srv.getTestRecommender(true);
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.TRUE);
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        Map<String, Map<String, Object>> result0 = srv.getContent();
    
        Map<String, Map<String, Object>> maps = result0;
        //System.out.println("mapkey " + maps.keySet());
        //System.out.println(maps.get("-1").keySet());
        //System.out.println(maps.get("-2").keySet());
        //System.out.println(maps.get("Index").keySet());
        Map mlIndicatorMaps = (Map) maps.get(PipelineConstants.MLINDICATOR);
        //System.out.println("mli " + mlIndicatorMaps.keySet());
        Integer category = (Integer) mlIndicatorMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) mlIndicatorMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Object>> resultMap = (Map<String, List<Object>>) mlIndicatorMaps.get(PipelineConstants.RESULT);
        Map<String, List<Double>> probabilityMap = (Map<String, List<Double>>) mlIndicatorMaps.get(PipelineConstants.PROBABILITY);
        List<Object[]> resultMetaArray = (List<Object[]>) mlIndicatorMaps.get(PipelineConstants.RESULTMETAARRAY);
        List<Object> objectList = (List<Object>) mlIndicatorMaps.get(PipelineConstants.RESULTMETA);
        List<ResultMeta> resultMeta = new ObjectMapper().convertValue(objectList, new TypeReference<List<ResultMeta>>() { });
        //System.out.println("m4 " + resultMap.keySet());
        //System.out.println("m4 " + probabilityMap.keySet());
        if (resultMap == null) {
            return null;
        }
        int size = resultMap.values().iterator().next().size();
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
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        srv.conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        try {
            int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
            return new ComponentMLIndicator().calculateMLindicator(market, futuredays, baseDate, futureDate, threshold, resultMap, size, categoryValueMap, resultMeta, categoryTitle, usedsec, doSave, doPrint);
            //result.config = MyPropertyConfig.instance();
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return null;
    }


}

package roart.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import roart.common.config.ConfigConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.ComponentPredictor;
import roart.iclij.model.MemoryItem;
import roart.util.ServiceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PredictionService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public List<MemoryItem> doPredict(String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doPredict(srv, market, offset, aDate, doSave, doPrint);
    }

    public List<MemoryItem> doPredict(ControlService srv, String market, Integer offset, String aDate, boolean doSave, boolean doPrint) throws Exception {
        long time0 = System.currentTimeMillis();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - 1 - index;
            }
        }
        int futuredays = (int) srv.conf.getPredictorLSTMHorizon();
        String baseDateStr = stocks.get(stocks.size() - 1 - futuredays - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - offset);
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        LocalDate baseDate = TimeUtil.convertDate(baseDateStr);
        LocalDate futureDate = TimeUtil.convertDate(futureDateStr);
        srv.conf.setdate(TimeUtil.convertDate(baseDate));
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOR, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATOREXTRAS, "");
        srv.conf.getConfigValueMap().put(ConfigConstants.AGGREGATORSINDICATORRECOMMENDER, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.TRUE);
        srv.conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.TRUE);
        srv.conf.getConfigValueMap().put(ConfigConstants.INDICATORSMACD, Boolean.FALSE);
        Map<String, Map<String, Object>> result0 = srv.getContent();
    
        Map<String, Map<String, Object>> maps = result0;
        if (maps == null) {
            return null;
        }
        //System.out.println("mapkey " + maps.keySet());
        //System.out.println(maps.get("-1").keySet());
        //System.out.println(maps.get("-2").keySet());
        //System.out.println(maps.get("Index").keySet());
        String wantedCat = ServiceUtil.getWantedCategory(maps, PipelineConstants.LSTM);
        if (wantedCat == null) {
            return null;
        }
        Map map = (Map) maps.get(wantedCat).get(PipelineConstants.LSTM);
    
        //System.out.println("lstm " + map.keySet());
        Integer category = (Integer) map.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) map.get(PipelineConstants.CATEGORYTITLE);
        Map<String, List<Double>> resultMap = (Map<String, List<Double>>) map.get(PipelineConstants.RESULT);
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        srv.conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
        return new ComponentPredictor().calculatePredictor(market, futuredays, baseDate, futureDate, categoryTitle, resultMap, categoryValueMap, usedsec, doSave, doPrint);
    }

}

package roart.service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.ComponentRecommender;
import roart.config.IclijXMLConfig;
import roart.iclij.config.IclijConfig;
import roart.iclij.model.MemoryItem;
import roart.util.ServiceUtil;

public class RecommenderService {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public List<MemoryItem> doRecommender(String market, Integer offset, String aDate, boolean doSave, List<String> disableList, boolean doPrint) throws Exception {
        ControlService srv = new ControlService();
        srv.getConfig();
        return doRecommender(srv, market, offset, aDate, doSave, disableList, doPrint);
    }

    public List<MemoryItem> doRecommender(ControlService srv, String market, Integer offset, String aDate, boolean doSave, List<String> disableList, boolean doPrint) throws Exception {
        long time0 = System.currentTimeMillis();
        srv.conf.setMarket(market);
        List<String> stocks = srv.getDates(market);
        if (aDate != null) {
            int index = stocks.indexOf(aDate);
            if (index >= 0) {
                offset = stocks.size() - index;
            }
        }
        int futuredays = (int) srv.conf.getTestIndicatorRecommenderComplexFutureDays();
        if (stocks.size() - 1 - futuredays - offset < 0) {
            int jj = 0;
        }
        String baseDateStr = stocks.get(stocks.size() - 1 - futuredays - offset);
        String futureDateStr = stocks.get(stocks.size() - 1 - offset);
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        LocalDate baseDate = TimeUtil.convertDate(baseDateStr);
        LocalDate futureDate = TimeUtil.convertDate(futureDateStr);
    
        srv.conf.setdate(TimeUtil.convertDate(baseDate));
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        if (instance.wantEvolveRecommender()) {
            srv.getEvolveRecommender(true, disableList);
        }
        srv.conf.getConfigValueMap().put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        srv.conf.getConfigValueMap().put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map<String, Map<String, Object>> maps = srv.getContent(disableList);
        Map recommendMaps = maps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        //System.out.println("m3 " + recommendMaps.keySet());
        if (recommendMaps == null) {
            return null;
        }
        Integer category = (Integer) recommendMaps.get(PipelineConstants.CATEGORY);
        String categoryTitle = (String) recommendMaps.get(PipelineConstants.CATEGORYTITLE);
        Map<String, Map<String, List<Double>>> resultMap = (Map<String, Map<String, List<Double>>>) recommendMaps.get(PipelineConstants.RESULT);
        //System.out.println("m4 " + resultMap.keySet());
        if (resultMap == null) {
            return null;
        }
        Map<String, List<Double>> recommendBuySell = resultMap.get("complex");
        //System.out.println("m5 " + recommendBuySell.keySet());
        Set<Double> buyset = new HashSet<>();
        Set<Double> sellset = new HashSet<>();
        for (String key : recommendBuySell.keySet()) {
            List<Double> vals = recommendBuySell.get(key);
            List v = recommendBuySell.get(key);
            if (vals.get(0) != null) {
                if (v.get(0).getClass().getSimpleName().contains("String")) {
                    int jj = 0;
                }
                buyset.add(vals.get(0));
            }
            if (vals.get(1) != null) {
                sellset.add(vals.get(1));
            }
        }
    
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        Map<String, Map<String, Object>> result = srv.getContent();
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) result.get("" + category).get(PipelineConstants.LIST);
        //System.out.println("k2 " + categoryValueMap.keySet());
        int usedsec = (int) ((System.currentTimeMillis() - time0) / 1000);
        return new ComponentRecommender().calculateRecommender(market, futuredays, baseDate, futureDate, categoryTitle, recommendBuySell,
                result, categoryValueMap, usedsec, doSave, doPrint);
    }

}

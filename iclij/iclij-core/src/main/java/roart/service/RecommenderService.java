package roart.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.constants.RecommendConstants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.component.ComponentRecommender;
import roart.component.model.RecommenderParam;
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
        RecommenderParam param = new RecommenderParam();
        param.setMarket(market);
        param.setDoPrint(doPrint);
        param.setDoSave(doSave);
        srv.conf.setMarket(market);
        int futuredays = (int) srv.conf.getTestIndicatorRecommenderComplexFutureDays();
        param.setFuturedays(futuredays);
        param.setDates(srv, futuredays, offset, aDate);
    
        srv.conf.setdate(TimeUtil.convertDate(param.getBaseDate()));
        IclijConfig instance = IclijXMLConfig.getConfigInstance();
        if (instance.wantEvolveRecommender()) {
            srv.getEvolveRecommender(true, disableList);
        }
        Map<String, Object> setValueMap = new HashMap<>();
        setValueMap.put(ConfigConstants.PREDICTORS, Boolean.FALSE);
        setValueMap.put(ConfigConstants.MACHINELEARNING, Boolean.FALSE);
        Map recommendMaps = (Map) param.getResultMap(srv, PipelineConstants.AGGREGATORRECOMMENDERINDICATOR, setValueMap);
        if (recommendMaps == null) {
            return null;
        }
        param.setCategory(recommendMaps);
        Map<String, Map<String, List<Double>>> resultMap = (Map<String, Map<String, List<Double>>>) recommendMaps.get(PipelineConstants.RESULT);
        //System.out.println("m4 " + resultMap.keySet());
        if (resultMap == null) {
            return null;
        }
        Map<String, List<Double>> recommendBuySell = resultMap.get(RecommendConstants.COMPLEX);
        param.setRecommendBuySell(recommendBuySell);
        //System.out.println("m5 " + recommendBuySell.keySet());
        /*
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
        */
        
        param.getAndSetCategoryValueMap(srv);
        //System.out.println("k2 " + categoryValueMap.keySet());
        param.setUsedsec(time0);
        return new ComponentRecommender().calculateRecommender(param);
    }

}

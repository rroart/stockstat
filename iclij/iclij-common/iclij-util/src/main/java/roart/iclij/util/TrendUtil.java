package roart.iclij.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.iclij.model.Trend;
import roart.iclij.service.ControlService;

public class TrendUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Trend getTrend(int days, LocalDate date, ControlService srv, int startoffset) {
        //log.info("Verify compare date {} with {}", oldDate, date);
        LocalDate futureDate = date;
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        new MLUtil().disabler(srv.conf.getConfigValueMap());
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        //Set<String> i = resultMaps.keySet();
        //Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        //Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Integer category = (Integer) resultMaps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("Price").get(PipelineConstants.LIST);
        //Set<String> j2 = resultMaps.get("" + category).keySet();
    
        VerifyProfit verify = new VerifyProfit();
        //verify.doVerify(listInc, days, true, categoryValueMap, oldDate);
        //verify.doVerify(listDec, days, false, categoryValueMap, oldDate);
        return verify.getTrend(days, categoryValueMap, startoffset);
    }

    public Trend getTrend(int days, LocalDate date, ControlService srv, int startoffset, List<String> stockDates, int loopoffset) {
        //log.info("Verify compare date {} with {}", oldDate, date);
        //LocalDate futureDate = date;
        try {
            srv.conf.setdate(TimeUtil.convertDate(TimeUtil.convertDate(stockDates.get(stockDates.size() - 1))));
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        new MLUtil().disabler(srv.conf.getConfigValueMap());
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        //Set<String> i = resultMaps.keySet();
        //Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        //Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Integer category = (Integer) resultMaps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("Price").get(PipelineConstants.LIST);
        //Set<String> j2 = resultMaps.get("" + category).keySet();
    
        VerifyProfit verify = new VerifyProfit();
        //verify.doVerify(listInc, days, true, categoryValueMap, oldDate);
        //verify.doVerify(listDec, days, false, categoryValueMap, oldDate);
        return verify.getTrend(days, categoryValueMap, startoffset, date, stockDates, loopoffset);
    }

}

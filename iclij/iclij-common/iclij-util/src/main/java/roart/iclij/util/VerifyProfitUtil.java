package roart.iclij.util;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.db.IclijDbDao;
import roart.iclij.config.Market;
import roart.iclij.model.IncDecItem;
import roart.iclij.model.WebData;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.model.config.ActionComponentConfig;
import roart.iclij.service.ControlService;
import roart.service.model.ProfitData;
import roart.service.model.ProfitInputData;

public class VerifyProfitUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void getVerifyProfit(int days, LocalDate date, ControlService srv,
            LocalDate oldDate, List<IncDecItem> listInc, List<IncDecItem> listDec, List<IncDecItem> listIncDec, int startoffset, Double threshold, List<String> stockDates, int loopoffset) {
        log.info("Verify compare date {} with {}", oldDate, date);
        LocalDate futureDate = date;
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        new MLUtil().disabler(srv.conf.getConfigValueMap());
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        Set<String> i = resultMaps.keySet();
        //Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        //Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Integer category = (Integer) resultMaps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("Price").get(PipelineConstants.LIST);
        //Set<String> j2 = resultMaps.get("" + category).keySet();
    
        VerifyProfit verify = new VerifyProfit();
        verify.doVerify(listInc, days, true, categoryValueMap, oldDate, startoffset, threshold, stockDates, loopoffset);
        verify.doVerify(listDec, days, false, categoryValueMap, oldDate, startoffset, threshold, stockDates, loopoffset);
        verify.doVerify(listIncDec, days, false, categoryValueMap, oldDate, startoffset, threshold, stockDates, loopoffset);
        //return verify.getTrend(days, categoryValueMap);
    }

    public void getVerifyProfit(int days, LocalDate date, ControlService srv,
            LocalDate oldDate, List<IncDecItem> listInc, List<IncDecItem> listDec, List<IncDecItem> listIncDec, int startoffset, Double threshold) {
        log.info("Verify compare date {} with {}", oldDate, date);
        LocalDate futureDate = date;
        srv.conf.setdate(TimeUtil.convertDate(futureDate));
        new MLUtil().disabler(srv.conf.getConfigValueMap());
        Map<String, Map<String, Object>> resultMaps = srv.getContent();
        Set<String> i = resultMaps.keySet();
        //Map maps = (Map) resultMaps.get(PipelineConstants.AGGREGATORRECOMMENDERINDICATOR);
        //Integer category = (Integer) maps.get(PipelineConstants.CATEGORY);
        Integer category = (Integer) resultMaps.get(PipelineConstants.META).get(PipelineConstants.WANTEDCAT);
        Map<String, List<List<Double>>> categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("" + category).get(PipelineConstants.LIST);
        //categoryValueMap = (Map<String, List<List<Double>>>) resultMaps.get("Price").get(PipelineConstants.LIST);
        //Set<String> j2 = resultMaps.get("" + category).keySet();
    
        VerifyProfit verify = new VerifyProfit();
        verify.doVerify(listInc, days, true, categoryValueMap, oldDate, startoffset, threshold);
        verify.doVerify(listDec, days, false, categoryValueMap, oldDate, startoffset, threshold);
        verify.doVerify(listIncDec, days, false, categoryValueMap, oldDate, startoffset, threshold);
        //return verify.getTrend(days, categoryValueMap);
    }
}

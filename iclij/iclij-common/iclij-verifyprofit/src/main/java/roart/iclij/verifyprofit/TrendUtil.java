package roart.iclij.verifyprofit;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.TimeUtil;
import roart.iclij.config.Market;
import roart.iclij.model.Trend;
import roart.component.model.ComponentData;

public class TrendUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Trend getTrend(int days, String date, int startoffset, List<String> stockDates, ComponentData componentData, Market market) {
        //log.info("Verify compare date {} with {}", oldDate, date);
        log.info("Use date {} with {} days", date, days);
        try {
            componentData.setFuturedays(0);
            componentData.setOffset(0);
            componentData.setDates(date, stockDates, null, market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        componentData.getAndSetWantedCategoryValueMap();
        Map<String, List<List<Double>>> categoryValueMap = componentData.getCategoryValueMap();
 
        VerifyProfit verify = new VerifyProfit();
        return verify.getTrend(days, categoryValueMap, startoffset);
    }

    public Trend getTrend(int days, String date, int startoffset, List<String> stockDates, int loopoffset, ComponentData componentData, Market market) {
        //log.info("Verify compare date {} with {}", oldDate, date);
        if (date == null) {
            //date = componentData.get
        }
        /*
        LocalDate mydate = TimeUtil.getEqualBefore(stockDates, date);
        mydate.minusDays(loopoffset);
        date = TimeUtil.convertDate2(mydate);
        */
        log.info("Use date {} with {} days", date, days);
        try {
            componentData.setFuturedays(0);
            componentData.setOffset(-loopoffset);
            componentData.setDates(date, stockDates, null, market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        componentData.getAndSetWantedCategoryValueMap();
        Map<String, List<List<Double>>> categoryValueMap = componentData.getCategoryValueMap();
  
        VerifyProfit verify = new VerifyProfit();
        return verify.getTrend(days, categoryValueMap, startoffset, loopoffset);
    }

}

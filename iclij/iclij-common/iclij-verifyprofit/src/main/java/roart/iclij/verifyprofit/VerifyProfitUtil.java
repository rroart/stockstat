package roart.iclij.verifyprofit;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.constants.Constants;
import roart.common.model.IncDecItem;
import roart.common.util.TimeUtil;
import roart.component.model.ComponentData;
import roart.iclij.config.Market;

public class VerifyProfitUtil {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Deprecated
    public void getVerifyProfit(int days, LocalDate date, LocalDate oldDate,
            List<IncDecItem> listInc, List<IncDecItem> listDec, List<IncDecItem> listIncDec, int startoffset, Double threshold, List<String> stockDates, int loopoffset, ComponentData componentData, Market market) {
        log.info("Verify compare date {} with {}", oldDate, date);
        try {
            componentData.setFuturedays(0);
            componentData.setOffset(0);
            componentData.setDates(TimeUtil.convertDate2(oldDate), stockDates, null, market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        componentData.getAndSetWantedCategoryValueMap();
        Map<String, List<List<Double>>> categoryValueMap = componentData.getCategoryValueMap();
        
        VerifyProfit verify = new VerifyProfit();
        verify.doVerify(listInc, days, categoryValueMap, oldDate, startoffset, threshold, stockDates);
        verify.doVerify(listDec, days, categoryValueMap, oldDate, startoffset, threshold, stockDates);
        verify.doVerify(listIncDec, days, categoryValueMap, oldDate, startoffset, threshold, stockDates);
    }

    public void getVerifyProfit(int days, LocalDate date, LocalDate oldDate,
            Collection<IncDecItem> listInc, Collection<IncDecItem> listDec, Collection<IncDecItem> listIncDec, int startoffset, Double threshold, ComponentData componentData, List<String> stockDates, Market market) {
        componentData = new ComponentData(componentData);
        try {
            componentData.setFuturedays(0);
            componentData.setOffset(0);
            componentData.setDates(oldDate != null ? TimeUtil.convertDate2(oldDate) : null, stockDates, null, market);
        } catch (ParseException e) {
            log.error(Constants.EXCEPTION, e);
        }
        log.info("Verify compare date {} with {} threshold {}", componentData.getComponentTime().getFutureDate(), days, threshold);
        LocalDate mydate = componentData.getFutureDate();
        componentData.getAndSetWantedCategoryValueMap();
        Map<String, List<List<Double>>> categoryValueMap = componentData.getCategoryValueMap();
    
        VerifyProfit verify = new VerifyProfit();
        verify.doVerify(listInc, days, categoryValueMap, mydate, startoffset, threshold, stockDates);
        verify.doVerify(listDec, days, categoryValueMap, mydate, startoffset, threshold, stockDates);
        verify.doVerify(listIncDec, days, categoryValueMap, mydate, startoffset, threshold, stockDates);
    }

    public void getVerifyProfit(int days, LocalDate date,
            Collection<IncDecItem> listInc, Collection<IncDecItem> listDec, Collection<IncDecItem> listIncDec, int startoffset, Double threshold, List<String> stockDates, Map<String, List<List<Double>>> categoryValueMap) {
        log.info("Verify compare date {} with {} threshold {}", date, days, threshold);
    
        VerifyProfit verify = new VerifyProfit();
        verify.doVerify(listInc, days, categoryValueMap, date, startoffset, threshold, stockDates);
        verify.doVerify(listDec, days, categoryValueMap, date, startoffset, threshold, stockDates);
        verify.doVerify(listIncDec, days, categoryValueMap, date, startoffset, threshold, stockDates);
    }
}

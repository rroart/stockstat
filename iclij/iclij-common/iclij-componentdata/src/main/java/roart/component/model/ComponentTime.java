package roart.component.model;

import java.text.ParseException;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.util.TimeUtil;
import roart.iclij.config.Market;
import roart.iclij.model.action.MarketActionData;
import roart.iclij.model.component.ComponentInput;
import roart.iclij.service.ControlService;

public class ComponentTime {
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    private LocalDate baseDate;
    
    private LocalDate futureDate;

    private Integer offset;
    
    private Integer futuredays;
    
    public LocalDate getBaseDate() {
        return baseDate;
    }

    public void setBaseDate(LocalDate baseDate) {
        this.baseDate = baseDate;
    }

    public LocalDate getFutureDate() {
        return futureDate;
    }

    public void setFutureDate(LocalDate futureDate) {
        this.futureDate = futureDate;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Integer getFuturedays() {
        return futuredays;
    }

    public void setFuturedays(Integer futuredays) {
        this.futuredays = futuredays;
    }

    public int setDates(String aDate, List<String> stockdates, MarketActionData actionData, Market market, ControlService service, ComponentInput input, String id) throws ParseException {
        String date;
        if (stockdates == null) {
            stockdates = service.getDates(market.getConfig().getMarket(), id);
        }
        int loopoffset = 0;
        Integer offsetMult = input.getLoopoffset();
        if (offsetMult != null) {            
            loopoffset = new ComponentTimeUtil().getFindProfitOffset(market, input);
        }
        if (aDate == null) {
            if (offsetMult != null) {            
                LocalDate start = input.getStartdate();
                start = input.getEnddate(); // temp fix
                String startDate = null;
                if (start == null) {
                    // to avoid negative in TimeUtil
                    startDate = stockdates.get(0 + futuredays);
                } else {
                    startDate = TimeUtil.convertDate2(start);
                    int index = TimeUtil.getIndexEqualAfter(stockdates, startDate);
                    startDate = stockdates.get(index);
                }
                date = startDate;
            } else {
                // differs here
                LocalDate end = input.getEnddate();
                String endDate = null;
                if (end == null) {
                    endDate = stockdates.get(0);
                } else {
                    endDate = TimeUtil.convertDate2(input.getEnddate()); // temp fix
                    // and differs here
                    int index = TimeUtil.getIndexEqualBefore(stockdates, endDate);
                    endDate = stockdates.get(index);
                }
                date = endDate;
            }
        } else {
            date = aDate;
        }
        List<String> list = new TimeUtil().setDates(date, stockdates, loopoffset, this.offset, futuredays);
        String baseDateStr = list.get(0);
        String futureDateStr = list.get(1);
        log.info("Base future date {} {}", baseDateStr, futureDateStr);
        this.baseDate = TimeUtil.convertDate(baseDateStr);
        this.futureDate = TimeUtil.convertDate(futureDateStr);
        if (stockdates.size() - 1 - futuredays - loopoffset < 0) {
            int jj = 0;
        }
        return loopoffset;
    }
    
}

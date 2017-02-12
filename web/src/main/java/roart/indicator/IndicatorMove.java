package roart.indicator;

import java.util.Map;

import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;

public class IndicatorMove extends Indicator {

    Map<String, Integer>[] periodmap;
    int period;

    public IndicatorMove(ControlService controlService, String string, Map<String, Integer>[] periodmap, int period) {
        super(controlService, string);
        this.periodmap = periodmap;
        this.period = period;
    }

    @Override
    public boolean isEnabled() {
        return controlService.isMoveEnabled();
    }

    @Override
    public Object getResultItem(Stock stock) {
        try {
            return periodmap[period].get(stock.getId());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return 0;
    }

}


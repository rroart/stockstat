package roart.indicator;

import java.util.Map;

import roart.config.MyConfig;
import roart.model.StockItem;
//import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;

public class IndicatorMove extends Indicator {

    Map<String, Integer>[] periodmap;
    // TODO fix category/period
    int period;

    public IndicatorMove(MyConfig conf, String string, Map<String, Integer>[] periodmap, int period) {
        super(conf, string, period);
        this.periodmap = periodmap;
        this.period = period;
    }

    @Override
    public boolean isEnabled() {
        return conf.isMoveEnabled();
    }

    @Override
    public Object getResultItem(StockItem stock) {
        try {
            return periodmap[period].get(stock.getId());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return 0;
    }

}


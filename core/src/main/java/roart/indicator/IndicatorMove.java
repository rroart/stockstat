package roart.indicator;

import java.util.Map;

import roart.config.MyMyConfig;
import roart.model.StockItem;
//import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.TaUtil;

public class IndicatorMove extends Indicator {

    Map<String, Integer>[] periodmap;
    // TODO fix category/period
    int period;

    public IndicatorMove(MyMyConfig conf, String string, Map<String, Integer>[] periodmap, int period) {
        super(conf, string, period);
        this.periodmap = periodmap;
        this.period = period;
    }

    @Override
    public boolean isEnabled() {
        return conf.isMoveEnabled();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
    	Object[] retArray = new Object[1];
    	retArray[0] = 0;
        try {
        	retArray[0] = periodmap[period].get(stock.getId());
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        }
        return retArray;
    }

    @Override
    protected Map<String, Object[]> getResultMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap,
            Map<String, Double[]> momMap) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected Map<String, Double[]> getCalculatedMap(MyMyConfig conf, TaUtil tu, Map<String, Object[]> objectMap,
            Map<String, double[]> truncListMap) {
        // TODO Auto-generated method stub
        return null;
    }

}


package roart.indicator.impl;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.constants.Constants;
import roart.model.StockItem;

public class IndicatorMove extends Indicator {

    // TODO fix category/period
    int period;

    public IndicatorMove(MyMyConfig conf, String string, Map<String, Integer>[] periodmap, int period) {
        super(conf, string, period);
        this.periodmap = periodmap;
        this.period = period;
        fieldSize = fieldSize();
    }

    @Override
    public boolean isEnabled() {
        return conf.isMoveEnabled();
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORMOVE;
    }

    private int fieldSize() {
        int size = 1;
        return size;
    }

    @Override
    protected void getFieldResult(MyMyConfig conf, Double[] result, Object[] fields) {
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
    protected Double[] getCalculated(MyMyConfig conf, Map<String, Object[]> objectMap, String id) {
        return null;
    }

}


package roart.indicator.impl;

import java.util.List;
import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.constants.Constants;
import roart.model.StockItem;
import roart.stockutil.StockUtil;

public class IndicatorMove extends Indicator {

    protected Map<String, Integer> periodmap;

    // TODO fix category/period
    int period;

    public IndicatorMove(MyMyConfig conf, String string, List<StockItem>[] datedstocklists, int period) throws Exception {
        super(conf, string, period);
        List<StockItem>[] stocklistPeriod = StockUtil.getListSorted(datedstocklists, 2, period);
        Map<String, Integer>[] periodmap = StockUtil.getListMove(2, stocklistPeriod, period);

        this.periodmap = periodmap[0];
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
            retArray[0] = periodmap.get(stock.getId());
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


package roart.indicator.impl;

import java.util.List;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.SerialTA;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.model.StockDTO;
import roart.stockutil.StockUtil;

public class IndicatorMove extends Indicator {

    protected Map<String, Integer> periodmap;

    // fix category/period
    int period;

    public IndicatorMove(IclijConfig conf, String string, List<StockDTO>[] datedstocklists, int period, Inmemory inmemory) throws Exception {
        super(conf, string, period, null, false, inmemory);
        List<StockDTO>[] stocklistPeriod = StockUtil.getListSorted(datedstocklists, 2, period);
        Map<String, Integer>[] periodmapArray = StockUtil.getListMove(2, stocklistPeriod, period);

        this.periodmap = periodmapArray[0];
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

    @Override
    protected int fieldSize() {
        int size = 1;
        return size;
    }

    @Override
    protected void getFieldResult(Double[] result, Object[] fields) {
    }

    @Override
    public Object[] getResultItem(StockDTO stock) {
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
    protected Double[] getCalculated(Map<String, SerialTA> objectMap, String id) {
        return null;
    }

    @Override
    protected int getAnythingHereRange() {
	return 1;
    }
    
    @Override
    public String getName() {
        return Constants.MOVE;
    }
    
    @Override
    public int getInputArrays() {
        return 1;
    }
    
}


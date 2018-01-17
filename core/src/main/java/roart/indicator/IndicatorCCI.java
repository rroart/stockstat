package roart.indicator;

import java.util.Map;

import roart.config.MyMyConfig;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public class IndicatorCCI extends Indicator {

   public IndicatorCCI(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category, Pipeline[] datareaders, boolean onlyExtra) throws Exception {
        super(conf, string, category);
        this.marketdatamap = marketdatamap;
        this.periodmap = periodmap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        fieldSize = fieldSize();
        if (isEnabled() && !onlyExtra) {
            calculateAll(conf, marketdatamap, periodDataMap, category, datareaders);
        }
        if (wantForExtras()) {
            calculateForExtras(datareaders);
        }
    }

	@Override
    public boolean isEnabled() {
        return conf.isCCIEnabled();
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORCCI;
    }
    
    private int fieldSize() {
        int size = 1;
        if (conf.isCCIDeltaEnabled()) {
            size++;
        }
        emptyField = new Object[size];
        return size;
    }
    
    @Override
    public Object calculate(double[][] array) {
        TaUtil tu = new TaUtil();
        return tu.getCCI(array[1], array[2], array[0], conf.getDays(), conf.isCCIDeltaEnabled(), conf.getCCIDeltaDays());
    }

    @Override
    protected Double[] getCalculated(MyMyConfig conf, Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getRsiAndDelta(conf.getCCIDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(MyMyConfig conf, TaUtil tu, Double[] result, Object[] fields) {
        tu.getRSIAndDelta(conf.isCCIDeltaEnabled(),  result, fields);
    }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getRsiAndDelta(conf.getCCIDeltaDays(), objs, offset);
    }
        
    // TODO call tautil
    @Override
    public int getResultSize() {
        return 2;        
    }

    @Override
    public Object[] getResultItemTitle() {
        Object[] objs = new Object[fieldSize];
        objs[0] = title;
        if (conf.isCCIDeltaEnabled()) {
            objs[1] = Constants.DELTA + title;
        }
        emptyField = new Double[fieldSize];
        return objs;
    }

}


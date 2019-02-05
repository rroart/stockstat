package roart.indicator.impl;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.constants.Constants;
import roart.pipeline.Pipeline;
import roart.talib.util.TaUtil;

public class IndicatorATR extends Indicator {

    // TODO extend to three cats
    public IndicatorATR(MyMyConfig conf, String string, String title, int category, Pipeline[] datareaders, boolean onlyExtra) throws Exception {
        super(conf, string, category);
        this.key = title;
        fieldSize = fieldSize();
        if (isEnabled() && !onlyExtra) {
            calculateAll(category, datareaders);
        }
        if (wantForExtras()) {
            calculateForExtras(datareaders);
        }
    }

	@Override
    public boolean isEnabled() {
        return conf.isATREnabled();
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORATR;
    }
    
    private int fieldSize() {
        int size = 1;
        if (conf.isATRDeltaEnabled()) {
            size++;
        }
        emptyField = new Object[size];
        return size;
    }
    
    @Override
    public Object calculate(double[][] array) {
        if (array.length != 180 && array.length > 0) {
            log.info("180");
        }
        TaUtil tu = new TaUtil();
        return tu.getATR(array[1], array[2], array[0], conf.getDays(), conf.isATRDeltaEnabled(), conf.getATRDeltaDays());
    }

    @Override
    protected void getFieldResult(Double[] result, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getRSIAndDelta(conf.isATRDeltaEnabled(),  result, fields);
    }

    @Override
    protected Double[] getCalculated(Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getRsiAndDelta(conf.getRSIDeltaDays(), objs);
    }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getRsiAndDelta(conf.getRSIDeltaDays(), objs, offset);
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
        if (conf.isATRDeltaEnabled()) {
            objs[1] = Constants.DELTA + title;
        }
        emptyField = new Double[fieldSize];
        return objs;
    }

}


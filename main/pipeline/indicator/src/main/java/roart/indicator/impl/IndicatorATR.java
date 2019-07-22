package roart.indicator.impl;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.constants.Constants;
import roart.pipeline.Pipeline;
import roart.talib.Ta;
import roart.talib.impl.TalibATR;
import roart.talib.util.TaUtil;

public class IndicatorATR extends Indicator {

    // extend to three cats
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
    public boolean wantForExtras() {
        return conf.wantAggregatorsIndicatorExtrasATR();        
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
        Ta tu = new TalibATR();
        return tu.calculate(array);
    }

    @Override
    protected void getFieldResult(Double[] result, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getWithOneAndDelta(conf.isATRDeltaEnabled(),  result, fields);
    }

    @Override
    protected Double[] getCalculated(Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getATRDeltaDays(), objs);
    }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getATRDeltaDays(), objs, offset);
    }

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

    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORATRRESULT, calculatedMap);
        map.put(PipelineConstants.INDICATORATROBJECT, objectMap);
        map.put(PipelineConstants.INDICATORATRLIST, listMap);
        return map;
    }

    @Override
    protected int getAnythingHereRange() {
        return 3;
    }

    @Override
    protected boolean anythingHere(Map<String, Double[][]> listMap) {
        return anythingHere3(listMap);
    }

    @Override
    protected Boolean wantPercentizedPriceIndex() {
        return false;
    }

}


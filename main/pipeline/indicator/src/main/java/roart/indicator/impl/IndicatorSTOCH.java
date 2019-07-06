package roart.indicator.impl;

import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.constants.Constants;
import roart.pipeline.Pipeline;
import roart.talib.Ta;
import roart.talib.impl.TalibSTOCH;
import roart.talib.util.TaUtil;

public class IndicatorSTOCH extends Indicator {

    public IndicatorSTOCH(MyMyConfig conf, String string, String title, int category, Pipeline[] datareaders, boolean onlyExtra) throws Exception {
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
        return conf.isSTOCHEnabled();
    }

    @Override
    public boolean wantForExtras() {
        return conf.wantAggregatorsIndicatorExtrasSTOCH();        
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORSTOCH;
    }
    
    private int fieldSize() {
        int size = 2;
        if (conf.isSTOCHDeltaEnabled()) {
            size += 2;
        }
        emptyField = new Object[size];
        return size;
    }
    
    @Override
    public Object calculate(double[][] array) {
        if (array.length != 180 && array.length > 0) {
            log.info("180");
        }
        Ta tu = new TalibSTOCH();
        return tu.calculate(array);
    }

    @Override
    protected Double[] getCalculated(Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getWithTwoAndDelta(conf.getSTOCHDeltaDays(), conf.getSTOCHDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(Double[] result, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getWithTwoAndDelta(conf.isSTOCHDeltaEnabled(), conf.isSTOCHDeltaEnabled(), result, fields);
    }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getWithTwoAndDelta(conf.getSTOCHDeltaDays(), conf.getSTOCHDeltaDays(), objs, offset);
    }
    
    @Override
    public int getResultSize() {
        int size = 2;
        if (conf.isSTOCHDeltaEnabled()) {
            size += 2;
        }
        return size;        
    }

    @Override
    public Object[] getResultItemTitle() {
        int size = 2;
        if (conf.isSTOCHDeltaEnabled()) {
            size += 2;
        }
        Object[] objs = new Object[size];
        objs[0] = title;
        objs[1] = title + "2";
        if (conf.isSTOCHDeltaEnabled()) {
            objs[2] = Constants.DELTA + title;
            objs[3] = Constants.DELTA + title + "2";
        }
        return objs;
    }

    @Override
    protected int getAnythingHereRange() {
	return 3;
    }

    @Override
    protected boolean anythingHere(Map<String, Double[][]> listMap) {
        return anythingHere3(listMap);
    }

}


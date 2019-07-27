package roart.indicator.impl;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.constants.Constants;
import roart.pipeline.Pipeline;
import roart.talib.Ta;
import roart.talib.impl.Ta4jSTOCHRSI;
import roart.talib.impl.TalibSTOCHRSI;
import roart.talib.util.TaUtil;

public class IndicatorSTOCHRSI extends Indicator {

    public IndicatorSTOCHRSI(MyMyConfig conf, String string, String title, int category, Pipeline[] datareaders, boolean onlyExtra) throws Exception {
        super(conf, string, category);
        this.key = title;
        if (isEnabled() && !onlyExtra) {
            calculateAll(category, datareaders);
        }
        if (wantForExtras()) {
            calculateForExtras(datareaders);
        }
    }

    @Override
    public boolean isEnabled() {
        return conf.isSTOCHRSIEnabled();
    }

    @Override
    public boolean wantForExtras() {
        return conf.wantAggregatorsIndicatorExtrasSTOCHRSI();        
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORSTOCHRSI;
    }
    
    @Override
    protected int fieldSize() {
        int size = 1;
        if (conf.isSTOCHRSIDeltaEnabled()) {
            size += 1;
        }
        emptyField = new Object[size];
        return size;
    }
    
    @Override
    public Object calculate(double[][] array) {
        if (array.length != 180 && array.length > 0) {
            log.info("180");
        }
        Ta tu = new Ta4jSTOCHRSI();
        return tu.calculate(array);
    }

   @Override
    protected Double[] getCalculated(Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getSTOCHRSIDeltaDays(), objs);
    }

   @Override
   protected void getFieldResult(Double[] result, Object[] fields) {
       TaUtil tu = new TaUtil();
       tu.getWithOneAndDelta(conf.isSTOCHRSIDeltaEnabled(), result, fields);
   }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getSTOCHRSIDeltaDays(), objs, offset);
    }
    
    @Override
    public int getResultSize() {
        int size = 1;
        if (conf.isSTOCHRSIDeltaEnabled()) {
            size += 1;
        }
        return size;    
    }

    @Override
    public Object[] getResultItemTitle() {
        /*
    	int size = 2;
    	if (conf.isSTOCHRSIDeltaEnabled()) {
    		size += 2;
    	}
    	Object[] objs = new Object[size];
    	objs[0] = title;
        objs[1] = title + "2";
    	if (conf.isSTOCHRSIDeltaEnabled()) {
    		objs[2] = Constants.DELTA + title;
            objs[3] = Constants.DELTA + title + "2";
    	}
    	*/
        Object[] objs = new Object[fieldSize];
        objs[0] = title;
        if (conf.isSTOCHRSIDeltaEnabled()) {
            objs[1] = Constants.DELTA + title;
        }
        emptyField = new Double[fieldSize];

        return objs;
    }

    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORSTOCHRSIRESULT, calculatedMap);
        map.put(PipelineConstants.INDICATORSTOCHRSILIST, listMap);
        map.put(PipelineConstants.INDICATORSTOCHRSIOBJECT, objectMap);
        return map;
    }

    @Override
    protected int getAnythingHereRange() {
	return 1;
    }
}


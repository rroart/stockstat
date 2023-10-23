package roart.indicator.impl;

import java.util.HashMap;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.constants.Constants;
import roart.ml.common.MLClassifyModel;
import roart.pipeline.Pipeline;
import roart.talib.Ta;
import roart.talib.impl.TalibMACD;
import roart.talib.util.TaUtil;

public class IndicatorMACD extends Indicator {

    Map<MLClassifyModel, Long> mapTime = new HashMap<>();
    
    public IndicatorMACD(IclijConfig conf, String string, String title, int category, PipelineData[] datareaders, boolean onlyExtra) throws Exception {
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
        return conf.isMACDEnabled();
    }

    @Override
    public boolean wantForExtras() {
        return conf.wantAggregatorsIndicatorExtrasMACD();        
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORMACD;
    }
    
    @Override
    protected int fieldSize() {
        int size = 3;
        if (conf.isMACDDeltaEnabled()) {
            size++;
        }
        if (conf.isMACDHistogramDeltaEnabled()) {
            size++;
        }
        if (conf.isMACDSignalDeltaEnabled()) {
            size++;
        }
        emptyField = new Object[size];
        log.info("fieldsizet {}", size);
        return size;
    }
    
    @Override
    public Object calculate(double[][] array) {
        Ta tu = new TalibMACD();
        return tu.calculate(array);
    }

    @Override
    protected Double[] getCalculated(Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getWithThreeAndDelta(conf.getMACDHistogramDeltaDays(), conf.getMACDDeltaDays(), conf.getMACDSignalDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(Double[] momentum, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getWithThreeAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), conf.isMACDSignalDeltaEnabled(), momentum, fields);
    }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getWithThreeAndDelta(conf.getMACDHistogramDeltaDays(), conf.getMACDDeltaDays(), conf.getMACDDeltaDays(), objs, offset);

    }
    
    @Override
    public int getResultSize() {
        return 6;        
    }
    
    @Override
    public Object[] getResultItemTitle() {
        Object[] objs = new Object[fieldSize];
        int retindex = 0;
        objs[retindex++] = title + Constants.WEBBR + "hist";
        if (conf.isMACDHistogramDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "hist";
        }
        objs[retindex++] = title + Constants.WEBBR + "macd";
        if (conf.isMACDDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "macd";
        }
        objs[retindex++] = title + Constants.WEBBR + "sig";
        if (conf.isMACDSignalDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "sig";
        }
        log.info("fieldsizet {}", retindex);
        return objs;
    }

    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORMACDRESULT, calculatedMap);
        map.put(PipelineConstants.INDICATORMACDOBJECT, objectMap);
        map.put(PipelineConstants.INDICATORMACDLIST, getListMap());
        return map;
    }

    @Override
    protected int getAnythingHereRange() {
	return 1;
    }
    
    @Override
    public String getName() {
        return Constants.MACD;
    }
    
}


package roart.indicator.impl;

import java.util.HashMap;
import java.util.Map;

import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.constants.Constants;
import roart.ml.common.MLClassifyModel;
import roart.pipeline.Pipeline;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.talib.util.TaUtil;

public class IndicatorMACD extends Indicator {

    Map<MLClassifyModel, Long> mapTime = new HashMap<>();
    
    public IndicatorMACD(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category, Pipeline[] datareaders, boolean onlyExtra) throws Exception {
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
    
    private int fieldSize() {
        int size = 2;
        if (conf.isMACDDeltaEnabled()) {
            size++;
        }
        if (conf.isMACDHistogramDeltaEnabled()) {
            size++;
        }
        emptyField = new Object[size];
        log.info("fieldsizet {}", size);
        return size;
    }
    
    @Override
    public Object calculate(double[][] array) {
        if (array.length != 180 && array.length > 0) {
            log.info("180");
        }
        TaUtil tu = new TaUtil();
        return tu.getMomAndDeltaFull(array[0], conf.getDays(), conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays());
    }

    @Override
    protected Double[] getCalculated(MyMyConfig conf, Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(MyMyConfig conf, Double[] momentum, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getMomAndDelta(conf.isMACDHistogramDeltaEnabled(), conf.isMACDDeltaEnabled(), momentum, fields);
    }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getMomAndDelta(conf.getMACDDeltaDays(), conf.getMACDHistogramDeltaDays(), objs, offset);

    }
    
    // TODO call tautil
    @Override
    public int getResultSize() {
        return 4;        
    }
    
    @Override
    public Object[] getResultItemTitle() {
        Object[] objs = new Object[fieldSize];
        int retindex = 0;
        objs[retindex++] = title + Constants.WEBBR + "hist";
        if (conf.isMACDHistogramDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "hist";
        }
        objs[retindex++] = title + Constants.WEBBR + "mom";
        if (conf.isMACDDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        log.info("fieldsizet {}", retindex);
        return objs;
    }

    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORMACDRESULT, calculatedMap);
        map.put(PipelineConstants.INDICATORMACDOBJECT, objectMap);
        map.put(PipelineConstants.INDICATORMACDLIST, listMap);
        return map;
    }
    
}


package roart.indicator;

import java.util.HashMap;
import java.util.Map;

import roart.config.MyMyConfig;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.TaUtil;

public class IndicatorRSI extends Indicator {

    public IndicatorRSI(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category, Pipeline[] datareaders, boolean onlyExtra) throws Exception {
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
        return conf.isRSIEnabled();
    }

    @Override
    public boolean wantForExtras() {
        return conf.wantAggregatorsIndicatorExtrasRSI();        
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORRSI;
    }

    private int fieldSize() {
        int size = 1;
        if (conf.isRSIDeltaEnabled()) {
            size++;
        }
        emptyField = new Object[size];
        return size;
    }

    @Override
    public Object calculate(double[][] array) {
        TaUtil tu = new TaUtil();
        return tu.getRsiAndDeltaFull(array[0], conf.getDays(), conf.getRSIDeltaDays());
    }

    @Override
    protected Double[] getCalculated(MyMyConfig conf, Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getRsiAndDelta(conf.getRSIDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(MyMyConfig conf, TaUtil tu, Double[] momentum, Object[] fields) {
        tu.getRSIAndDelta(conf.isRSIDeltaEnabled(),  momentum, fields);
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
        if (conf.isRSIDeltaEnabled()) {
            objs[1] = Constants.DELTA + title;
        }
        emptyField = new Double[fieldSize];
        return objs;
    }

    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORRSIRESULT, calculatedMap);
        map.put(PipelineConstants.INDICATORRSILIST, listMap);
        map.put(PipelineConstants.INDICATORRSIOBJECT, objectMap);
        return map;
    }

}


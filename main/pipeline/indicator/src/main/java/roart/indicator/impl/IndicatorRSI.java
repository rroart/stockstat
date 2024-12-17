package roart.indicator.impl;

import java.util.HashMap;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialTA;
import roart.common.constants.Constants;
import roart.pipeline.Pipeline;
import roart.talib.Ta;
import roart.talib.impl.TalibRSI;
import roart.talib.util.TaUtil;

public class IndicatorRSI extends Indicator {

    public IndicatorRSI(IclijConfig conf, String string, String title, int category, PipelineData[] datareaders, boolean onlyExtra) {
        super(conf, string, category, datareaders, onlyExtra);
        this.key = title;
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

    @Override
    protected int fieldSize() {
        int size = 1;
        if (conf.isRSIDeltaEnabled()) {
            size++;
        }
        emptyField = new Object[size];
        return size;
    }

    @Override
    public SerialTA calculate(double[][] array) {
        Ta tu = new TalibRSI();
        return tu.calculate(array);
    }

    @Override
    protected Double[] getCalculated(Map<String, SerialTA> objectMap, String id) {
        SerialTA objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getRSIDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(Double[] momentum, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getWithOneAndDelta(conf.isRSIDeltaEnabled(),  momentum, fields);
    }

    @Override
    public Object[] getDayResult(SerialTA objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getRSIDeltaDays(), objs, offset);
    }

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
        map.put(PipelineConstants.INDICATORRSILIST, getListMap());
        map.put(PipelineConstants.INDICATORRSIOBJECT, objectMap);
        return map;
    }

    @Override
    protected int getAnythingHereRange() {
	return 1;
    }

    @Override
    public String getName() {
        return Constants.RSI;
    }
    
}


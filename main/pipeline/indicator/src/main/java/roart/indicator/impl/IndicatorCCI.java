package roart.indicator.impl;

import java.util.HashMap;
import java.util.Map;

import roart.iclij.config.IclijConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialTA;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.pipeline.Pipeline;
import roart.talib.Ta;
import roart.talib.impl.TalibCCI;
import roart.talib.util.TaUtil;

public class IndicatorCCI extends Indicator {

    public IndicatorCCI(IclijConfig conf, String string, String title, int category, PipelineData[] datareaders, boolean onlyExtra, Inmemory inmemory) {
        super(conf, string, category, datareaders, onlyExtra, inmemory);
        this.key = title;
    }

    @Override
    public boolean isEnabled() {
        return conf.isCCIEnabled();
    }

    @Override
    public boolean wantForExtras() {
        return conf.wantAggregatorsIndicatorExtrasCCI();        
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORCCI;
    }

    @Override
    protected int fieldSize() {
        int size = 1;
        if (conf.isCCIDeltaEnabled()) {
            size++;
        }
        emptyField = new Object[size];
        return size;
    }

    @Override
    public SerialTA calculate(double[][] array) {
        Ta tu = new TalibCCI();
        return tu.calculate(array);
    }

    @Override
    protected Double[] getCalculated(Map<String, SerialTA> objectMap, String id) {
        SerialTA objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getCCIDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(Double[] result, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getWithOneAndDelta(conf.isCCIDeltaEnabled(),  result, fields);
    }

    @Override
    public Object[] getDayResult(SerialTA objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getWithOneAndDelta(conf.getCCIDeltaDays(), objs, offset);
    }

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

    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORCCIRESULT, calculatedMap);
        map.put(PipelineConstants.INDICATORCCIOBJECT, objectMap);
        map.put(PipelineConstants.INDICATORCCILIST, getListMap());
        return map;
    }

    @Override
    protected int getAnythingHereRange() {
        return 3;
    }

    @Override
    public boolean anythingHere(Map<String, Double[][]> listMap) {
        return anythingHere3(listMap);
    }

    @Override
    protected Boolean wantPercentizedPriceIndex() {
        return false;
    }

    @Override
    public String getName() {
        return Constants.CCI;
    }
    
}


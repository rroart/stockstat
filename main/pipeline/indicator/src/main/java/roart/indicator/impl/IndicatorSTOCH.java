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
import roart.talib.impl.TalibSTOCH;
import roart.talib.util.TaUtil;

public class IndicatorSTOCH extends Indicator {

    public IndicatorSTOCH(IclijConfig conf, String string, String title, int category, PipelineData[] datareaders, boolean onlyExtra, Inmemory inmemory) {
        super(conf, string, category, datareaders, onlyExtra, inmemory);
        this.key = title;
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
    
    @Override
    protected int fieldSize() {
        int size = 2;
        if (conf.isSTOCHDeltaEnabled()) {
            size += 2;
        }
        emptyField = new Object[size];
        return size;
    }
    
    @Override
    public SerialTA calculate(double[][] array) {
        Ta tu = new TalibSTOCH();
        return tu.calculate(array);
    }

    @Override
    public int getInputArrays() {
        return new TalibSTOCH().getInputArrays();
    }
    
    @Override
    protected Double[] getCalculated(Map<String, SerialTA> objectMap, String id) {
        SerialTA objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        return tu.getWithTwoAndDelta(conf.getSTOCHDeltaDays(), conf.getSTOCHDeltaDays(), objs);
    }

    @Override
    protected void getFieldResult(Double[] result, Object[] fields) {
        TaUtil tu = new TaUtil();
        tu.getWithTwoAndDelta(conf.isSTOCHDeltaEnabled(), conf.isSTOCHDeltaEnabled(), result, fields);
    }

    @Override
    public Object[] getDayResult(SerialTA objs, int offset) {
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
        objs[0] = title + "k";
        objs[1] = title + "d";
        if (conf.isSTOCHDeltaEnabled()) {
            objs[2] = Constants.DELTA + title + "k";
            objs[3] = Constants.DELTA + title + "d";
        }
        return objs;
    }

    @Override
    public Map<String, Object> getResultMap() {
        Map<String, Object> map = new HashMap<>();
        map.put(PipelineConstants.INDICATORSTOCHRESULT, calculatedMap);
        map.put(PipelineConstants.INDICATORSTOCHOBJECT, objectMap);
        map.put(PipelineConstants.INDICATORSTOCHLIST, getListMap());
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
        return Constants.STOCH;
    }
    
}


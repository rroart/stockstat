package roart.indicator;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import roart.config.MyMyConfig;
import roart.model.StockItem;
import roart.pipeline.Pipeline;
import roart.pipeline.PipelineConstants;
//import roart.model.Stock;
import roart.service.ControlService;
import roart.util.Constants;
import roart.util.MarketData;
import roart.util.PeriodData;
import roart.util.StockDao;
import roart.util.TaUtil;

// TODO warning this looks weird, avoid for now?

public class IndicatorSTOCHRSI extends Indicator {

    public IndicatorSTOCHRSI(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, Map<String, Integer>[] periodmap, String title, int category, Pipeline[] datareaders, boolean onlyExtra) throws Exception {
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
        return conf.isSTOCHRSIEnabled();
    }

    @Override
    public String indicatorName() {
        return PipelineConstants.INDICATORSTOCHRSI;
    }
    
    private int fieldSize() {
        int size = 2;
        if (conf.isSTOCHRSIDeltaEnabled()) {
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
        TaUtil tu = new TaUtil();
        Object[] objs = tu.getSTOCHRSI(array, conf.getDays(), conf.getSTOCHRSIDeltaDays());
        return objs;
    }

   @Override
    protected Double[] getCalculated(MyMyConfig conf, Map<String, Object[]> objectMap, String id) {
        Object[] objs = objectMap.get(id);
        TaUtil tu = new TaUtil();
        Double[] stoch = tu.getSRSIAndDelta(conf.getSTOCHRSIDeltaDays(), conf.getSTOCHRSIDeltaDays(), objs);
        return stoch;
    }

   @Override
   protected void getFieldResult(MyMyConfig conf, TaUtil tu, Double[] result, Object[] fields) {
       int retindex = tu.getSRSIAndDelta(conf.isSTOCHRSIDeltaEnabled(), conf.isSTOCHRSIDeltaEnabled(), result, fields);
   }

    @Override
    public Object[] getDayResult(Object[] objs, int offset) {
        TaUtil tu = new TaUtil();
        return tu.getSRSIAndDelta(conf.getSTOCHRSIDeltaDays(), conf.getSTOCHRSIDeltaDays(), objs, offset);
    }
    
    // TODO call tautil
    @Override
    public int getResultSize() {
        int size = 2;
        if (conf.isSTOCHRSIDeltaEnabled()) {
            size += 2;
        }
        return size;    
    }

    @Override
    public Object[] getResultItemTitle() {
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
        return objs;
    }

}


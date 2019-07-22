package roart.aggregator.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.aggregator.impl.IndicatorAggregator.MergeSubType;
import roart.aggregator.impl.IndicatorAggregator.MySubType;
import roart.aggregator.impl.IndicatorAggregator.SubType;
import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.db.dao.DbDao;
import roart.executor.MyExecutors;
import roart.pipeline.Pipeline;
import roart.indicator.util.IndicatorUtils;
import roart.common.constants.Constants;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.common.MLClassifyModel;
import roart.model.StockItem;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.model.data.PeriodData;
import roart.talib.Ta;
import roart.talib.impl.TalibMACD;
import roart.talib.util.TaConstants;

public class MLMACD extends IndicatorAggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> getResultMap() {
        return new HashMap<>();
    }

    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }

    public MLMACD(MyMyConfig conf, String string, List<StockItem> stocks, Map<String, PeriodData> periodDataMap, 
            String title, int category, AbstractCategory[] categories, Map<String, String> idNameMap, Pipeline[] datareaders) throws Exception {
        super(conf, string, category, title, idNameMap, categories, datareaders);
    }

    private abstract class MacdSubType extends MergeSubType {
        public MacdSubType(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(afterbefore);
            this.listMap = (Map<String, Double[][]>) list;
            this.taMap = (Map<String, Object[]>) taObject;
            this.resultMap = (Map<String, Double[]>) resultObject;
            this.afterbefore = afterbefore;
            this.range = range;
            this.filters = new Filter[] { new Filter(true, 0, shortneg), new Filter(false, 0, shortpos) };
        }

        public MacdSubType() {
            super(getAfterBefore());
        }
    }

    private class MacdSubTypeHist extends MacdSubType {
        public MacdSubTypeHist(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
            this.mySubType = MySubType.MACDHIST;
        }

        public MacdSubTypeHist() {
        }
        
        @Override
        public String getType() {
            return "H";
        }
        
        @Override
        public String getName() {
            return "Hist";
        }
        
        @Override
        public int getArrIdx() {
            return TalibMACD.MACDIDXHIST;
        }
    }

    private class MacdSubTypeMacd extends MacdSubType {
        public MacdSubTypeMacd(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
            this.mySubType = MySubType.MACDMACD;
       }
        
        public MacdSubTypeMacd() {
        }
        
        @Override
        public String getType() {
            return "M";
        }
        @Override
        public String getName() {
            return "Macd";
        }
        @Override
        public int getArrIdx() {
            return TalibMACD.MACDIDXMACD;
        }
    }

    private class MacdSubTypeSignal extends MacdSubType {
        public MacdSubTypeSignal(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
            this.mySubType = MySubType.MACDSIG;
        }
        
        public MacdSubTypeSignal() {
        }
        
        @Override
        public String getType() {
            return "S";
        }
        
        @Override
        public String getName() {
            return "Sig";
        }
        
        @Override
        public int getArrIdx() {
            return TalibMACD.MACDIDXSIGN;
        }
    }

    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }

    @Override
    public List<Integer> getTypeList() {
        return getMapTypeList();
    }

    @Override
    protected List<SubType> getWantedSubTypes(AbstractCategory cat, AfterBeforeLimit afterbefore) {
        List<SubType> wantedSubTypesList = new ArrayList<>();
        Object list = cat.getResultMap().get(PipelineConstants.INDICATORMACDLIST);
        Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORMACDOBJECT);
        Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD).get(PipelineConstants.RESULT);
        if (conf.wantMLHist()) {
            wantedSubTypesList.add(new MacdSubTypeHist(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
        }
        if (conf.wantMLMacd()) {
            wantedSubTypesList.add(new MacdSubTypeMacd(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
        }
        if (conf.wantMLSignal()) {
            wantedSubTypesList.add(new MacdSubTypeSignal(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
        }
        return wantedSubTypesList;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getMLMACDMLConfig();
    }
    
    static <K, V> Map<K, V> mapGetterOrig(Map<String, Map<K, V>> mapMap, String key) {
        Map<K, V> map = mapMap.get(key);
        if (map == null) {
            map = new HashMap<>();
            mapMap.put(key, map);
        }
        return map;
    }

    @Override
    protected void printSignChange(String txt, String id, Map<Integer, Integer> posneg, boolean positive, int listsize, int daysAfterZero, Map<Double, String> labelMapShort) {
        String pnString = positive ? "negative" : "positive";
        if (!posneg.isEmpty()) {
            int posnegmaxind = Collections.max(posneg.keySet());
            int posnegmax = posneg.get(posnegmaxind);
            if (posnegmax + 1 == listsize) {
                return;
            }
            if (posnegmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to " + (pnString) + " since " + (listsize - posnegmax), getName(id), id);
            }
        }
        /*
        if (!neg.isEmpty()) {
            int negmaxind = Collections.max(neg.keySet());
            int negmax = neg.get(negmaxind);
            if (negmax + 1 == listsize) {
                return;
            }
            if (negmax + daysAfterZero >= listsize) {
                addEventRow(txt + " sign changed to positive since " + (listsize - negmax), getName(id), id);
            }
        }
        */
    }

    public static void printout(Double[] type, String id, Map<Double, String> labelMapShort) {
        if (type != null) {
            //log.debug("Type " + labelMapShort.get(type[0]) + " id " + id);
        }
    }

    @Override
    public boolean isEnabled() {
        return conf.wantMLMACD();
    }

    @Override
    public Object calculate(double[][] array) {
        Ta tu = new TalibMACD();
        return tu.calculate(array);
    }

    @Override
    protected AfterBeforeLimit getAfterBefore() {
        return new AfterBeforeLimit(conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero());
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
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        objs[retindex++] = title + Constants.WEBBR + "sig";
        if (conf.isMACDSignalDeltaEnabled()) {
            objs[retindex++] = title + Constants.WEBBR + Constants.DELTA + "mom";
        }
        retindex = getTitles(retindex, objs);
        log.debug("fieldsizet {}", retindex);
        return objs;
    }

    @Override
    protected int fieldSize() {
        int size = 0;
        List<SubType> subTypes = usedSubTypes();
        for (SubType subType : subTypes) {
            for (MLClassifyDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
        }
        emptyField = new Object[size];
        return size;
    }

    @Override
    public String getName() {
        return PipelineConstants.MLMACD;
    }

    /*
    @Override
    protected List<SubType> wantedMergeSubTypes() {
        List<SubType> list = new ArrayList<>();
        if (conf.wantMLHist()) {
            list.add(new MacdSubTypeHist());
        }
        if (conf.wantMLMacd()) {
            list.add(new MacdSubTypeMacd());
        }
        if (conf.wantMLSignal()) {
            list.add(new MacdSubTypeSignal());
        }
        return list;
    }
*/
}


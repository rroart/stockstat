package roart.aggregator.impl;

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

import roart.aggregator.impl.IndicatorAggregator.AfterBeforeLimit;
import roart.aggregator.impl.IndicatorAggregator.Filter;
import roart.aggregator.impl.IndicatorAggregator.SubType;
import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.executor.MyExecutors;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.model.StockItem;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.aggregate.Aggregator;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.talib.Ta;
import roart.talib.impl.TalibMACD;
import roart.talib.util.TaConstants;

public class MLRSI extends IndicatorAggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    // save and return this map
    // need getters for this and not? buy/sell
    @Override
    public Map<String, Object> getResultMap() {
        return new HashMap<>();
    }

    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }

    public MLRSI(MyMyConfig conf, String string, List<StockItem> stocks, Map<String, PeriodData> periodDataMap, 
            String title, int category, AbstractCategory[] categories, Map<String, String> idNameMap, Pipeline[] datareaders) throws Exception {
        super(conf, string, category, title, idNameMap, categories, datareaders);
    }

    private abstract class RsiSubType extends MergeSubType {
        public RsiSubType(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(afterbefore);
            this.listMap = (Map<String, Double[][]>) list;
            this.taMap = (Map<String, Object[]>) taObject;
            this.resultMap = (Map<String, Double[]>) resultObject;
            this.afterbefore = afterbefore;
            this.range = range;
        }
        
        public RsiSubType() {
            super(getAfterBefore());
        }
    }

    private class SubTypeRSI extends RsiSubType {
        public SubTypeRSI(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range, MyMyConfig conf) {
            super(list, taObject, resultObject, afterbefore, range);
            double high = conf.getMLRSIBuyRSILimit();
            double low = conf.getMLRSISellRSILimit();
            this.filters = new Filter[] { new Filter(true, high, shortpos), new Filter(false, low, shortneg) };
            this.mySubType = MySubType.RSI;
        }
        public SubTypeRSI() {
            // TODO Auto-generated constructor stub
        }
        @Override
        public String getType() {
            return "R";
        }
        @Override
        public String getName() {
            return "RSI";
        }
        @Override
        public int getArrIdx() {
            return TaConstants.ONEIDXARRONE;
        }
    }

    private class SubTypeSRSI extends RsiSubType {
        public SubTypeSRSI(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range, MyMyConfig conf) {
            super(list, taObject, resultObject, afterbefore, range);
            double high = conf.getMLRSIBuySRSILimit();
            double low = conf.getMLRSISellSRSILimit();
            this.filters = new Filter[] { new Filter(true, high, shortpos), new Filter(false, low, shortneg) };
            this.mySubType = MySubType.STOCHRSI;
        }
        public SubTypeSRSI() {
            // TODO Auto-generated constructor stub
        }
        @Override
        public String getType() {
            return "S";
        }
        @Override
        public String getName() {
            return "STOCHRSI";
        }
        @Override
        public int getArrIdx() {
            return TaConstants.ONEIDXARRONE;
        }
    }

    private class MergeSubTypeRSI extends SubTypeRSI {

        public MergeSubTypeRSI(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore,
                int[] range, MyMyConfig conf) {
            super(list, taObject, resultObject, afterbefore, range, conf);
         }

        public MergeSubTypeRSI(AfterBeforeLimit afterBefore) {
            super(null, null, null, afterBefore, null, null);
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
        if (conf.wantMLRSI()) {
            if (conf.isRSIEnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORRSILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORRSIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORRSI).get(PipelineConstants.RESULT);
                wantedSubTypesList.add(new SubTypeRSI(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf));
            }
            if (conf.isSTOCHRSIEnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHRSILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHRSIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORSTOCHRSI).get(PipelineConstants.RESULT);
                wantedSubTypesList.add(new SubTypeSRSI(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf));
            }
        }
        return wantedSubTypesList;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getMLRSIMLConfig();
    }
    
    @Override
    protected AfterBeforeLimit getAfterBefore() {
        return new AfterBeforeLimit(conf.getMLRSIDaysBeforeLimit(), conf.getMLRSIDaysAfterLimit());
    }
    
    static <K, V> Map<K, V> mapGetterOrig(Map<String, Map<K, V>> mapMap, String key) {
        Map<K, V> map = mapMap.get(key);
        if (map == null) {
            map = new HashMap<>();
            mapMap.put(key, map);
        }
        return map;
    }

    static <K, V> Map<K, V> mapGetterNot(Map<String, Map<K, V>> mapMap, String key) {
        return mapMap.computeIfAbsent(key, k -> new HashMap<>());
    }

    public static void printout(Double[] type, String id, Map<Double, String> labelMapShort) {
        if (type != null) {
            //log.debug("Type " + labelMapShort.get(type[0]) + " id " + id);
        }
    }

    @Override
    public Object calculate(double[][] array) {
        Ta tu = new TalibMACD();
        return tu.calculate(array);
    }

    @Override
    public boolean isEnabled() {
        return conf.wantMLRSI();
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
        return PipelineConstants.MLRSI;
    }

    /*
    @Override
    protected List<SubType> wantedMergeSubTypes() {
        List<SubType> list = new ArrayList<>();
        if (conf.wantMLRSI()) {
            if (conf.isRSIEnabled()) {
                list.add(new SubTypeRSI());
            }
            if (conf.isSTOCHRSIEnabled()) {
                list.add(new SubTypeSRSI());
            }
        }
        return list;
    }
    */

}


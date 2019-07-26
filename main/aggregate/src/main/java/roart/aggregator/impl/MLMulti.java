package roart.aggregator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.pipeline.PipelineConstants;
import roart.ml.dao.MLClassifyDao;
import roart.model.StockItem;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.talib.util.TaConstants;

public class MLMulti extends IndicatorAggregator {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public MLMulti(MyMyConfig conf, String string, List<StockItem> stocks, Map<String, PeriodData> periodDataMap, 
            String title, int category, AbstractCategory[] categories, Map<String, String> idNameMap, Pipeline[] datareaders) throws Exception {
        super(conf, string, category, title, idNameMap, categories, datareaders);
/*
        if (isEnabled()) {
            calculateMe(conf, periodDataMap, category, categories, datareaders);    
            cleanMLDaos();
        }
        */
    }

    @Override
    public boolean isEnabled() {
        return conf.wantAggregatorsMlmultiML();
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getAggregatorsMLMlmultiMLConfig();
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
        if (conf.wantAggregatorsMlmultiML()) {
            if (conf.isMACDEnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORMACDLIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORMACDOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, 0, shortpos), new Filter(false, 0, shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE, conf, filter, "macd"));
                }
            }
            if (conf.isRSIEnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORRSILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORRSIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORRSI).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, conf.getMLRSIBuyRSILimit(), shortpos), new Filter(false, conf.getMLRSISellRSILimit(), shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "rsi"));
                }
            }
            if (conf.isATREnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORATRLIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORATROBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORATR).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, 0, shortpos), new Filter(false, 0, shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "atr"));
                }
            }
            if (conf.isCCIEnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORCCILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORCCIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORCCI).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, 0, shortpos), new Filter(false, 0, shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "cci"));
                }
            }
            if (conf.isSTOCHEnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHLIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORSTOCH).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, 0, shortpos), new Filter(false, 0, shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.TWORANGE, conf, filter, "stoch"));
                }
            }
            if (conf.isSTOCHRSIEnabled()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHRSILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHRSIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORSTOCHRSI).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, conf.getMLRSIBuySRSILimit(), shortpos), new Filter(false, conf.getMLRSISellSRSILimit(), shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "stochrsi"));
                }
            }
        }
        return wantedSubTypesList;
    }

    @Override
    public String getName() {
        return PipelineConstants.MLMULTI;
    }

    private class SubTypeMulti extends MergeSubType {
        private MyMyConfig conf;
        private String name;
        public SubTypeMulti(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range, MyMyConfig conf, Filter[] filter, String name) {
            super(afterbefore);
            this.listMap = (Map<String, Double[][]>) list;
            this.taMap = (Map<String, Object[]>) taObject;
            this.resultMap = (Map<String, Double[]>) resultObject;
            this.afterbefore = afterbefore;
            this.range = range;
            this.filters = filter;
            this.conf = conf;
            this.name = name;
        }
        @Override
        public String getType() {
            return name;
        }
        @Override
        public String getName() {
            return "MUL" + name;
        }
        @Override
        public int getArrIdx() {
            return 0;
        }
    }

    @Override
    protected AfterBeforeLimit getAfterBefore() {
        return new AfterBeforeLimit(conf.getMLMultiDaysBeforeLimit(), conf.getMLMultiDaysAfterLimit());
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

    /*
    @Override
    protected List<SubType> wantedMergeSubTypes() {
        List<SubType> list = new ArrayList<>();
        if (conf.isMACDEnabled()) {
            if (conf.wantMLHist()) {
                list.add(new MacdSubTypeHist(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
            }
            if (conf.wantMLMacd()) {
                list.add(new MacdSubTypeMacd(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
            }
            if (conf.wantMLSignal()) {
                list.add(new MacdSubTypeSignal(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE));
            }
            list.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE, conf, filter, "macd", MySubType.MACD));
        }
        if (conf.isRSIEnabled()) {
            list.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "rsi", MySubType.RSI));
        }
        if (conf.isATREnabled()) {
            list.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "atr", MySubType.ATR));
        }
        if (conf.isCCIEnabled()) {
            list.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "cci", MySubType.CCI));
        }
        if (conf.isSTOCHEnabled()) {
            list.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.TWORANGE, conf, filter, "stoch", MySubType.STOCH));
        }
        if (conf.isSTOCHRSIEnabled()) {
            list.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, "stochrsi", MySubType.STOCHRSI));
        }
        return list;
    }
    */
    
}

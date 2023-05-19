package roart.aggregator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.category.AbstractCategory;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.ml.dao.MLClassifyDao;
import roart.pipeline.Pipeline;
import roart.talib.util.TaConstants;

public class MLMulti extends IndicatorAggregator {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    public MLMulti(IclijConfig conf, String string, String title, int category, 
            AbstractCategory[] categories, Map<String, String> idNameMap, Pipeline[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        super(conf, string, category, title, idNameMap, categories, datareaders, neuralnetcommand);
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
            if (conf.wantAggregatorsMlmultiMACD()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORMACDLIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORMACDOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORMACD).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, 0, shortneg), new Filter(false, 0, shortpos) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.THREERANGE, conf, filter, Constants.MACD, MySubType.MACDHIST));
                }
            }
            if (conf.wantAggregatorsMlmultiRSI()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORRSILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORRSIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORRSI).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, conf.getMLRSIBuyRSILimit(), shortpos), new Filter(false, conf.getMLRSISellRSILimit(), shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, Constants.RSI, MySubType.RSI));
                }
            }
            if (conf.wantAggregatorsMlmultiATR()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORATRLIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORATROBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORATR).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, conf.getMLATRBuyLimit(), shortpos), new Filter(false, conf.getMLATRSellLimit(), shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, Constants.ATR, MySubType.ATR));
                }
            }
            if (conf.wantAggregatorsMlmultiCCI()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORCCILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORCCIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORCCI).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, conf.getMLCCIBuyLimit(), shortpos), new Filter(false, conf.getMLCCISellLimit(), shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, Constants.CCI, MySubType.CCI));
                }
            }
            if (conf.wantAggregatorsMlmultiSTOCH()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHLIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORSTOCH).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, conf.getMLSTOCHBuyLimit(), shortpos), new Filter(false, conf.getMLSTOCHSellLimit(), shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.TWORANGE, conf, filter, Constants.STOCH, MySubType.STOCH));
                }
            }
            if (conf.wantAggregatorsMlmultiSTOCHRSI()) {
                Object list = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHRSILIST);
                Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORSTOCHRSIOBJECT);
                Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORSTOCHRSI).get(PipelineConstants.RESULT);
                Filter[] filter = new Filter[] { new Filter(true, conf.getMLRSIBuySRSILimit(), shortpos), new Filter(false, conf.getMLRSISellSRSILimit(), shortneg) };
                if (taObject != null) {
                    wantedSubTypesList.add(new SubTypeMulti(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf, filter, Constants.STOCHRSI, MySubType.STOCHRSI));
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
        private IclijConfig conf;
        private String name;
        public SubTypeMulti(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range, IclijConfig conf, Filter[] filter, String name, MySubType mySubType) {
            super(afterbefore);
            this.listMap = (Map<String, Double[][]>) list;
            this.taMap = (Map<String, Object[]>) taObject;
            this.resultMap = (Map<String, Double[]>) resultObject;
            this.afterbefore = afterbefore;
            this.range = range;
            this.filters = filter;
            this.conf = conf;
            this.name = name;
            this.mySubType = mySubType;
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

    @Override
    public String getFilenamePart() {
        String ret = "MULTI" + "_";
        if (conf.wantAggregatorsMlmultiMACD()) {
            ret = ret + Constants.MACD + "_";
        }
        if (conf.wantAggregatorsMlmultiRSI()) {
            ret = ret + Constants.RSI + "_";
        }
        if (conf.wantAggregatorsMlmultiATR()) {
            ret = ret + Constants.ATR + "_";
        }
        if (conf.wantAggregatorsMlmultiCCI()) {
            ret = ret + Constants.CCI + "_";
        }
        if (conf.wantAggregatorsMlmultiSTOCH()) {
            ret = ret + Constants.STOCH + "_" ;
        }
        if (conf.wantAggregatorsMlmultiSTOCHRSI()) {
            ret = ret + Constants.STOCHRSI + "_" ;
        }
        return ret;
    }

    @Override
    protected String getAggregatorsThreshold() {
        return conf.getMLMULTIThreshold();
    }
    
}

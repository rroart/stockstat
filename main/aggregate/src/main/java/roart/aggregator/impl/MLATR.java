package roart.aggregator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.aggregator.impl.IndicatorAggregator.Filter;
import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.ml.dao.MLClassifyDao;
import roart.pipeline.Pipeline;
import roart.talib.util.TaConstants;

public class MLATR extends IndicatorAggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> getResultMap() {
        return new HashMap<>();
    }

    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }

    public MLATR(MyMyConfig conf, String string, String title, int category, 
            AbstractCategory[] categories, Map<String, String> idNameMap, Pipeline[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        super(conf, string, category, title, idNameMap, categories, datareaders, neuralnetcommand);
    }

    private abstract class ATRSubType extends SubType {
        public ATRSubType(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            this.listMap = (Map<String, Double[][]>) list;
            this.taMap = (Map<String, Object[]>) taObject;
            this.resultMap = (Map<String, Double[]>) resultObject;
            this.afterbefore = afterbefore;
            this.range = range;
            this.filters = new Filter[] { new Filter(true, conf.getMLATRBuyLimit(), shortpos), new Filter(false, conf.getMLATRSellLimit(), shortneg) };
        }
    }

    private class ATRSubTypeATR extends ATRSubType {
        public ATRSubTypeATR(Object list, Object taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
        }
        @Override
        public String getType() {
            return "A";
        }
        @Override
        public String getName() {
            return "ATR";
        }
        @Override
        public int getArrIdx() {
            return TaConstants.ONEIDXARRONE;
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
        Object list = cat.getResultMap().get(PipelineConstants.INDICATORATRLIST);
        Object taObject = cat.getResultMap().get(PipelineConstants.INDICATORATROBJECT);
        Object resultObject = cat.getIndicatorLocalResultMap().get(PipelineConstants.INDICATORATR).get(PipelineConstants.RESULT);
        wantedSubTypesList.add(new ATRSubTypeATR(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE));
        return wantedSubTypesList;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getMLATRMLConfig();
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
        return conf.wantMLATR();
    }

    @Override
    protected AfterBeforeLimit getAfterBefore() {
        return new AfterBeforeLimit(conf.getMLATRDaysBeforeLimit(), conf.getMLATRDaysAfterLimit());
    }
    
    @Override
    protected int fieldSize() {
        int size = 0;
        List<SubType> subTypes = wantedSubTypes();
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
        return PipelineConstants.MLATR;
    }

    @Override
    protected boolean anythingHere(Map<String, Double[][]> listMap) {
        return anythingHere3(listMap);
    }

    @Override
    public String getFilenamePart() {
        return Constants.ATR + "_" + getAfterBefore().getFilePart();
    }
    
    @Override
    protected String getAggregatorsThreshold() {
        return conf.getMLATRThreshold();
    }
    
}


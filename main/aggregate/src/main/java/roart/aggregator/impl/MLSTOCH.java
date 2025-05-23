package roart.aggregator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.inmemory.model.Inmemory;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMapD;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.pipeline.util.PipelineUtils;
import roart.ml.dao.MLClassifyDao;
import roart.talib.util.TaConstants;

public class MLSTOCH extends IndicatorAggregator {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public Map<String, Object> getResultMap() {
        return new HashMap<>();
    }

    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }

    public MLSTOCH(IclijConfig conf, String string, String title, int category, 
            Map<String, String> idNameMap, PipelineData[] datareaders, NeuralNetCommand neuralnetcommand, List<String> stockDates, Inmemory inmemory) throws Exception {
        super(conf, string, category, title, idNameMap, datareaders, neuralnetcommand, stockDates, inmemory);
    }

    private abstract class STOCHSubType extends SubType {
        public STOCHSubType(Object list, SerialMapTA taObject, SerialMapD smap, AfterBeforeLimit afterbefore, int[] range) {
            this.listMap = list != null ? (Map<String, Double[][]>) list : new HashMap<>();
            this.taMap = taObject;
            this.resultMap = smap.getMap() != null ? smap.getMap() : new HashMap<>();
            this.afterbefore = afterbefore;
            this.range = range;
            this.filters = new Filter[] { new Filter(true, conf.getMLSTOCHBuyLimit(), shortpos), new Filter(false, conf.getMLSTOCHSellLimit(), shortneg) };
        }
    }

    private class STOCHSubTypeSTOCH extends STOCHSubType {
        public STOCHSubTypeSTOCH(Object list, SerialMapTA taObject, SerialMapD resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
        }
        @Override
        public String getType() {
            return "S";
        }
        @Override
        public String getName() {
            return "Stoch";
        }
        @Override
        public int getArrIdx() {
            return TaConstants.TWOIDXARRONE;
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
    protected List<SubType> getWantedSubTypes(AfterBeforeLimit afterbefore) {
        List<SubType> wantedSubTypesList = new ArrayList<>();
        PipelineData pipelineData = PipelineUtils.getPipeline(datareaders, PipelineConstants.INDICATORSTOCH, inmemory);
        Object list = null;
        SerialMapTA taObject = PipelineUtils.getMapTA(pipelineData);
        SerialMapD resultObject = PipelineUtils.getResultMap(pipelineData);
        wantedSubTypesList.add(new STOCHSubTypeSTOCH(list, taObject, resultObject, afterbefore, TaConstants.TWORANGE));
        return wantedSubTypesList;
    }

    @Override
    protected String getNeuralNetConfig() {
        return conf.getMLSTOCHMLConfig();
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
        return conf.wantMLSTOCH();
    }

    @Override
    protected AfterBeforeLimit getAfterBefore() {
        return new AfterBeforeLimit(conf.getMLSTOCHDaysBeforeLimit(), conf.getMLSTOCHDaysAfterLimit());
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
        return PipelineConstants.MLSTOCH;
    }

    @Override
    protected boolean anythingHere(Map<String, List<List<Double>>> listMap) {
        return anythingHere3(listMap);
    }

    @Override
    public String getFilenamePart() {
        return Constants.STOCH + "_" + getAfterBefore().getFilePart();
    }
    
    @Override
    protected String getAggregatorsThreshold() {
        return conf.getMLSTOCHThreshold();
    }
    
}


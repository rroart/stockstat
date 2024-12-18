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
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMapD;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.util.ArraysUtil;
import roart.common.util.PipelineUtils;
import roart.db.dao.DbDao;
import roart.executor.MyExecutors;
import roart.indicator.util.IndicatorUtils;
import roart.common.constants.Constants;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.common.MLClassifyModel;
import roart.result.model.ResultItemTable;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
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

    public MLMACD(IclijConfig conf, String string, String title, int category, 
            Map<String, String> idNameMap, PipelineData[] datareaders, NeuralNetCommand neuralnetcommand, List<String> stockDates) throws Exception {
        super(conf, string, category, title, idNameMap, datareaders, neuralnetcommand, stockDates);
    }

    private abstract class MacdSubType extends MergeSubType {
        public MacdSubType(Object list, SerialMapTA taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(afterbefore);
            this.listMap = list != null ? (Map<String, Double[][]>) list : new HashMap<>();
            this.taMap = taObject;
            SerialMapD smap = (SerialMapD) resultObject;
            this.resultMap = smap.getMap() != null ? smap.getMap() : new HashMap<>();
            this.afterbefore = afterbefore;
            this.range = range;
            this.filters = new Filter[] { new Filter(true, 0, shortneg), new Filter(false, 0, shortpos) };
        }

        public MacdSubType() {
            super(getAfterBefore());
        }
    }

    private class MacdSubTypeHist extends MacdSubType {
        public MacdSubTypeHist(Object list, SerialMapTA taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
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
        public MacdSubTypeMacd(Object list, SerialMapTA taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
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
        public MacdSubTypeSignal(Object list, SerialMapTA taObject, Object resultObject, AfterBeforeLimit afterbefore, int[] range) {
            super(list, taObject, resultObject, afterbefore, range);
            this.mySubType = MySubType.MACDSIG;
            this.useDirectly = false;
            this.useMergeLimitTrigger = false;
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
    protected List<SubType> getWantedSubTypes(AfterBeforeLimit afterbefore) {
        List<SubType> wantedSubTypesList = new ArrayList<>();
        PipelineData pipelineData = PipelineUtils.getPipeline(datareaders, PipelineConstants.INDICATORMACD);
        Object list = null;
        SerialMapTA taObject = (SerialMapTA) pipelineData.get(PipelineConstants.OBJECT);
        Object resultObject = pipelineData.get(PipelineConstants.RESULT);
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
    protected AfterBeforeLimit getAfterBefore() {
        return new AfterBeforeLimit(conf.getMACDDaysBeforeZero(), conf.getMACDDaysAfterZero());
    }
    
    @Override
    protected int fieldSize() {
        int size = 0;
        List<SubType> subTypes = usedSubTypes();
        for (SubType subType : subTypes) {
            if (!subType.useDirectly) {
                continue;
            }
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

    @Override
    public String getFilenamePart() {
        String ret = "";
        if (conf.wantMLHist()) {
            ret = ret + Constants.HIST + "_";
        }
        if (conf.wantMLMacd()) {
            ret = ret + Constants.MACD + "_";
        }
        if (conf.wantMLSignal()) {
            ret = ret + Constants.SIGNAL + "_";
        }
        ret = ret + getAfterBefore().getFilePart();
        return ret;
    }

    @Override
    protected String getAggregatorsThreshold() {
        return conf.getMLMACDThreshold();
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


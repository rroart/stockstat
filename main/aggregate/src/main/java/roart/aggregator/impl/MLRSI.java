package roart.aggregator.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.pipeline.data.SerialMapD;
import roart.common.pipeline.data.SerialMapTA;
import roart.common.pipeline.data.SerialTA;
import roart.common.pipeline.util.PipelineUtils;
import roart.ml.dao.MLClassifyDao;
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

    public MLRSI(IclijConfig conf, String string, String title, int category, 
            Map<String, String> idNameMap, PipelineData[] datareaders, NeuralNetCommand neuralnetcommand, List<String> stockDates) throws Exception {
        super(conf, string, category, title, idNameMap, datareaders, neuralnetcommand, stockDates);
    }

    private abstract class RsiSubType extends MergeSubType {
        public RsiSubType(Object list, SerialMapTA taObject, SerialMapD smap, AfterBeforeLimit afterbefore, int[] range) {
            super(afterbefore);
            this.listMap = list != null ? (Map<String, Double[][]>) list : new HashMap<>();
            this.taMap = taObject;
            this.resultMap = smap.getMap() != null ? smap.getMap() : new HashMap<>();
            this.afterbefore = afterbefore;
            this.range = range;
        }
        
        public RsiSubType() {
            super(getAfterBefore());
        }
    }

    private class SubTypeRSI extends RsiSubType {
        public SubTypeRSI(Object list, SerialMapTA taObject, SerialMapD resultObject, AfterBeforeLimit afterbefore, int[] range, IclijConfig conf) {
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
        public SubTypeSRSI(Object list, SerialMapTA taObject, SerialMapD resultObject, AfterBeforeLimit afterbefore, int[] range, IclijConfig conf) {
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

        public MergeSubTypeRSI(Object list, SerialMapTA taObject, SerialMapD resultObject, AfterBeforeLimit afterbefore,
                int[] range, IclijConfig conf) {
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
    protected List<SubType> getWantedSubTypes(AfterBeforeLimit afterbefore) {
        List<SubType> wantedSubTypesList = new ArrayList<>();
        if (conf.wantMLRSI()) {
            if (conf.isRSIEnabled()) {
                PipelineData pipelineData = PipelineUtils.getPipeline(datareaders, PipelineConstants.INDICATORRSI);
                Object list = null;
                SerialMapTA taObject = PipelineUtils.getMapTA(pipelineData);
                SerialMapD resultObject = PipelineUtils.getResultMap(pipelineData);
                wantedSubTypesList.add(new SubTypeRSI(list, taObject, resultObject, afterbefore, TaConstants.ONERANGE, conf));
            }
            if (conf.isSTOCHRSIEnabled()) {
                PipelineData pipelineData = PipelineUtils.getPipeline(datareaders, PipelineConstants.INDICATORSTOCHRSI);
                Object list = null;
                SerialMapTA taObject = PipelineUtils.getMapTA(pipelineData);
                SerialMapD resultObject = PipelineUtils.getResultMap(pipelineData);
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
    
    public static void printout(Double[] type, String id, Map<Double, String> labelMapShort) {
        if (type != null) {
            //log.debug("Type " + labelMapShort.get(type[0]) + " id " + id);
        }
    }

    @Override
    public boolean isEnabled() {
        return conf.wantMLRSI();
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

    @Override
    public String getFilenamePart() {
        String ret = "";
        if (conf.isRSIEnabled()) {
            ret = ret + Constants.RSI + "_";
        }
        if (conf.isSTOCHRSIEnabled()) {
            ret = ret + Constants.STOCHRSI + "_";
        }
        ret = ret + getAfterBefore().getFilePart();
        return ret;
    }

    @Override
    protected String getAggregatorsThreshold() {
        return conf.getMLRSIThreshold();
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


package roart.predictor.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.util.Pair;

import roart.category.AbstractCategory;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.ml.dao.MLPredictDao;
import roart.ml.model.LearnTestPredictResult;
import roart.ml.model.MLPredictModel;
import roart.model.StockItem;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockDao;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.common.predictor.AbstractPredictor;

public class PredictorLSTM extends AbstractPredictor {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    String key;
    Map<String, Double[][]> listMap;
    Map<String, double[][]> truncListMap;
    Object[] emptyField = new Object[1];
    Map<MLPredictModel, Long> mapTime = new HashMap<>();

    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;

    private int fieldSize = 0;

    @Override
    public Map<Integer, List<ResultItemTableRow>> otherTables() {
        Map<Integer, List<ResultItemTableRow>> retMap = new HashMap<>();
        if (mlTimesTableRows != null) {
            retMap.put(Constants.MLTIMES, mlTimesTableRows);
        }
        if (eventTableRows != null) {
            retMap.put(Constants.EVENT, eventTableRows);
        }
        return retMap;
    }

    List<MLPredictDao> mldaos = new ArrayList<>();

    public PredictorLSTM(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title, int category) throws Exception {
        super(conf, string, category);
        if (!isEnabled()) {
            return;
        }
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        makeWantedSubTypes();
        makeMapTypes();
        if (conf.wantML()) {
            if (false && conf.wantMLSpark()) {
                mldaos.add(new MLPredictDao("spark", conf));
            }
            if (conf.wantMLTensorflow()) {
                mldaos.add(new MLPredictDao("tensorflow", conf));
            }
        }
        fieldSize = fieldSize();
        if (conf.wantMLTimes()) {
            mlTimesTableRows = new ArrayList<>();
            Object[] objs = new Object[fieldSize];
            int retindex = 0;
            objs[retindex++] = "";
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        calculate(conf, marketdatamap, periodDataMap, category);        
    }

    private interface PredSubType {
        public abstract  String getType();
        public abstract  String getName();
    }

    private class PredSubTypeFull implements PredSubType {
        @Override
        public String getType() {
            return "F";
        }
        @Override
        public String getName() {
            return "Full";
        }
    }

    private class PredSubTypeSingle implements PredSubType {
        @Override
        public String getType() {
            return "S";
        }
        @Override
        public String getName() {
            return "Single";
        }
    }

    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }

    private List<Integer> getMapTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(0);
        return retList;
    }

    @Override
    public List<Integer> getTypeList() {
        return getMapTypeList();
    }

    private Map<Integer, String> mapTypes = new HashMap<>();

    private void makeMapTypes() {
        mapTypes.put(0, "me");
    }

    private List<PredSubType> wantedSubTypes = new ArrayList<>();

    private List<PredSubType> wantedSubTypes() {
        return wantedSubTypes;
    }

    private void makeWantedSubTypes() {
        wantedSubTypes.add(new PredSubTypeSingle());
    }
    
    // TODO make an oo version of this
    private void calculate(MyMyConfig conf, Map<String, MarketData> marketdatamap,
            Map<String, PeriodData> periodDataMap, int category) throws Exception {
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        Map<String, Double[][]> retArray = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        this.listMap = retArray;
        this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        if (conf.wantPercentizedPriceIndex()) {

        }
        if (!anythingHere(listMap)) {
            log.info("empty {}", key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        probabilityMap = new HashMap<>();

        long time2 = System.currentTimeMillis();
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();
        log.info("listmap {} {}", listMap.size(), listMap.keySet());
        for (String id : listMap.keySet()) {
            double[][] list0 = truncListMap.get(id);
            double[] list = list0[0];
            log.info("list {} {}", list.length, Arrays.asList(list));
        }
        Map<String, Double[]> mapResult = new HashMap<>();
        if (conf.wantML()) {
            doPredictions(conf, mapResult);
        }
        createResultMap(conf, mapResult);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        handleSpentTime(conf);

    }

    private void doPredictions(MyMyConfig conf, Map<String, Double[]> mapResult) {
        try {
            List<PredSubType> subTypes = wantedSubTypes();
            for (PredSubType subType : subTypes) {
                for (MLPredictDao mldao : mldaos) {
                    for (MLPredictModel model : mldao.getModels()) {
                        LearnTestPredictResult result = getMapResultList(conf, mldao, model);
                        Map<String, Double[]> localMapResult = result.predictMap;
                        mapResult.putAll(localMapResult);
                        Map<String, Double> accuracyMap = result.accuracyMap;
                        probabilityMap.putAll(accuracyMap);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private Map<String, Double[]> getMapResult(MyMyConfig conf, MLPredictDao mldao, int horizon, int windowsize,
            int epochs, MLPredictModel model) {
        Map<String, Double[]> localMapResult = new HashMap<>();
        for (String id : listMap.keySet()) {
            double[][] list0 = truncListMap.get(id);
            double[] list = list0[0];
            // TODO check reverse. move up before if?
            log.info("list {} {}", list.length, windowsize);
            if (list != null && list.length > 2 * windowsize ) {
                Double[] list3 = ArrayUtils.toObject(list);
                LearnTestPredictResult result = mldao.predictone(new NeuralNetConfigs(), this, list3, model, conf.getMACDDaysBeforeZero(), key, 4, mapTime);  
                localMapResult.put(id, result.predicted);
                probabilityMap.put(id, result.accuracy);
            }
        }
        return localMapResult;
    }

    private LearnTestPredictResult getMapResultList(MyMyConfig conf, MLPredictDao mldao, MLPredictModel model) {
        Map<String, Double[]> map = new HashMap<>();
        for (String id : listMap.keySet()) {
            double[][] list0 = truncListMap.get(id);
            double[] list = list0[0];
            // TODO check reverse. move up before if?
            if (list != null) {
                log.info("list {}", list.length);
                Double[] list3 = ArrayUtils.toObject(list);
                map.put(id, list3);
            }
        }
        return mldao.predict(this, new NeuralNetConfigs(), map, model, conf.getMACDDaysBeforeZero(), key, 4, mapTime);  
    }

    private void createResultMap(MyMyConfig conf, Map<String, Double[]> mapResult) {
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[1];
            resultMap.put(id, fields);
            int retindex = 0;
            if (conf.wantML()) {
                List<PredSubType> subTypes2 = wantedSubTypes();
                for (PredSubType subType : subTypes2) {
                    for (MLPredictDao mldao : mldaos) {
                        for (MLPredictModel model : mldao.getModels()) {
                            retindex = mldao.addResults(fields, retindex, id, model, this, mapResult, null);
                        }   
                    }
                }
            }
        }
    }

    private void handleSpentTime(MyMyConfig conf) {
        if (conf.wantMLTimes()) {
            for (Entry<MLPredictModel, Long> entry : mapTime.entrySet()) {
                MLPredictModel model = entry.getKey();
                ResultItemTableRow row = new ResultItemTableRow();
                row.add(key);
                row.add(model.getEngineName());
                row.add(model.getName());
                row.add(entry.getValue());
                mlTimesTableRows.add(row);
            }
        }
    }

    private boolean anythingHere(Map<String, Double[][]> listMap2) {
        for (Double[][] array : listMap2.values()) {
            for (int i = 0; i < array.length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

  public void addEventRow(String text, String name, String id) {
        ResultItemTableRow event = new ResultItemTableRow();
        event.add(key);
        event.add(text);
        event.add(name);
        event.add(id);
        eventTableRows.add(event);
    }

    @Override
    public Object calculate(Double[] array) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return conf.wantPredictorLSTM();
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        String market = conf.getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new Pair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        if (perioddata == null) {
            log.info("key {} {}", key, periodDataMap.keySet());
        }
        Object[] result = null;
        if (resultMap != null) {
            result = resultMap.get(id);
        }
        if (result == null) {
            result = emptyField;
        }
        return result;
    }

    @Override
    public Object[] getResultItemTitle() {
        Object[] objs = new Object[fieldSize];
        int retindex = 0;
        // TODO make OO of this
        List<PredSubType> subTypes = wantedSubTypes();
        for (PredSubType subType : subTypes) {
            for (MLPredictDao mldao : mldaos) {
                retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
            }
        }
        log.info("fieldsizet {}", retindex);
        return objs;
    }

    private int fieldSize() {
        emptyField = new Object[1];
        if (true) return 1;
        int size = 0;
        List<PredSubType> subTypes = wantedSubTypes();
        for (PredSubType subType : subTypes) {
            for (MLPredictDao mldao : mldaos) {
                size += mldao.getSizes(this);
            }
        }
        emptyField = new Object[size];
        log.info("fieldsizet {}", size);
        return size;
    }

    public String predictorName() {
        return PipelineConstants.LSTM;
    }
}


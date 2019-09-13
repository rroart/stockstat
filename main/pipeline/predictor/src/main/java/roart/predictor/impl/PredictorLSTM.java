package roart.predictor.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalDouble;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.category.AbstractCategory;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.dao.MLPredictDao;
import roart.ml.model.LearnTestPredictResult;
import roart.ml.model.MLPredictModel;
import roart.model.StockItem;
import roart.result.model.ResultItemTableRow;
import roart.stockutil.StockDao;
import roart.stockutil.StockUtil;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;

public class PredictorLSTM extends AbstractPredictor {

    Map<String, MarketData> marketdatamap;
    Map<String, PeriodData> periodDataMap;
    String key;
    protected Map<String, Double[][]> listMap;
    protected Map<String, Double[][]> fillListMap;

    protected Map<String, double[][]> truncListMap;
    protected Map<String, double[][]> truncFillListMap;
    
    protected Map<String, Double[][]> base100ListMap;
    protected Map<String, Double[][]> base100FillListMap;
    
    protected Map<String, double[][]> truncBase100ListMap;
    protected Map<String, double[][]> truncBase100FillListMap;
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
    
    private AbstractCategory[] categories;
    private Pipeline[] datareaders;

    public PredictorLSTM(MyMyConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, String title, int category, AbstractCategory[] categories, Pipeline[] datareaders) throws Exception {
        super(conf, string, category);
        if (!isEnabled()) {
            return;
        }
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        this.categories = categories;
        this.datareaders = datareaders;
        makeWantedSubTypes();
        makeMapTypes();
        if (conf.wantML()) {
            if (false && conf.wantMLSpark()) {
                mldaos.add(new MLPredictDao(MLConstants.SPARK, conf));
            }
            if (conf.wantMLTensorflow()) {
                mldaos.add(new MLPredictDao(MLConstants.TENSORFLOW, conf));
                if (mldaos.get(0).getModels().isEmpty()) {
                    int jj = 0;
                }
            }
        }
        if (mldaos.isEmpty()) {
            int jj = 0;
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
        //calculate(); //conf, marketdatamap, periodDataMap, category, categories);        
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
    
    // make an oo version of this
    @Override
    public void calculate() throws Exception { // MyMyConfig conf, Map<String, MarketData> marketdatamap,
           // Map<String, PeriodData> periodDataMap, int category2, AbstractCategory[] categories) throws Exception {
        if (!isEnabled()) {
            return;
        }
        SimpleDateFormat dt = new SimpleDateFormat(Constants.MYDATEFORMAT);
        String dateme = dt.format(conf.getdate());
        
        AbstractCategory cat = IndicatorUtils.getWantedCategory(categories, category);
        if (cat == null) {
            return;
        }
        log.info("checkthis {}", category == cat.getPeriod());
        log.info("checkthis {}", title.equals(cat.getTitle()));
        log.info("checkthis {}", key.equals(title));
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        this.listMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST);
        this.fillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.FILLLIST);
        this.truncListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCLIST);       
        this.truncFillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCFILLLIST);       
        this.base100ListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.BASE100LIST);
        this.base100FillListMap = (Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.BASE100FILLLIST);
        this.truncBase100ListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100LIST);       
        this.truncBase100FillListMap = (Map<String, double[][]>) datareader.getLocalResultMap().get(PipelineConstants.TRUNCBASE100FILLLIST);       

        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        //Map<String, Double[][]> retArray = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        //this.listMap = retArray;
        //this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        if (!anythingHere(listMap)) {
            log.info("empty {}", key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        probabilityMap = new HashMap<>();

        NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
        String nnconfigString = conf.getTensorflowPredictorLSTMConfig();
        if (nnconfigString != null) {
            ObjectMapper mapper = new ObjectMapper();
            TensorflowPredictorLSTMConfig lstmConfig = mapper.readValue(nnconfigString, TensorflowPredictorLSTMConfig.class);
            nnConfigs.getTensorflowConfig().setTensorflowPredictorLSTMConfig(lstmConfig);
        }

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
            if (false && conf.wantPercentizedPriceIndex()) {
                doPredictions(conf, mapResult, base100FillListMap, truncBase100FillListMap, nnConfigs);
            } else {
                doPredictions(conf, mapResult, fillListMap, truncFillListMap, nnConfigs);                
            }
        }
        createResultMap(conf, mapResult, false && conf.wantPercentizedPriceIndex() ? base100FillListMap : fillListMap);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        handleSpentTime(conf);

    }

    private void doPredictions(MyMyConfig conf, Map<String, Double[]> mapResult, Map<String, Double[][]> aListMap, Map<String, double[][]> aTruncListMap, NeuralNetConfigs nnConfigs) {
        try {
            List<PredSubType> subTypes = wantedSubTypes();
            for (PredSubType subType : subTypes) {
                for (MLPredictDao mldao : mldaos) {
                    for (MLPredictModel model : mldao.getModels()) {
                        LearnTestPredictResult result = getMapResultList(conf, mldao, model, aListMap, aTruncListMap, nnConfigs);
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
            int epochs, MLPredictModel model, Map<String, Double[][]> aListMap, Map<String, double[][]> aTruncListMap) {
        Map<String, Double[]> localMapResult = new HashMap<>();
        for (String id : aListMap.keySet()) {
            double[][] list0 = aTruncListMap.get(id);
            double[] list = list0[0];
            // check reverse. move up before if?
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

    private LearnTestPredictResult getMapResultList(MyMyConfig conf, MLPredictDao mldao, MLPredictModel model, Map<String, Double[][]> aListMap, Map<String, double[][]> aTruncListMap, NeuralNetConfigs nnConfigs) {
        Map<String, Double[]> map = new HashMap<>();
        for (String id : aListMap.keySet()) {
            Double[] listl = aListMap.get(id)[0];
            double[][] list0 = aTruncListMap.get(id);
            double[] list = list0[0];
            // check reverse. move up before if?
            if (list != null) {
                log.info("list {}", list.length);
                Double[] list3 = ArrayUtils.toObject(list);
                map.put(id, list3);
            }
        }
        return mldao.predict(this, nnConfigs, map, model, conf.getMACDDaysBeforeZero(), key, 4, mapTime);  
    }

    private void createResultMap(MyMyConfig conf, Map<String, Double[]> mapResult, Map<String, Double[][]> aListMap) {
        for (String id : aListMap.keySet()) {
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

    protected boolean anythingHere(Map<String, Double[][]> myListMap) {
        for (Double[][] array : myListMap.values()) {
            for (int i = 0; i < array[0].length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean anythingHereNot(Map<String, Double[][]> listMap2) {
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
        Pair<String, String> pair = new ImmutablePair<>(market, id);
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
        OptionalDouble average = probabilityMap
                .values()
                .stream()
                .mapToDouble(a -> (Double) a)
                .average();
        // make OO of this
        String val = "";
        // workaround
        try {
            val = "" + MLClassifyModel.roundmebig(average.getAsDouble());
            //val = "" + MLClassifyModel.roundme(mldao.eval(model . getId(), key, subType + mapType));
        } catch (Exception e) {
            log.error("Exception fix later, refactor", e);
        }
        List<PredSubType> subTypes = wantedSubTypes();
        for (PredSubType subType : subTypes) {
            for (MLPredictDao mldao : mldaos) {
                objs[retindex++] = title + Constants.WEBBR + val;
                //objs[retindex++] = predictorName() + Constants.WEBBR + "value";
                //retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
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
    
    @Override
    public boolean hasValue() {
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        return anythingHere((Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST));
    }

}


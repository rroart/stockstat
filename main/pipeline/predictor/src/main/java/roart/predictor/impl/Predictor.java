package roart.predictor.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.Map.Entry;
import java.util.Objects;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.fasterxml.jackson.databind.ObjectMapper;

import roart.category.AbstractCategory;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.NeuralNetTensorflowConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.MathUtil;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.dao.MLClassifyDao;
import roart.ml.model.LearnTestClassifyResult;
import roart.model.StockItem;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;

public abstract class Predictor extends AbstractPredictor {

    public Predictor(MyMyConfig conf, String string, int category, NeuralNetCommand neuralnetcommand, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, AbstractCategory[] categories, Pipeline[] datareaders) {
        super(conf, string, category, neuralnetcommand);
        if (!isEnabled()) {
            return;
        }
        this.marketdatamap = marketdatamap;
        this.periodDataMap = periodDataMap;
        this.key = title;
        this.categories = categories;
        this.datareaders = datareaders;
        makeMapTypes();
        if (conf.wantML()) {
            if (getType().equals(MLConstants.TENSORFLOW) && conf.wantPredictorsTensorflow()) {
                mldaos.add(new MLClassifyDao(getType(), conf));
            }
            if (getType().equals(MLConstants.PYTORCH) && conf.wantPredictorsPytorch()) {
                mldaos.add(new MLClassifyDao(getType(), conf));
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
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();

    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;

    private int fieldSize = 0;

    public abstract String getType();

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

    List<MLClassifyDao> mldaos = new ArrayList<>();

    private AbstractCategory[] categories;
    private Pipeline[] datareaders;

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
        accuracyMap = new HashMap<>();
        lossMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();

        List<Date> dateList = (List<Date>) pipelineMap.get("" + this.category).getLocalResultMap().get(PipelineConstants.DATELIST);
        Integer days = conf.getDays();
        if (days == 0) {
            days = dateList.size();
        }
        
        NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
        String nnconfigString = getMyNeuralNetConfig();
        if (nnconfigString != null) {
            ObjectMapper mapper = new ObjectMapper();
            nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);
            //TensorflowPredictorLSTMConfig lstmConfig = mapper.readValue(nnconfigString, TensorflowPredictorLSTMConfig.class);
            //nnConfigs.setTensorflowConfig(new NeuralNetTensorflowConfig(null, null, null, null, null, null, null, null, null));
            //nnConfigs.getTensorflowConfig().setTensorflowPredictorLSTMConfig(lstmConfig);
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
        Map<MLClassifyModel, Map<String, Double[]>> mapResult = new HashMap<>();
        if (conf.wantML()) {
            if (false && conf.wantPercentizedPriceIndex()) {
                doPredictions(conf, mapResult, base100FillListMap, truncBase100FillListMap, nnConfigs, days);
            } else {
                doPredictions(conf, mapResult, fillListMap, truncFillListMap, nnConfigs, days);                
            }
        }
        createResultMap(conf, mapResult, false && conf.wantPercentizedPriceIndex() ? base100FillListMap : fillListMap);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        handleSpentTime(conf);

    }

    protected String getMyNeuralNetConfig() {
        return conf.getPredictorsMLConfig();
    }

    protected abstract String getNeuralNetConfig();

    private void doPredictions(MyMyConfig conf, Map<MLClassifyModel, Map<String, Double[]>> mapResult, Map<String, Double[][]> aListMap, Map<String, double[][]> aTruncListMap, NeuralNetConfigs nnConfigs, int days) {
        try {
            for (MLClassifyDao mldao : mldaos) {
                if (mldao.getModels().size() != 1) {
                    log.error("Models size is {}", mldao.getModels().size());
                }
                for (MLClassifyModel model : mldao.getModels(predictorName())) {
                    List<Triple<String, Object, Double>> map = new ArrayList<>();
                    // days find max
                    days = 0;
                    for (String id : aListMap.keySet()) {
                        Double[] listl = aListMap.get(id)[0];
                        double[][] list0 = aTruncListMap.get(id);
                        double[] list = list0[0];
                        // check reverse. move up before if?
                        if (list != null && list.length > days) {
                            days = list.length;
                        }
                    }
                    log.info("list days {}", days);
                    for (String id : aListMap.keySet()) {
                        Double[] listl = aListMap.get(id)[0];
                        double[][] list0 = aTruncListMap.get(id);
                        double[] list = list0[0];
                        // check reverse. move up before if?
                        if (list != null && list.length == days) {
                            log.info("list {}", list.length);
                            Object list3 = ArrayUtils.toObject(list);
                            map.add(new ImmutableTriple(id, list3, null));
                        }
                    }
                    List<Triple<String, Object, Double>> classifylist = new ArrayList<>();
                    for (String id : aListMap.keySet()) {
                        Double[] listl = aListMap.get(id)[0];
                        double[][] list0 = aTruncListMap.get(id);
                        double[] list = list0[0];
                        // check reverse. move up before if?
                        if (list != null && list.length >= conf.getPredictorsDays()) {
                            log.info("list {}", list.length);
                            Object list3 = ArrayUtils.toObject(Arrays.copyOfRange(list, list.length - conf.getPredictorsDays(), list.length));
                            classifylist.add(new ImmutableTriple(id, list3, null));
                        }
                    }
                    // make OO of this, create object
                    Object[] meta = new Object[10];
                    meta[0] = mldao.getName();
                    meta[1] = model.getName();
                    meta[2] = model.getReturnSize();
                    resultMetaArray.add(meta);
                    ResultMeta resultMeta = new ResultMeta();
                    resultMeta.setMlName(mldao.getName());
                    resultMeta.setModelName(model.getName());
                    resultMeta.setReturnSize(model.getReturnSize());
                    getResultMetas().add(resultMeta);
                    MLMeta mlmeta = new MLMeta();
                    mlmeta.dim1 = 10;
                    mlmeta.classify = true;
                    mlmeta.features = true;
                    String filename = getFilename(mldao, model, "" + conf.getPredictorsDays(), "" + conf.getPredictorsFuturedays(), conf.getMarket(), null);
                    String path = model.getPath();
                    LearnTestClassifyResult result = mldao.learntestclassify(nnConfigs, null, map, model, conf.getPredictorsDays(), conf.getPredictorsFuturedays(), mapTime, classifylist, null, path, filename, neuralnetcommand, mlmeta, false);  
                    Map<String, Double[]> classifyResult = result.getCatMap();
                    accuracyMap.put(mldao.getName() + model.getName(), result.getAccuracy());
                    lossMap.put(mldao.getName() + model.getName(), result.getLoss());
                    meta[9] = result.getLoss();
                    resultMeta.setLoss(result.getLoss());
                    mapResult.put(model, classifyResult);
                    meta[4] = result.getAccuracy();
                    resultMeta.setTestAccuracy(result.getAccuracy());
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private Map<String, Double[]> getMapResult(MyMyConfig conf, MLClassifyDao mldao, int horizon, int windowsize,
            int epochs, MLClassifyModel model, Map<String, Double[][]> aListMap, Map<String, double[][]> aTruncListMap) {
        Map<String, Double[]> localMapResult = new HashMap<>();
        for (String id : aListMap.keySet()) {
            double[][] list0 = aTruncListMap.get(id);
            double[] list = list0[0];
            // check reverse. move up before if?
            log.info("list {} {}", list.length, windowsize);
            if (list != null && list.length > 2 * windowsize ) {
                Double[] list3 = ArrayUtils.toObject(list);
                LearnTestClassifyResult result = null; //mldao.learntestclassify(nnConfigs, null, map, model, 10, 1, mapTime, null, null, path, filename, neuralnetcommand, mlmeta);  
                localMapResult = result.getCatMap();
                accuracyMap.put(id, result.getAccuracy());
                lossMap.put(id, result.getLoss());
            }
        }
        return localMapResult;
    }

    private void createResultMap(MyMyConfig conf, Map<MLClassifyModel, Map<String, Double[]>> mapResult, Map<String, Double[][]> aListMap) {
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[fieldSize];
            resultMap.put(id, fields);
            int retindex = 0 ;

            // make OO of this
            if (conf.wantML()) {
                for (MLClassifyDao mldao : mldaos) {
                    for (MLClassifyModel model : mldao.getModels(predictorName())) {
                        Map<String, Double[]> resultMap = mapResult.get(model);
                        Double[] aType = null;
                        if (resultMap != null) {
                            aType = resultMap.get(id);
                            if (aType != null) {
                                fields[retindex++] = aType[aType.length - 1];
                            }
                        }
                    }   
                }
            }
        }
    }

    private void handleSpentTime(MyMyConfig conf) {
        if (conf.wantMLTimes()) {
            for (Entry<MLClassifyModel, Long> entry : mapTime.entrySet()) {
                MLClassifyModel model = entry.getKey();
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
            result = MathUtil.round2(result, 3);
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
        // make OO of this
        String val = "";
        // workaround
        try {
            OptionalDouble average = lossMap
                    .values()
                    .stream()
                    .mapToDouble(a -> (Double) a)
                    .filter(Objects::nonNull)
                    .average();
            val = "" + MLClassifyModel.roundmebig(average.getAsDouble());
            //val = "" + MLClassifyModel.roundme(mldao.eval(model . getId(), key, subType + mapType));
        } catch (Exception e) {
            log.error("Exception fix later, refactor", e);
        }
        for (MLClassifyDao mldao : mldaos) {
            objs[retindex++] = title + Constants.WEBBR + val;
            //objs[retindex++] = predictorName() + Constants.WEBBR + "value";
            //retindex = mldao.addTitles(objs, retindex, this, title, key, subType.getType());
        }


        log.info("fieldsizet {}", retindex);
        return objs;
    }

    private int fieldSize() {
        emptyField = new Object[1];
        return 1;
    }

    @Override
    public boolean hasValue() {
        Map<String, Pipeline> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        Pipeline datareader = pipelineMap.get("" + category);
        return anythingHere((Map<String, Double[][]>) datareader.getLocalResultMap().get(PipelineConstants.LIST));
    }
    
    @Override
    public String getName() {
        return PipelineConstants.PREDICTOR;
    }

    public String getFilename(MLClassifyDao dao, MLClassifyModel model, String in, String out, String market, List indicators) {
        String testmarket = conf.getMLmarket();
        if (testmarket != null) {
            market = testmarket;
        }
        return market + "_" + getName() + "_" + dao.getName() + "_" +  model.getName() + "_" + conf.getPredictorsDays() + "_" + conf.getPredictorsFuturedays() + "_" + in + "_" + out;
    }

}

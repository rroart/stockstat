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

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.constants.ResultMetaConstants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.NeuralNetTensorflowConfig;
import roart.common.ml.TensorflowPredictorLSTMConfig;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.pipeline.data.PipelineData;
import roart.common.util.MathUtil;
import roart.common.util.PipelineUtils;
import roart.common.util.JsonUtil;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.dao.MLClassifyDao;
import roart.ml.model.LearnTestClassifyResult;
import roart.pipeline.Pipeline;
import roart.pipeline.common.predictor.AbstractPredictor;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.stockutil.StockUtil;
import roart.ml.model.LearnClassify;
import roart.common.pipeline.data.TwoDimD;
import roart.common.pipeline.data.TwoDimd;

public abstract class Predictor extends AbstractPredictor {

    public Predictor(IclijConfig conf, String string, String title, int category, NeuralNetCommand neuralnetcommand, PipelineData[] datareaders) {
        super(conf, string, category, neuralnetcommand);
        if (!isEnabled()) {
            return;
        }
        this.title = title;
        this.key = title;
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
    }

    String key;
    protected Map<String, Double[][]> listMap;
    protected Map<String, Double[][]> fillListMap;

    //protected Map<String, double[][]> truncListMap;
    protected Map<String, double[][]> truncFillListMap;

    //protected Map<String, Double[][]> base100ListMap;
    //protected Map<String, Double[][]> base100FillListMap;

    //protected Map<String, double[][]> truncBase100ListMap;
    //protected Map<String, double[][]> truncBase100FillListMap;
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

    private PipelineData[] datareaders;

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
    public void calculate() throws Exception { // IclijConfig conf, Map<String, MarketData> marketdatamap,
        if (!isEnabled()) {
            return;
        }

        log.info("checkthis {}", key.equals(title));
        Map<String, PipelineData> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        PipelineData datareader = pipelineMap.get(key);
        if (datareader == null) {
            log.info("empty {}", category);
            return;
        }
        this.listMap = PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) datareader.get(PipelineConstants.LIST));
        this.fillListMap = PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) datareader.get(PipelineConstants.FILLLIST));
        //this.truncListMap = PipelineUtils.convertTwoDimd((Map<String, TwoDimd>) datareader.get(PipelineConstants.TRUNCLIST));       
        this.truncFillListMap = PipelineUtils.convertTwoDimd((Map<String, TwoDimd>) datareader.get(PipelineConstants.TRUNCFILLLIST));       
        //this.base100ListMap = PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) datareader.get(PipelineConstants.BASE100LIST));
        //this.base100FillListMap = PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) datareader.get(PipelineConstants.BASE100FILLLIST));
        //this.truncBase100ListMap = PipelineUtils.convertTwoDimd((Map<String, TwoDimd>) datareader.get(PipelineConstants.TRUNCBASE100LIST));       
        //this.truncBase100FillListMap = PipelineUtils.convertTwoDimd((Map<String, TwoDimd>) datareader.get(PipelineConstants.TRUNCBASE100FILLLIST));       

        long time0 = System.currentTimeMillis();
        // note that there are nulls in the lists with sparse
        //Map<String, Double[][]> retArray = StockDao.getArrSparse(conf, conf.getMarket(), dateme, category, conf.getDays(), conf.getTableIntervalDays(), marketdatamap, false);
        //this.listMap = retArray;
        //this.truncListMap = ArraysUtil.getTruncListArr(this.listMap);
        if (!anythingHereNot(listMap)) {
            log.info("empty {}", key);
            return;
        }
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        accuracyMap = new HashMap<>();
        lossMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();

        List<String> dateList = (List<String>) pipelineMap.get(key).get(PipelineConstants.DATELIST);
        Integer days = conf.getDays();
        if (days == 0) {
            days = dateList.size();
        }
        
        NeuralNetConfigs nnConfigs = new NeuralNetConfigs();
        String nnconfigString = getMyNeuralNetConfig();
        if (nnconfigString != null) {
            nnConfigs = JsonUtil.convertnostrip(nnconfigString, NeuralNetConfigs.class);
            //TensorflowPredictorLSTMConfig lstmConfig = mapper.readValue(nnconfigString, TensorflowPredictorLSTMConfig.class);
            //nnConfigs.setTensorflowConfig(new NeuralNetTensorflowConfig(null, null, null, null, null, null, null, null, null));
            //nnConfigs.getTensorflowConfig().setTensorflowPredictorLSTMConfig(lstmConfig);
        }

        long time1 = System.currentTimeMillis();
        log.info("listmap {} {}", listMap.size(), listMap.keySet());
        for (String id : listMap.keySet()) {
            Double[][] list0 = listMap.get(id);
            Double[] list = list0[0];
            log.info("list {} {}", list.length, list);
        }
        Map<Double, Map<MLClassifyModel, Map<String, Double[]>>> mapResult0 = new HashMap<>();
        Double[] thresholds = getThresholds();
        for (Double threshold : thresholds) {

        Map<MLClassifyModel, Map<String, Double[]>> mapResult = new HashMap<>();
        mapResult0.put(threshold, mapResult);
        if (conf.wantML()) {
            if (false && conf.wantPercentizedPriceIndex()) {
                //doPredictions(conf, mapResult, base100FillListMap, truncBase100FillListMap, nnConfigs, days, threshold);
            } else {
                doPredictions(conf, mapResult, fillListMap, truncFillListMap, nnConfigs, days, threshold);                
            }
        }
        }
        // TODO createResultMap(conf, mapResult0, false && conf.wantPercentizedPriceIndex() ? base100FillListMap : fillListMap);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        handleSpentTime(conf);

    }

    protected String getMyNeuralNetConfig() {
        return conf.getPredictorsMLConfig();
    }

    protected abstract String getNeuralNetConfig();

    private void doPredictions(IclijConfig conf, Map<MLClassifyModel, Map<String, Double[]>> mapResult, Map<String, Double[][]> aListMap, Map<String, double[][]> aTruncListMap, NeuralNetConfigs nnConfigs, int days, Double threshold) {
        try {
            for (MLClassifyDao mldao : mldaos) {
                if (mldao.getModels().size() != 1) {
                    log.error("Models size is {}", mldao.getModels().size());
                }
                for (MLClassifyModel model : mldao.getModels(predictorName())) {
                    List<LearnClassify> map = new ArrayList<>();
                    // days find max
                    days = 0;
                    for (String id : aListMap.keySet()) {
                        //Double[] listl = aListMap.get(id)[0];
                        double[][] list0 = aTruncListMap.get(id);
                        double[] list = list0[0];
                        // check reverse. move up before if?
                        if (list != null && list.length > days) {
                            days = list.length;
                        }
                    }
                    log.info("list days {}", days);
                    for (String id : aListMap.keySet()) {
                        //Double[] listl = aListMap.get(id)[0];
                        double[][] list0 = aTruncListMap.get(id);
                        double[] list = list0[0];
                        // check reverse. move up before if?
                        if (list != null && list.length == days) {
                            log.info("list {}", list.length);
                            Object list3 = ArrayUtils.toObject(list);
                            map.add(new LearnClassify(id, list3, (Double) null));
                        }
                    }
                    List<LearnClassify> classifylist = new ArrayList<>();
                    for (String id : aListMap.keySet()) {
                        //Double[] listl = aListMap.get(id)[0];
                        double[][] list0 = aTruncListMap.get(id);
                        double[] list = list0[0];
                        // check reverse. move up before if?
                        if (list != null && list.length >= conf.getPredictorsDays()) {
                            log.info("list {}", list.length);
                            Object list3 = ArrayUtils.toObject(Arrays.copyOfRange(list, list.length - conf.getPredictorsDays(), list.length));
                            classifylist.add(new LearnClassify(id, list3, (Double) null));
                        }
                    }
                    // make OO of this, create object
                    Object[] meta = new Object[ResultMetaConstants.SIZE];
                    meta[ResultMetaConstants.MLNAME] = mldao.getName();
                    meta[ResultMetaConstants.MODELNAME] = model.getName();
                    meta[ResultMetaConstants.RETURNSIZE] = model.getReturnSize();
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
                    String filename = getFilename(mldao, model, "" + conf.getPredictorsDays(), "" + conf.getPredictorsFuturedays(), conf.getConfigData().getMarket(), null);
                    String path = model.getPath();
                    LearnTestClassifyResult result = mldao.learntestclassify(nnConfigs, null, map, model, conf.getPredictorsDays(), conf.getPredictorsFuturedays(), mapTime, classifylist, null, path, filename, neuralnetcommand, mlmeta, false);  
                    lossMap.put(mldao.getName() + model.getName(), result.getLoss());
                    meta[ResultMetaConstants.LOSS] = result.getLoss();
                    meta[ResultMetaConstants.THRESHOLD] = threshold;
                    resultMeta.setLoss(result.getLoss());
                    resultMeta.setThreshold(threshold);
                    if (!neuralnetcommand.isMldynamic() && neuralnetcommand.isMllearn()) {
                        neuralnetcommand.setMllearn(false);
                        neuralnetcommand.setMlclassify(true);
                        result = mldao.learntestclassify(nnConfigs, null, map, model, conf.getPredictorsDays(), conf.getPredictorsFuturedays(), mapTime, classifylist, null, path, filename, neuralnetcommand, mlmeta, false);  
                    } else {
                        
                    }
                    Map<String, Double[]> classifyResult = result.getCatMap();
                    Map<String, Double[]> classifyResultNew = new HashMap<>();
                    mapResult.put(model, classifyResultNew);
                    double accuracy = 0;
                    //accuracy = result.getAccuracy();
                    accuracy = calculateAccuracy(aListMap, threshold, classifyResult, classifyResultNew);
                    accuracyMap.put(mldao.getName() + model.getName(), accuracy);
                    meta[ResultMetaConstants.TESTACCURACY] = accuracy;
                    resultMeta.setTestAccuracy(accuracy);
                }
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
    }

    private double calculateAccuracy(Map<String, Double[][]> aListMap, Double threshold,
            Map<String, Double[]> classifyResult, Map<String, Double[]> classifyResultNew) {
        double accuracy = 0;
        if (classifyResult != null) {
            int count = 0;
            int total = 0;
            for (Entry<String, Double[]> entry : classifyResult.entrySet()) {
                String key = entry.getKey();
                Double[][] v = aListMap.get(key);
                Double[] list = v[0];
                Double val = list[list.length - 1];
                Double[] list2 = entry.getValue();
                Double val2 = list2[list2.length - 1];
                if (val != null && val2 != null) {
                    double abovebelow = 2.0;
                    if (val2 / val > threshold) {
                        count++;
                        abovebelow = 1.0;
                    }
                    classifyResultNew.put(key, new Double[] { abovebelow });
                }
                total++;
            }
            if (total != 0) {
                accuracy = ( (double) count) / total;
            }
        }
        return accuracy;
    }

    private Map<String, Double[]> getMapResult(IclijConfig conf, MLClassifyDao mldao, int horizon, int windowsize,
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

    public static Map<Double, String> createLabelMapShort() {
        Map<Double, String> labelMap1 = new HashMap<>();
        labelMap1.put(1.0, Constants.ABOVE);
        labelMap1.put(2.0, Constants.BELOW);
        return labelMap1;
    }

    private void createResultMap(IclijConfig conf, Map<Double, Map<MLClassifyModel, Map<String, Double[]>>> mapResult0, Map<String, Double[][]> aListMap) {
        Map<Double, String> labelMapShort = createLabelMapShort();
        for (String id : listMap.keySet()) {
            Object[] fields = new Object[fieldSize];
            resultMap.put(id, fields);
            int retindex = 0 ;

            // make OO of this
            if (conf.wantML()) {
                Double[] thresholds = getThresholds();
                for (Double threshold : thresholds) {
                Map<MLClassifyModel, Map<String, Double[]>> mapResult = mapResult0.get(threshold);    
                for (MLClassifyDao mldao : mldaos) {
                    for (MLClassifyModel model : mldao.getModels(predictorName())) {
                        Map<String, Double[]> resultMap = mapResult.get(model);
                        Double[] aType = null;
                        if (resultMap != null) {
                            aType = resultMap.get(id);
                        } else {
                            log.info("map null  {}");
                        }
                        fields[retindex++] = aType != null ? labelMapShort.get(aType[0]) : null;
                        /*
                        Double[] aType = null;
                        if (resultMap != null) {
                            aType = resultMap.get(id);
                            if (aType != null) {
                                fields[retindex++] = aType[aType.length - 1];
                            }
                        }
                        */
                    }   
                }
            }
            }
        }
    }

    private void handleSpentTime(IclijConfig conf) {
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

    protected boolean anythingHereA(Map<String, Double[][]> myListMap) {
        for (Double[][] array : myListMap.values()) {
            for (int i = 0; i < array[0].length; i++) {
                if (array[0][i] != null) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean anythingHere(Map<String, List<List<Double>>> listMap2) {
        for (List<List<Double>> array : listMap2.values()) {
            for (int i = 0; i < array.get(0).size(); i++) {
                if (array.get(0).get(i) != null) {
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
        String market = conf.getConfigData().getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new ImmutablePair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
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
        Map<String, PipelineData> pipelineMap = IndicatorUtils.getPipelineMap(datareaders);
        PipelineData datareader = pipelineMap.get(key);
        return anythingHereA(PipelineUtils.convertTwoDimD((Map<String, TwoDimD>) datareader.get(PipelineConstants.LIST)));
    }
    
    @Override
    public String getName() {
        return PipelineConstants.PREDICTOR;
    }

    public String getFilename(MLClassifyDao dao, MLClassifyModel model, String in, String out, String market, List indicators) {
        String testmarket = conf.getConfigData().getMlmarket();
        if (testmarket != null) {
            market = testmarket;
        }
        return market + "_" + getName() + "_" + dao.getName() + "_" +  model.getName() + "_" + conf.getPredictorsDays() + "_" + conf.getPredictorsFuturedays() + "_" + in + "_" + out;
    }

    private Double[] getThresholds() {
        boolean gui = conf.getConfigData().getConfigValueMap().get(ConfigConstants.MISCTHRESHOLD) != null;
        log.info("GUI thresholds {}", gui);
        String thresholdString = conf.getAggregatorsIndicatorThreshold();
        if (gui) {
            thresholdString = conf.getThreshold();
        }
        try {
            Double.valueOf(thresholdString);
            log.error("Using old format {}", thresholdString);
            thresholdString = "[" + thresholdString + "]";
        } catch (Exception e) {            
        }
        return JsonUtil.convert(thresholdString, Double[].class);
    }

}

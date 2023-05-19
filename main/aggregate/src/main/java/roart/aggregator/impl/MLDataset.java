package roart.aggregator.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.pipeline.common.aggregate.Aggregator;
import roart.category.AbstractCategory;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.common.model.StockItem;
import roart.common.pipeline.PipelineConstants;
import roart.common.util.ArraysUtil;
import roart.executor.MyExecutors;
import roart.indicator.AbstractIndicator;
import roart.indicator.util.IndicatorUtils;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.dao.MLClassifyDao;
import roart.ml.dao.MLClassifyDatasetCallable;
import roart.ml.dao.MLClassifyLearnTestPredictCallable;
import roart.ml.model.LearnTestClassifyResult;
import roart.model.data.MarketData;
import roart.model.data.PeriodData;
import roart.pipeline.Pipeline;
import roart.pipeline.data.ExtraData;
import roart.result.model.ResultItemTableRow;
import roart.result.model.ResultMeta;
import roart.talib.Ta;
import roart.talib.impl.TalibMACD;
import roart.talib.util.TaUtil;

public class MLDataset extends Aggregator {

    Map<String, PeriodData> periodDataMap;
    String key;
    Map<String, Double[][]> listMap;
    Object[] emptyField;
    Map<MLClassifyModel, Long> mapTime = new HashMap<>();

    List<ResultItemTableRow> mlTimesTableRows = null;
    List<ResultItemTableRow> eventTableRows = null;

    private static final String MYTITLE = "comb";

    @Override
    public Map<String, Object> getResultMap() {
        return new HashMap<>();
    }

    public Map<String, Object[]> getObjectMap() {
        return objectMap;
    }

    public Map<String, Double[][]> getListMap() {
        return listMap;
    }

    private int fieldSize = 0;

    public static final int MULTILAYERPERCEPTRONCLASSIFIER = 1;
    public static final int LOGISTICREGRESSION = 2;

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

    public MLDataset(IclijConfig conf, String string, Map<String, MarketData> marketdatamap, Map<String, PeriodData> periodDataMap, 
            String title, int category, AbstractCategory[] categories, Pipeline[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        super(conf, string, category);
        this.periodDataMap = periodDataMap;
        this.key = title;
        makeMapTypes();
        if (conf.wantML()) {
            if (true) {
                if (conf.wantMLSpark()) {
                    mldaos.add(new MLClassifyDao(MLConstants.SPARK, conf));
                }
                if (conf.wantMLTensorflow()) {
                    mldaos.add(new MLClassifyDao(MLConstants.TENSORFLOW, conf));
                }
                if (conf.wantMLPytorch()) {
                    mldaos.add(new MLClassifyDao(MLConstants.PYTORCH, conf));
                }
                if (conf.wantMLGem()) {
                    mldaos.add(new MLClassifyDao(MLConstants.GEM, conf));
                }
            } else {
                mldaos.add(new MLClassifyDao(MLConstants.RANDOM, conf));
            }
        }
        fieldSize = fieldSize();
        if (conf.wantMLTimes()) {
            mlTimesTableRows = new ArrayList<>();
        }
        if (conf.wantOtherStats()) {
            eventTableRows = new ArrayList<>();
        }
        if (isEnabled()) {
            calculate(conf, marketdatamap, categories, datareaders, neuralnetcommand);
            cleanMLDaos();
        }
    }

    private void cleanMLDaos() {
        for (MLClassifyDao mldao : mldaos) {
            mldao.clean();
        }        
    }

    @Override
    public Map<Integer, String> getMapTypes() {
        return mapTypes;
    }

    private void makeMapTypes() {
        mapTypes.put(0, MYTITLE);
    }

    @Override
    public List<Integer> getTypeList() {
        List<Integer> retList = new ArrayList<>();
        retList.add(0);
        return retList;
    }

    private Map<Integer, String> mapTypes = new HashMap<>();

    private void calculate(IclijConfig conf, Map<String, MarketData> marketdatamap,
            AbstractCategory[] categories, Pipeline[] datareaders, NeuralNetCommand neuralnetcommand) throws Exception {
        Map<String, Pipeline> pipelineMap = new HashMap<>();
        long time0 = System.currentTimeMillis();
        log.info("time0 {}", (System.currentTimeMillis() - time0));
        resultMap = new HashMap<>();
        otherResultMap = new HashMap<>();
        accuracyMap = new HashMap<>();
        lossMap = new HashMap<>();
        resultMetaArray = new ArrayList<>();
        otherMeta = new ArrayList<>();
        objectMap = new HashMap<>();
        long time2 = System.currentTimeMillis();
        log.info("time2 {}", (System.currentTimeMillis() - time2));
        long time1 = System.currentTimeMillis();

        Map<String, Map<Object, Double>> mapMap = new HashMap<>();
        // map from h/m to model to posnegcom map<model, results>
        Map<MLClassifyModel, Map<String, Double[]>> mapResult = new HashMap<>();
        log.info("Period {} {}", title, mapMap.keySet());
        String nnconfigString = conf.getDatasetMLConfig();
        NeuralNetConfigs nnConfigs = null;
        if (nnconfigString != null) {
            ObjectMapper mapper = new ObjectMapper();
            nnConfigs = mapper.readValue(nnconfigString, NeuralNetConfigs.class);
        }
        if (conf.wantML()) {
            // add a null check

            MLMeta mlmeta = new MLMeta();
            mlmeta.classify = true;
            mlmeta.features = true;
            String dataset = conf.getConfigData().getMarket();
            boolean multi = neuralnetcommand.isMldynamic() || (neuralnetcommand.isMlclassify() && !neuralnetcommand.isMllearn());
            if (multi /*conf.wantMLMP()*/) {
                doLearnTestClassifyFuture(nnConfigs, conf, mlmeta, neuralnetcommand, dataset);
            } else {
                doLearnTestClassify(nnConfigs, conf, mlmeta, neuralnetcommand, dataset);
           }
        }
        createResultMap(conf, mapResult);
        log.info("time1 {}", (System.currentTimeMillis() - time1));
        // and others done with println
        handleSpentTimes(conf);

    }

    private void doLearnTestClassify(NeuralNetConfigs nnconfigs, IclijConfig conf, MLMeta mlmeta,
            NeuralNetCommand neuralnetcommand, String dataset) {
        try {
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {          
                    boolean mldynamic = conf.wantMLDynamic();
                    LearnTestClassifyResult result = mldao.dataset(nnconfigs, model, mapTime, neuralnetcommand, mlmeta, dataset);  
                    if (result == null) {
                        continue;
                    }
                    accuracyMap.put(mldao.getName() + model.getName(), result.getAccuracy());
                    lossMap.put(mldao.getName() + model.getName(), result.getLoss());
               }
            }
        } catch (Exception e1) {
            log.error("Exception", e1);
        }
        for (MLClassifyDao mldao : mldaos) {
            for (MLClassifyModel model : mldao.getModels()) {
            }
        }
    }

    private void doLearnTestClassifyFuture(NeuralNetConfigs nnconfigs, IclijConfig conf, MLMeta mlmeta,
            NeuralNetCommand neuralnetcommand, String dataset) {
        try {
            // calculate sections and do ML
            List<Future<LearnTestClassifyResult>> futureList = new ArrayList<>();
            Map<Future<LearnTestClassifyResult>, FutureMap> futureMap = new HashMap<>();
            for (MLClassifyDao mldao : mldaos) {
                for (MLClassifyModel model : mldao.getModels()) {          
                    boolean mldynamic = conf.wantMLDynamic();
                    Callable callable = new MLClassifyDatasetCallable(nnconfigs, mldao, model, mapTime, neuralnetcommand, mlmeta, dataset);  
                    Future<LearnTestClassifyResult> future = MyExecutors.run(callable, 1);
                    futureList.add(future);
                    futureMap.put(future, new FutureMap(mldao, model, resultMetaArray.size() - 1));
                }
            }
            for (Future<LearnTestClassifyResult> future: futureList) {
                FutureMap futMap = futureMap.get(future);
                MLClassifyDao mldao = futMap.getDao();
                MLClassifyModel model = futMap.getModel();
                int testCount = futMap.getTestCount();
                LearnTestClassifyResult result = future.get();
                if (result == null) {
                    continue;
                }
                Map<String, Double[]> classifyResult = result.getCatMap();
                accuracyMap.put(mldao.getName() + model.getName(), result.getAccuracy());
                lossMap.put(mldao.getName() + model.getName(), result.getLoss());
            }
        } catch (Exception e1) {
            log.error("Exception", e1);
        }
    }

    private void handleSpentTimes(IclijConfig conf) {
        if (conf.wantMLTimes()) {
            for (Map.Entry<MLClassifyModel, Long> entry : mapTime.entrySet()) {
                MLClassifyModel model = entry.getKey();
                ResultItemTableRow row = new ResultItemTableRow();
                row.add("MLDataset " + key);
                row.add(model.getEngineName());
                row.add(model.getName());
                row.add(entry.getValue());
                mlTimesTableRows.add(row);
            }
        }
    }

    private void createResultMap(IclijConfig conf, Map<MLClassifyModel, Map<String, Double[]>> mapResult) {
        // empty
    }

    public static void mapAdder(Map<MLClassifyModel, Long> map, MLClassifyModel key, Long add) {
        Long val = map.get(key);
        if (val == null) {
            val = Long.valueOf(0);
        }
        val += add;
        map.put(key, val);
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
    public Object calculate(double[][] array) {
        Ta tu = new TalibMACD();
        return tu.calculate(array);
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public Object[] getResultItem(StockItem stock) {
        String market = conf.getConfigData().getMarket();
        String id = stock.getId();
        Pair<String, String> pair = new ImmutablePair<>(market, id);
        Set<Pair<String, String>> ids = new HashSet<>();
        ids.add(pair);
        String periodstr = key;
        PeriodData perioddata = periodDataMap.get(periodstr);
        if (perioddata == null) {
            log.info("key {} : {}", key, periodDataMap.keySet());
        }
        Object[] result = resultMap.get(id);
        if (result == null) {
            result = emptyField;
        }
        return result;
    }

    @Override
    public Object[] getResultItemTitle() {
        Object[] objs = new Object[fieldSize];
        return objs;
   }

    private int fieldSize() {
        int size = 0;
        for (MLClassifyDao mldao : mldaos) {
            size += mldao.getSizes(this);
        }
        emptyField = new Object[size];
        return size;
    }

    @Override
    public void addResultItem(ResultItemTableRow row, StockItem stock) {
        Object[] objs = new Object[fieldSize];
        Object[] fields = objs;
        if (resultMap != null) {
            fields = resultMap.get(stock.getId());
        }
        row.addarr(fields);        
    }

    @Override
    public void addResultItemTitle(ResultItemTableRow headrow) {
        int retindex = 0;
        Object[] objs = new Object[fieldSize];
        getTitles(retindex, objs);
        headrow.addarr(objs);
    }

    private int getTitles2(int retindex, Object[] objs) {
        for (MLClassifyDao mldao : mldaos) {
            //retindex = mldao.addTitles(objs, retindex, this, title, key, MYTITLE);
        }
        return retindex;
    }

    private int getTitles(int retindex, Object[] objs) {
        // make OO of this
        for (MLClassifyDao mldao : mldaos) {
            for (MLClassifyModel model : mldao.getModels()) {
                List<Integer> typeList = getTypeList();
                for (int mapTypeInt : typeList) {
                    String mapType = mapTypes.get(mapTypeInt);
                    String val = "";
                    // workaround
                    try {
                        val = "" + MLClassifyModel.roundme((Double) accuracyMap.get(mldao.getName() + model.getId()));
                    } catch (Exception e) {
                        log.error("Exception fix later, refactor", e);
                    }
                    objs[retindex++] = title + " " + "mlind" + Constants.WEBBR +  model.getShortName() + mapType + " " + val;
                    if (model.getReturnSize() > 1) {
                        objs[retindex++] = title + " " + "mlind" + Constants.WEBBR +  model.getShortName() + mapType + " prob ";
                    }
                }
            }
        }
        return retindex;
    }

    @Override
    public String getName() {
        return PipelineConstants.DATASET;
    }

    private class FutureMap {
        private MLClassifyDao dao;

        private MLClassifyModel model;

        private int testCount;
        
        public FutureMap(MLClassifyDao dao, MLClassifyModel model, int testCount) {
            super();
            this.dao = dao;
            this.model = model;
            this.testCount = testCount;
        }

        public MLClassifyDao getDao() {
            return dao;
        }

        public void setDao(MLClassifyDao dao) {
            this.dao = dao;
        }

        public MLClassifyModel getModel() {
            return model;
        }

        public void setModel(MLClassifyModel model) {
            this.model = model;
        }

        public int getTestCount() {
            return testCount;
        }

        public void setTestCount(int testCount) {
            this.testCount = testCount;
        }
    }
}

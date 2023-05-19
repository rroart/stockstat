package roart.ml.spark;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestPredictResult;
import roart.ml.model.MLPredictAccess;
import roart.ml.model.MLPredictModel;
import roart.ml.spark.util.SparkUtil;
import roart.pipeline.common.predictor.AbstractPredictor;

public class MLPredictSparkAccess extends MLPredictAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected SparkSession spark;

    private IclijConfig conf;

    private Map<String, Model> modelMap = new HashMap<>();
    private Map<String, Double> accuracyMap = new HashMap<>();

    public MLPredictSparkAccess(IclijConfig conf) {
        this.conf = conf;
        findModels();	
        String sparkmaster = conf.getMLSparkMaster();
        Integer timeout = conf.getMLSparkTimeout();
        spark = SparkUtil.createSparkSession(sparkmaster, "Stockstat ML", timeout);
    }


    private void findModels() {
        models = new ArrayList<>();
    }
    
    @Override
    public LearnTestPredictResult predictone(NeuralNetConfigs nnconfigs, AbstractPredictor predictor, Double[] array, MLPredictModel model, int size,
            String period, int outcomes) {
        return learntestInner(array, null, model, size, period, null, outcomes);       
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        return evalInner(modelInt, period, mapname);
    }

    @Override
    public LearnTestPredictResult predict(NeuralNetConfigs nnconfigs, AbstractPredictor indicator, Map<String, Double[]> map, MLPredictModel model,
            int size, String period, int outcomes) {
         return null;
    }

    @Override
    public List<MLPredictModel> getModels() {
        return models;
    }

    public Map<String, Double[]> classifyInner(Map<String, double[]> map, int modelInt, int size, String period, int outcomes) {
        return null;
    }

    public LearnTestPredictResult learntestInner(Double[] array, Map<double[], Double> map, MLPredictModel mlmodel, int size, String period, String mapname, int outcomes) {
        long time0 = System.currentTimeMillis();
        if (spark == null) {
            return null;
        }
        if (map.isEmpty()) {
            return null;
        }
        Map<Double, Long> counts =
                map.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));       
        log.info("learning distribution {}", counts);
        try {
            Dataset<Row> data = SparkUtil.createDFfromMap(spark, map);
            Dataset<Row>[] splits = data.randomSplit(new double[]{0.6, 0.4}, 1234);
            Dataset<Row> train = splits[0];
            Dataset<Row> test = splits[1];
            log.info("data size {} {}", map.size(), train.count());
            if (train.count() == 0) {
                train = data;
                test = data;
            }
            MLPredictSparkModel sparkModel = (MLPredictSparkModel) mlmodel;
            Model model = sparkModel.getModel(train, size, outcomes);

            modelMap.put(mlmodel.getId()+period+mapname, model);
            // compute accuracy on the test set                                         
            Dataset<Row> result = model.transform(test);
            Dataset<Row> predictionAndLabels = result.select("prediction", "label");
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setMetricName("accuracy");
            double eval = evaluator.evaluate(predictionAndLabels);
            log.info("Test set accuracy for {} {} {} = {}", mapname, mlmodel.getId(), period, eval);
            accuracyMap.put(mlmodel.getId()+period+mapname, eval);
        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            log.info("time learn test model {} {} {} {}", mlmodel.getName(), period, map.size(), (System.currentTimeMillis() - time0));
        }
        return null;
    }

    public Double evalInner(int modelInt, String period, String mapname) {
        return accuracyMap.get(modelInt+period+mapname);
    }

}


package roart.ml.spark;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.spark.ContextCleaner;
import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator;
import org.apache.spark.ml.linalg.DenseVector;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.spark.util.SparkUtil;
import roart.pipeline.common.aggregate.Aggregator;
import scala.Option;

public class MLClassifySparkAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected SparkSession spark;

    private MyMyConfig conf;

    private Map<String, Model> modelMap = new HashMap<>();
    private Map<String, Double> accuracyMap = new HashMap<>();

    public MLClassifySparkAccess(MyMyConfig conf) {
        this.conf = conf;
        findModels();	
        String sparkmaster = conf.getMLSparkMaster();
        Integer timeout = conf.getMLSparkTimeout();
        spark = SparkUtil.createSparkSession(sparkmaster, "Stockstat ML", timeout);
    }


    private void findModels() {
        models = new ArrayList<>();
        if (conf.wantMCP()) {
            MLClassifyModel model = new MLClassifySparkMCPModel(conf);
            models.add(model);
        }
        if (conf.wantLR()) {
            MLClassifyModel model = new MLClassifySparkLRModel(conf);
            models.add(model);
        }
        if (conf.wantOVR()) {
            MLClassifyModel model = new MLClassifySparkOVRModel(conf);
            models.add(model);
        }
    }
    @Override
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, Map<double[], Double> map, MLClassifyModel model, int size, String period,
            String mapname, int outcomes) {
        return learntestInner(nnconfigs, map, model, size, period, mapname, outcomes);       
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        return evalInner(modelInt, period, mapname);
    }

    @Override
    public Map<String, Double[]> classify(Aggregator indicator, Map<String, double[]> map, MLClassifyModel model, int size,
            String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        return classifyInner(map, Integer.valueOf(model.getId()), size, period, mapname, outcomes, shortMap);
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    public Map<String, Double[]> classifyInner(Map<String, double[]> map, int modelInt, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        long time0 = System.currentTimeMillis();
        Map<String, Double[]> retMap = new HashMap<>();
        Dataset<Row> data = SparkUtil.createDFfromMap2(spark, map);
        try {
            Model model = modelMap.get(modelInt+period+mapname);
            if (model == null) {
                return retMap;
            }
            Dataset<Row> resultDF = model.transform(data);

            for (Row row : resultDF.collectAsList()) {
                String id = row.getAs("id");
                Double predict = row.getAs("prediction");
                Double prob = null;
                if (MLConstants.LOGISTICREGRESSION == modelInt) {
                    try {
                        DenseVector probvector = row.getAs("probability");
                        double[] probarray = probvector.values();
                        prob = probarray[predict.intValue()];
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
                Double[] retVal = new Double[2];
                retVal[0] = predict;
                retVal[1] = prob;
                retMap.put(id, retVal);
            }
            log.info("classify done");
            return retMap;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        } finally {
            log.info("time classify model {} {} {} {}", modelInt, period, map.size(), (System.currentTimeMillis() - time0));            
        }
        return null;
    }

    public Double learntestInner(NeuralNetConfigs nnconfigs, Map<double[], Double> map, MLClassifyModel mlmodel, int size, String period, String mapname, int outcomes) {
        Double accuracy = null;
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
            MLClassifySparkModel sparkModel = (MLClassifySparkModel) mlmodel;
            Model model = sparkModel.getModel(nnconfigs, train, size, outcomes);

            modelMap.put(mlmodel.getId()+period+mapname, model);
            // compute accuracy on the test set                                         
            Dataset<Row> result = model.transform(test);
            Dataset<Row> predictionAndLabels = result.select("prediction", "label");
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setMetricName("accuracy");
            double eval = evaluator.evaluate(predictionAndLabels);
            log.info("Test set accuracy for {} {} {} = {}", mapname, mlmodel.getId(), period, eval);
            accuracyMap.put(mlmodel.getId()+period+mapname, eval);
            accuracy = eval;

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            log.info("time learn test model {} {} {} {}",mlmodel.getName(), period, map.size(),(System.currentTimeMillis() - time0));
        }
        return accuracy;
    }

    public Double evalInner(int modelInt, String period, String mapname) {
        return accuracyMap.get(modelInt+period+mapname);
    }

    @Override
    public String getName() {
        return ConfigConstants.SPARK;
    }


    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfig, Aggregator indicator, Map<double[], Double> map,
            MLClassifyModel mlmodel, int size, String period, String mapname, int outcomes, Map<String, double[]> map2,
            Map<Double, String> shortMap) {
        Double accuracy = null;
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
            //train.show();
            Dataset<Row> test = splits[1];
            log.info("data size {} {}", map.size(), train.count());
            if (train.count() == 0) {
                train = data;
                test = data;
            }
            for (double[] e : map.keySet()) {
                log.info(" e " + Arrays.asList(e));
            }
            for (double[] e : map.keySet()) {
                List<Double> l = Arrays.stream(e).boxed().collect(Collectors.toList());
                log.info(" e " + l.size() + " " + l);
            }
            MLClassifySparkModel sparkModel = (MLClassifySparkModel) mlmodel;
            Model model = sparkModel.getModel(nnconfig, train, size, outcomes);

            // compute accuracy on the test set                                         
            Dataset<Row> result = model.transform(test);
            Dataset<Row> predictionAndLabels = result.select("prediction", "label");
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setMetricName("accuracy");
            double eval = evaluator.evaluate(predictionAndLabels);
            log.info("Test set accuracy for {} {} {} = {}", mapname, mlmodel.getId(), period, eval);
            accuracyMap.put(mlmodel.getId()+period+mapname, eval);
            accuracy = eval;
            Map<String, Double[]> retMap = new HashMap<>();
            Dataset<Row> data2 = SparkUtil.createDFfromMap2(spark, map2);
            int modelInt = Integer.valueOf(mlmodel.getId());
            Dataset<Row> resultDF = model.transform(data2);

            for (Row row : resultDF.collectAsList()) {
                String id = row.getAs("id");
                Double predict = row.getAs("prediction");
                if (!counts.keySet().contains(predict)) {
                    log.error("Prediction does not exist {}", predict);
                    continue;
                }
                Double prob = null;
                if (MLConstants.LOGISTICREGRESSION == modelInt) {
                    try {
                        DenseVector probvector = row.getAs("probability");
                        double[] probarray = probvector.values();
                        prob = probarray[predict.intValue()];
                    } catch (Exception e) {
                        log.error(Constants.EXCEPTION, e);
                    }
                }
                Double[] retVal = new Double[2];
                retVal[0] = predict;
                retVal[1] = prob;
                retMap.put(id, retVal);
            }
            log.info("classify done");
            LearnTestClassifyResult result2 = new LearnTestClassifyResult();
            result2.setAccuracy(accuracy);
            result2.setCatMap(retMap);
            return result2;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        } finally {
            log.info("time learn test classify model {} {} {} {}", Integer.valueOf(mlmodel.getId()), period, map.size(), (System.currentTimeMillis() - time0));            
        }
        return null;
    }

    @Override
    public void clean() {
        /*
        Option<ContextCleaner> i = spark.sparkContext().cleaner();
        if (i.isDefined()) {
            i.get().start();
        }
        */
        spark.sparkContext().clean$default$2();
        log.info("nam " + spark.sparkContext().appName() + " " + spark.sparkContext().applicationId() + " " + spark.sparkContext().applicationId());
        //spark.close();
    }
}


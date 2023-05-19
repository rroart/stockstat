package roart.ml.spark;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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
import org.apache.spark.ml.PipelineModel;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetCommand;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyAccess;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;
import roart.ml.model.LearnTestClassifyResult;
import roart.ml.spark.util.SparkUtil;
import roart.pipeline.common.aggregate.Aggregator;
import scala.Option;

public class MLClassifySparkAccess extends MLClassifyAccess {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    protected SparkSession spark;

    private IclijConfig conf;

    private Map<String, Model> modelMap = new HashMap<>();
    private Map<String, Double> accuracyMap = new HashMap<>();

    public MLClassifySparkAccess(IclijConfig conf) {
        this.conf = conf;
        findModels();	
        String sparkmaster = conf.getMLSparkMaster();
        Integer timeout = conf.getMLSparkTimeout();
        spark = SparkUtil.createSparkSession(sparkmaster, "Stockstat ML", timeout);
    }


    private void findModels() {
        models = new ArrayList<>();
        if (conf.wantSparkMLPC()) {
            MLClassifyModel model = new MLClassifySparkMLPCModel(conf);
            models.add(model);
        }
        if (conf.wantSparkLOR()) {
            MLClassifyModel model = new MLClassifySparkLORModel(conf);
            models.add(model);
        }
        if (conf.wantSparkOVR()) {
            MLClassifyModel model = new MLClassifySparkOVRModel(conf);
            models.add(model);
        }
        if (conf.wantSparkLSVC()) {
            MLClassifyModel model = new MLClassifySparkLSVCModel(conf);
            models.add(model);
        }
    }
    @Override
    public Double learntest(NeuralNetConfigs nnconfigs, Aggregator indicator, List<Triple<String, Object, Double>> map, MLClassifyModel model, int size,
            int outcomes, String filename) {
        return learntestInner(nnconfigs, map, model, size, outcomes, filename);       
    }

    @Override
    public Double eval(int modelInt) {
        return evalInner(modelInt);
    }

    @Override
    public Map<String, Double[]> classify(Aggregator indicator, List<Triple<String, Object, Double>> map, MLClassifyModel model, int size,
            int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        return classifyInner(map, Integer.valueOf(model.getId()), size, outcomes, shortMap);
    }

    @Override
    public List<MLClassifyModel> getModels() {
        return models;
    }

    public Map<String, Double[]> classifyInner(List<Triple<String, Object, Double>> newMap, int modelInt, int size, int outcomes, Map<Double, String> shortMap) {
        long time0 = System.currentTimeMillis();
        Map<String, double[]> map = getMap2(newMap);
        Map<String, Double[]> retMap = new HashMap<>();
        Dataset<Row> data = SparkUtil.createDFfromMap2(spark, map);
        try {
            Model model = modelMap.get(modelInt);
            if (model == null) {
                return retMap;
            }
            Dataset<Row> resultDF = model.transform(data);

            int j = 0;
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
                //MutablePair pair = (MutablePair) newMap.get(id);
                //pair.setRight(predict);
                Triple triple = newMap.get(j);
                if (triple.getRight() != null) {
                    int jj = 0;
                }
                Triple mutableTriple = new MutableTriple(triple.getLeft(), triple.getMiddle(), retVal[0]);
                //triple.setRight(acat);
                newMap.set(j, mutableTriple);
                j++;
            }
            log.info("classify done");
            return retMap;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        } finally {
            log.info("time classify model {} {} {}", modelInt, map.size(), (System.currentTimeMillis() - time0));            
        }
        return null;
    }

    public Double learntestInner(NeuralNetConfigs nnconfigs, List<Triple<String, Object, Double>> newMap, MLClassifyModel mlmodel, int size, int classes, String filename) {
        Double accuracy = null;
        long time0 = System.currentTimeMillis();
        if (spark == null) {
            return null;
        }
        Map<double[], Double> map = getMap(newMap);
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
            Model model = sparkModel.getModel(nnconfigs, train, size, classes);

            modelMap.put(filename, model);
            // compute accuracy on the test set                                         
            Dataset<Row> result = model.transform(test);
            Dataset<Row> predictionAndLabels = result.select("prediction", "label");
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setMetricName("accuracy");
            double eval = evaluator.evaluate(predictionAndLabels);
            log.info("Test set accuracy for {} = {}", mlmodel.getId(), eval);
            accuracyMap.put(filename, eval);
            accuracy = eval;

        } catch (Exception e) {
            log.error("Exception", e);
        } finally {
            log.info("time learn test model {} {} {}",mlmodel.getName(), map.size(),(System.currentTimeMillis() - time0));
        }
        return accuracy;
    }


    private Map<String, double[]> getMap2(List<Triple<String, Object, Double>> list) {
        Map<String, double[]> map = new HashMap<>();
        for (Triple<String, Object, Double> entry : list) {
            map.put(entry.getLeft(), (double[]) entry.getMiddle());
        }
        return map;
    }

    private Map<double[], Double> getMap(List<Triple<String, Object, Double>> list) {
        Map<double[], Double> map = new HashMap<>();
        for (Triple<String, Object, Double> entry : list) {
            map.put((double[]) entry.getMiddle(), entry.getRight());
        }
        return map;
    }

    public Double evalInner(int modelInt) {
        return accuracyMap.get(modelInt);
    }

    @Override
    public String getName() {
        return MLConstants.SPARK;
    }


    @Override
    public String getShortName() {
        return MLConstants.SP;
    }


    @Override
    public LearnTestClassifyResult learntestclassify(NeuralNetConfigs nnconfig, Aggregator indicator, List<Triple<String, Object, Double>> learnMap,
            MLClassifyModel mlmodel, int size, int classes, List<Triple<String, Object, Double>> classifyMap,
            Map<Double, String> shortMap, String path, String filename, NeuralNetCommand neuralnetcommand, MLMeta mlmeta, boolean classify) {
        LearnTestClassifyResult result2 = new LearnTestClassifyResult();
        if (neuralnetcommand.isMlclassify() && (classifyMap == null || classifyMap.isEmpty())) {
            result2.setCatMap(new HashMap<>());
            return result2;
        }
        Double accuracy = null;
        long time0 = System.currentTimeMillis();
        if (spark == null) {
            return null;
        }
        // return if
        // persist is false and dynamic is false
        boolean persist = mlmodel.wantPersist();
        if (!neuralnetcommand.isMldynamic() && !persist) {
            return null;
        }
        boolean exists = false;
        try {
            File file = new File(path + "/" + filename);
            exists = file.exists();
            if (!exists && (!neuralnetcommand.isMldynamic() && neuralnetcommand.isMlclassify())) {
                return null;
            }
        } catch (Exception e) {
            log.error("Exception", e);
        }
        Map<double[], Double> map = getMap(learnMap);
        Map<Double, Double> zeroMap = new HashMap<>();
        Map<Double, Double> revZeroMap = new HashMap<>();
        map = getZeroMap(map, zeroMap, revZeroMap);
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
                log.debug(" e " + Arrays.asList(e));
            }
            for (double[] e : map.keySet()) {
                List<Double> l = Arrays.stream(e).boxed().collect(Collectors.toList());
                log.debug(" e " + l.size() + " " + l);
            }
            MLClassifySparkModel sparkModel = (MLClassifySparkModel) mlmodel;
            PipelineModel model = null;
            if (!exists || neuralnetcommand.isMldynamic() || neuralnetcommand.isMllearn()) {
                model = sparkModel.getModel(nnconfig, train, size, classes);
            } else {
                model = PipelineModel.load(path + "/" + filename);
            }
            if (!neuralnetcommand.isMldynamic() && neuralnetcommand.isMllearn()) {
                model.write().overwrite().save(path + "/" + filename);
                //model = model.write.overwrite().save(path + "/" + filename);
            }
            // compute accuracy on the test set                                         
            Dataset<Row> result = model.transform(test);
            Dataset<Row> predictionAndLabels = result.select("prediction", "label");
            MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                    .setMetricName("accuracy");
            double eval = evaluator.evaluate(predictionAndLabels);
            log.info("Test set accuracy for {} = {}", mlmodel.getId(), eval);
            accuracyMap.put(filename, eval);
            accuracy = eval;
            Map<String, Double[]> retMap = new HashMap<>();
            Map<String, double[]> map2 = getMap2(classifyMap);
            Dataset<Row> data2 = SparkUtil.createDFfromMap2(spark, map2);
            int modelInt = Integer.valueOf(mlmodel.getId());
            Dataset<Row> resultDF = model.transform(data2);

            int j = 0;
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
                retVal[0] = revZeroMap.get(predict);
                retVal[1] = prob;
                retMap.put(id, retVal);
                //MutablePair pair = (MutablePair) newMap2.get(id);
                //pair.setRight(predict);
                Triple triple = classifyMap.get(j);
                if (triple.getRight() != null) {
                    int jj = 0;
                }
                Triple mutableTriple = new MutableTriple(triple.getLeft(), triple.getMiddle(), retVal[0]);
                //triple.setRight(acat);
                classifyMap.set(j, mutableTriple);
                j++;
            }
            log.info("classify done");
            result2.setAccuracy(accuracy);
            result2.setCatMap(retMap);
            if (retMap == null || retMap.isEmpty()) {
                int jj = 0;
            }
            return result2;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        } finally {
            log.info("time learn test classify model {} {} {}", Integer.valueOf(mlmodel.getId()), map.size(), (System.currentTimeMillis() - time0));            
        }
        return null;
    }

    private Map<double[], Double> getZeroMap(Map<double[], Double> map, Map<Double, Double> zeroMap,
            Map<Double, Double> revZeroMap) {
        Map<double[], Double> newMap = new HashMap<>();
        Set<Double> values = new HashSet<>(map.values());
        int i = 0;
        for (Double value : values) {
            revZeroMap.put((double) i, value);
            zeroMap.put(value, (double) i);
            i++;
        }
        for (Entry<double[], Double> entry : map.entrySet()) {
            Double value = entry.getValue();
            newMap.put(entry.getKey(), zeroMap.get(value));
        }
        return newMap;
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


    @Override
    public LearnTestClassifyResult dataset(NeuralNetConfigs nnconfigs, MLClassifyModel model,
            NeuralNetCommand neuralnetcommand, MLMeta mlmeta, String dataset) {
        return null;
    }
}


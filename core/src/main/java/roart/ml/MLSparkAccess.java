package roart.ml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

import roart.db.DbSpark;
import roart.indicator.Indicator;
import roart.indicator.IndicatorMACD;
import roart.ml.MLModel;
import roart.ml.MLSparkLRModel;
import roart.ml.MLSparkMCPModel;
import roart.util.Constants;
import roart.util.SparkUtil;

public class MLSparkAccess extends MLAccess {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	protected SparkSession spark;
	
    private Map<String, Model> modelMap = new HashMap<>();
    private Map<String, Double> accuracyMap = new HashMap<>();
    
	public MLSparkAccess() {
        findModels();	
        spark = DbSpark.getSparkSession();
	}
    private void findModels() {
        models = new ArrayList<>();
        if (IndicatorMACD.wantDNN()) {
            MLModel model = new MLSparkMCPModel();
            models.add(model);
        }
        if (IndicatorMACD.wantL()) {
            MLModel model = new MLSparkLRModel();
            models.add(model);
        }
    }
    @Override
    public void learntest(Indicator indicator, Map<double[], Double> map, MLModel model, int size, String period,
            String mapname, int outcomes) {
        //List<MLModel> models = model.getModels();
        //for (MLModel modelInt : models) {
       learntestInner(map, model, size, period, mapname, outcomes);       
    //}
    }

    @Override
    public Double eval(int modelInt, String period, String mapname) {
        return evalInner(modelInt, period, mapname);
    }

    @Override
    public Map<String, Double[]> classify(Indicator indicator, Map<String, double[]> map, MLModel model, int size,
            String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        Map<Integer, Map<String, Double[]>> retMap = new HashMap<>();
        //List<MLModel> models = getModels();
        //for (MLModel modelInt : models) {
         return classifyInner(map, new Integer(model.getId()), size, period, mapname, outcomes, shortMap);
        //}
        //return retMap;
    }

    @Override
    public List<MLModel> getModels() {
        return models;
    }
    
    public Map<String, Double[]> classifyInner(Map<String, double[]> map, int modelInt, int size, String period, String mapname, int outcomes, Map<Double, String> shortMap) {
        long time0 = System.currentTimeMillis();
        //List<double[]> arrList = new ArrayList<>();
        //arrList.add(array);
        Map<String, Double[]> retMap = new HashMap<>();
        Dataset<Row> data = SparkUtil.createDFfromMap2(spark, map);
        try {
            /*
            List<Row> jrdd = Arrays.asList(
                    RowFactory.create(content));

            String schemaString = "sentence";

            // Generate the schema based on the string of schema
            List<StructField> fields = new ArrayList<>();
            for (String fieldName : schemaString.split(" ")) {
                StructField field = DataTypes.createStructField(fieldName, DataTypes.StringType, true);
                fields.add(field);
            }
            StructType schema = DataTypes.createStructType(fields);
             */
            //Dataset<Row> sentenceDF = spark.createDataFrame(jrdd, schema);
            Model model = modelMap.get(modelInt+period+mapname);
            if (model == null) {
                return retMap;
            }
            Dataset<Row> resultDF = model.transform(data);

            for (Row row : resultDF.collectAsList()) {
                String id = row.getAs("id");
                Double predict = row.getAs("prediction");
                Double prob = null;
                if (IndicatorMACD.LOGISTICREGRESSION == modelInt) {
                try {
                    DenseVector probvector = row.getAs("probability");
                    double[] probarray = probvector.values();
                    prob = probarray[predict.intValue()];
                } catch (Exception e) {
                    log.error(Constants.EXCEPTION, e);
                }
                }
            //Map<Double, String> label = conf.labelsMap.get(language);
            //String cat = label.get(predict);
             //  log.info(" cat " + predict);
            //MachineLearningClassifyResult result = new MachineLearningClassifyResult();
            //result.result = cat;
                Double[] retVal = new Double[2];
                retVal[0] = predict;
                retVal[1] = prob;
                String label = shortMap.get(predict);
                retMap.put(id, retVal);
            }
            System.out.println("classed " + retMap.values());
            log.info("classify done");
            return retMap;
        } catch (Exception e) {
            log.error(Constants.EXCEPTION, e);
        } finally {
            log.info("time classify model " + modelInt + " " + period + " " + map.size() + " " + (System.currentTimeMillis() - time0));            
        }
        return null;
    }

    public void learntestInner(Map<double[], Double> map, MLModel mlmodel, int size, String period, String mapname, int outcomes) {
        long time0 = System.currentTimeMillis();
           if (spark == null) {
                return;
            }
            if (map.isEmpty()) {
                return;
            }
            Map<Double, Long> counts =
                    map.values().stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));       
           log.info("learning distribution " + counts);
            try {
             Dataset<Row> data = SparkUtil.createDFfromMap(spark, map);
        Dataset<Row>[] splits = data.randomSplit(new double[]{0.6, 0.4}, 1234);
        Dataset<Row> train = splits[0];
        Dataset<Row> test = splits[1];
        log.info("data size " + map.size());
        MLSparkModel sparkModel = (MLSparkModel) mlmodel;
        Model model = sparkModel.getModel(train, size, outcomes);
        
        modelMap.put(mlmodel.getId()+period+mapname, model);
                    // compute accuracy on the test set                                         
                    Dataset<Row> result = model.transform(test);
                    //result.schema().toString();
                    //result.show();
                    Dataset<Row> predictionAndLabels = result.select("prediction", "label");
                    //predictionAndLabels.show();
                    MulticlassClassificationEvaluator evaluator = new MulticlassClassificationEvaluator()
                      .setMetricName("accuracy");
                    double eval = evaluator.evaluate(predictionAndLabels);
                    log.info("Test set accuracy for " + mapname + " " + mlmodel.getId() + " " + period + " = " + eval);
                    accuracyMap.put(mlmodel.getId()+period+mapname, eval);

       
    } catch (Exception e) {
        log.error("Exception", e);
    } finally {
        log.info("time learn test model " + mlmodel.getName() + " " + period + " " + map.size() + " " + (System.currentTimeMillis() - time0));
    }
    }
    
    public Double evalInner(int modelInt, String period, String mapname) {
        //System.out.println("str vs " + modelStr+period+mapname + " :" + accuracyMap.keySet());
        return accuracyMap.get(modelInt+period+mapname);
    }
}


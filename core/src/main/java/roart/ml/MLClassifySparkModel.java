package roart.ml;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.indicator.IndicatorMACD;
import roart.util.Constants;
import roart.util.SparkUtil;

public abstract class MLClassifySparkModel extends MLClassifyModel {

    private static Logger log = LoggerFactory.getLogger(MLClassifySparkModel.class);
    
    public String getEngineName() {
        return "Spark ML";
    }

    public abstract Model getModel(Dataset<Row> train, int size, int outcomes);
    
}

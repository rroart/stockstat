package roart.ml;

import org.apache.spark.ml.Model;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MLPredictSparkModel extends MLPredictModel {

    private static Logger log = LoggerFactory.getLogger(MLClassifySparkModel.class);
    
    public String getEngineName() {
        return "Spark ML";
    }

    public abstract Model getModel(Dataset<Row> train, int size, int outcomes);
    
}

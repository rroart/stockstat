package roart.ml.spark;

import org.apache.spark.ml.Model;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.iclij.config.IclijConfig;
import roart.ml.model.MLPredictModel;

public abstract class MLPredictSparkModel extends MLPredictModel {

    public MLPredictSparkModel(IclijConfig conf) {
        super(conf);
    }

    private static Logger log = LoggerFactory.getLogger(MLClassifySparkModel.class);
    
    public String getEngineName() {
        return "Spark ML";
    }

    public abstract Model getModel(Dataset<Row> train, int size, int outcomes);
    
}

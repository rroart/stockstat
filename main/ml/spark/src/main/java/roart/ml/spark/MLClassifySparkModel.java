package roart.ml.spark;

import org.apache.spark.ml.Model;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MyMyConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;

public abstract class MLClassifySparkModel extends MLClassifyModel {

    protected static Logger log = LoggerFactory.getLogger(MLClassifySparkModel.class);
    
    public MLClassifySparkModel(MyMyConfig conf) {
        super(conf);
    }

    public String getEngineName() {
        return "Spark ML";
    }

    public abstract Model getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes);
    
    public abstract NeuralNetConfig getModel(NeuralNetConfigs conf);
    
}

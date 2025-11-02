package roart.spark;

import org.apache.spark.ml.Model;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.PipelineModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;
import roart.ml.common.MLMeta;

public abstract class MLClassifySparkModel {

    protected static Logger log = LoggerFactory.getLogger(MLClassifySparkModel.class);
    
    protected MLClassifyModel model;
    
    protected final boolean binary = false;

    public MLClassifySparkModel(MLClassifyModel model) {
        this.model = model;
    }

    public MLClassifyModel getModel() {
        return model;
    }

    public void setModel(MLClassifyModel model) {
        this.model = model;
    }

    public abstract PipelineModel getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes, boolean binary);
    
    public abstract NeuralNetConfig getModel(NeuralNetConfigs conf, boolean binary);
    }

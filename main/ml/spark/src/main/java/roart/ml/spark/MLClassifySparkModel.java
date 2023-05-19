package roart.ml.spark;

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

public abstract class MLClassifySparkModel extends MLClassifyModel {

    protected static Logger log = LoggerFactory.getLogger(MLClassifySparkModel.class);
    
    public MLClassifySparkModel(IclijConfig conf) {
        super(conf);
    }

    public String getEngineName() {
        return MLConstants.SPARK;
    }

    public abstract PipelineModel getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes);
    
    public abstract NeuralNetConfig getModel(NeuralNetConfigs conf);
    
    @Override
    public String getPath() {
        return getConf().getSparkMLPath();
    }
    
    @Override
    public String getShortName() {
        return getName();
    }

}

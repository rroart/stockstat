package roart.spark;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkLORConfig;
import roart.pipeline.common.aggregate.Aggregator;
import roart.ml.common.MLClassifyModel;

public class MLClassifySparkLORModel  extends MLClassifySparkModel {
    public MLClassifySparkLORModel(MLClassifyModel model) {
        super(model);
    }
    
    @Override
    public PipelineModel getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkLORConfig modelConf = (SparkLORConfig) getModel(conf);
        LogisticRegression reg = new LogisticRegression();
        reg.setMaxIter(modelConf.getMaxiter());
        reg.setTol(modelConf.getTol());
        //reg.setRegParam(modelConf.getReg());
        //if (modelConf.getElasticnet() != null) {
        //    reg.setElasticNetParam(modelConf.getElasticnet());
        //}
        log.info("Used ML config {}", modelConf);
        LogisticRegression dummy = new LogisticRegression();
        log.info("dymmy " + dummy.getElasticNetParam() + " " + dummy.getMaxIter() + " " + dummy.getRegParam() + " " + dummy.getThreshold() + " " + dummy.getTol() + " " + dummy.getStandardization() + " " + dummy.getFitIntercept());
        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] { reg } );
        return pipeline.fit(train);
    }

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        SparkLORConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkLORConfig();
        }    
        if (modelConf == null) {
            modelConf = getModel().convert(SparkLORConfig.class);
            if (modelConf == null) {
                modelConf = getModel().getDefault(SparkLORConfig.class);
            }
        }
        return modelConf;
    }
}


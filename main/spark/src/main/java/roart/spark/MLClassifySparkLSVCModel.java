package roart.spark;

import java.util.Arrays;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LinearSVC;
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
import roart.common.ml.SparkLSVCConfig;
import roart.ml.common.MLClassifyModel;

public  class MLClassifySparkLSVCModel  extends MLClassifySparkModel {
    public MLClassifySparkLSVCModel(MLClassifyModel model) {
        super(model);
    }
    
    @Override
    public PipelineModel getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes, boolean binary) {
        SparkLSVCConfig modelConf = getModel(conf, outcomes);
        log.info("Used ML config {}", modelConf);
        LinearSVC trainer = new LinearSVC()
                .setTol(modelConf.getTol())
                .setFitIntercept(modelConf.getFitintercept())
                //.setSeed(1234L)
                .setMaxIter(modelConf.getMaxiter());
        LinearSVC dummy = new LinearSVC();
        log.info("dymmy " + dummy.getMaxIter() + " " + dummy.getTol() + " " + dummy.getFitIntercept());
        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] { trainer } );
        return pipeline.fit(train);
    }

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf, boolean binary) {
        return null;
    }
    
    private SparkLSVCConfig getModel(NeuralNetConfigs conf, int outcomes) {
        SparkLSVCConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkLSVCConfig();
        }
        if (modelConf == null) {
            modelConf = getModel().convert(SparkLSVCConfig.class, binary);
            if (modelConf == null) {
                modelConf = getModel().getDefault(SparkLSVCConfig.class, binary);
            }
        }
        return modelConf;
    }

}

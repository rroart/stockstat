package roart.ml.spark;

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

public  class MLClassifySparkLSVCModel  extends MLClassifySparkModel {
    public MLClassifySparkLSVCModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return MLConstants.LINEARSUPPORTVECTORCLASSIFIER;
    }
    
    @Override
    public String getName() {
        return MLConstants.LSVC;
    }

    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGSPARKMLLSVCCONFIG;
    }

    @Override
    public PipelineModel getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes) {
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
    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        return null;
    }
    
    private SparkLSVCConfig getModel(NeuralNetConfigs conf, int outcomes) {
        SparkLSVCConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkLSVCConfig();
        }
        if (modelConf == null) {
            modelConf = convert(SparkLSVCConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(SparkLSVCConfig.class);
            }
        }
        return modelConf;
    }
    
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantSparkLSVCPersist();
    }

}

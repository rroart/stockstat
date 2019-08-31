package roart.ml.spark;

import java.util.Arrays;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LinearSVC;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkLRConfig;
import roart.common.ml.SparkLSVCConfig;

public  class MLClassifySparkLSVCModel  extends MLClassifySparkModel {
    public MLClassifySparkLSVCModel(MyMyConfig conf) {
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
    public Model getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkLSVCConfig modelConf = getModel(conf, outcomes);
        log.info("Used ML config {}", modelConf);
        LinearSVC trainer = new LinearSVC()
                .setTol(modelConf.getTol())
                .setFitIntercept(modelConf.getFitintercept())
                //.setSeed(1234L)
                .setMaxIter(modelConf.getMaxiter());
        LinearSVC dummy = new LinearSVC();
        log.info("dymmy " + dummy.getMaxIter() + " " + dummy.getTol() + " " + dummy.getFitIntercept());
        return trainer.fit(train);
    }

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        return null;
    }
    
    private SparkLSVCConfig getModel(NeuralNetConfigs conf, int outcomes) {
        SparkLSVCConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkLSVCConfig();
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

}

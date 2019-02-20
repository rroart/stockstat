package roart.ml.spark;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkLRConfig;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifySparkLRModel  extends MLClassifySparkModel {
    public MLClassifySparkLRModel(MyMyConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return MLConstants.LOGISTICREGRESSION;
    }
    
    @Override
    public String getName() {
        return MLConstants.LR;
    }

    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGSPARKMLLRCONFIG;
    }

    @Override
    public int getSizes(Aggregator indicator) { 
        return 2 * super.getSizes(indicator);
    }

    @Override
    public int getReturnSize() {
        return 2;
    }

    @Override
    public Model getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkLRConfig modelConf = (SparkLRConfig) getModel(conf);
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
        return reg.fit(train);
    }

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        SparkLRConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkLRConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(SparkLRConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(SparkLRConfig.class);
            }
        }
        return modelConf;
    }

}


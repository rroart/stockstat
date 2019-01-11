package roart.ml.spark;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.common.config.MLConstants;
import roart.common.constants.Constants;
import roart.common.ml.NNConfigs;
import roart.common.ml.SparkLRConfig;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifySparkLRModel  extends MLClassifySparkModel {
    @Override
    public int getId() {
        return MLConstants.LOGISTICREGRESSION;
    }
    @Override
    public String getName() {
        return MLConstants.LR;
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
    public Model getModel(NNConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkLRConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkLRConfig();
        }    
        if (modelConf == null) {
            modelConf = new SparkLRConfig(5, 1E-6);
        }
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

}


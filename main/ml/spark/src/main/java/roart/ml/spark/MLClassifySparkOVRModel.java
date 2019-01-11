package roart.ml.spark;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.OneVsRest;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.common.config.MLConstants;
import roart.common.ml.NNConfigs;
import roart.common.ml.SparkOVRConfig;
import roart.ml.common.MLClassifyModel;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifySparkOVRModel  extends MLClassifySparkModel {
    @Override
    public int getId() {
        return MLConstants.ONEVSREST;
    }
    @Override
    public String getName() {
        return MLConstants.OVR;
    }

    @Override
    public int getSizes(Aggregator indicator) { 
        return super.getSizes(indicator);
    }

    @Override
    public int getReturnSize() {
        return 1;
    }

    @Override
    public Model getModel(NNConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkOVRConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkOVRConfig();
        }
        if (modelConf == null) {
            modelConf = new SparkOVRConfig(10, 1E-6, true);
        }
        LogisticRegression lr = new LogisticRegression()
                .setMaxIter(modelConf.getMaxiter())
                .setTol(modelConf.getTol())
                .setFitIntercept(modelConf.getFitintercept());
        OneVsRest ovr = new OneVsRest().setClassifier(lr);

        log.info("Used ML config {}", modelConf);
        // train the multiclass model.
        return ovr.fit(train);
    }

}


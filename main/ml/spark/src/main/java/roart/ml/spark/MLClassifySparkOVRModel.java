package roart.ml.spark;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.LogisticRegression;
import org.apache.spark.ml.classification.OneVsRest;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.ml.PipelineModel;
import org.apache.spark.ml.Pipeline;
import org.apache.spark.ml.PipelineStage;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkLORConfig;
import roart.common.ml.SparkOVRConfig;
import roart.ml.common.MLClassifyModel;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifySparkOVRModel  extends MLClassifySparkModel {
    public MLClassifySparkOVRModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return MLConstants.ONEVSREST;
    }
    
    @Override
    public String getName() {
        return MLConstants.OVR;
    }

    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGSPARKMLOVRCONFIG;
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
    public PipelineModel getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkOVRConfig modelConf = (SparkOVRConfig) getModel(conf);
        LogisticRegression lr = new LogisticRegression()
                .setMaxIter(modelConf.getMaxiter())
                .setTol(modelConf.getTol())
                .setFitIntercept(modelConf.getFitintercept());
        OneVsRest ovr = new OneVsRest().setClassifier(lr);

        log.info("Used ML config {}", modelConf);
        // train the multiclass model.
        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] { ovr } );
        return pipeline.fit(train);
    }

    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        SparkOVRConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkOVRConfig();
        }
        if (modelConf == null) {
            //modelConf = convert(getKey(), SparkOVRConfig.class);
            modelConf = convert(SparkOVRConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(SparkOVRConfig.class);
            }
        }
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantSparkOVRPersist();
    }

}


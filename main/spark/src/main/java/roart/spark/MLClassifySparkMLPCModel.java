package roart.spark;

import java.util.Arrays;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
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
import roart.common.ml.SparkMLPCConfig;
import roart.ml.common.MLClassifyModel;

public class MLClassifySparkMLPCModel  extends MLClassifySparkModel {
    public MLClassifySparkMLPCModel(MLClassifyModel model) {
        super(model);
    }
    
    @Override
    public PipelineModel getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkMLPCConfig modelConf = getModel(conf, outcomes);
        int layer = modelConf.getLayers();
        int hidden = modelConf.getHidden();
        //int[] nn = modelConf.getNn();
        int[] layers = new int[layer + 2];
        for (int i = 1; i <= layer; i++) {
            layers[i] = hidden;
        }
        layers[0] = size;
        layers[layers.length - 1] = outcomes + 1;
        log.info("Used ML config {} {}", modelConf, Arrays.toString(layers));
        MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setTol(modelConf.getTol())
                //.setLearningRate(0.03)
                //.setSeed(1234L)
                .setMaxIter(modelConf.getMaxiter());
        MultilayerPerceptronClassifier dummy = new MultilayerPerceptronClassifier();
        log.info("dymmy " + dummy.getBlockSize() + " " + dummy.getMaxIter() + " " + dummy.getSeed() + " " + dummy.getStepSize() + " " + dummy.getTol());
        Pipeline pipeline = new Pipeline().setStages(new PipelineStage[] { trainer } );
        return pipeline.fit(train);
    }

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        return null;
    }
    
    private SparkMLPCConfig getModel(NeuralNetConfigs conf, int outcomes) {
        SparkMLPCConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkMLPCConfig();
        }
        if (modelConf == null) {
            modelConf = getModel().convert(SparkMLPCConfig.class);
            if (modelConf == null) {
                modelConf = getModel().getDefault(SparkMLPCConfig.class);
                /*
                int[] layers = modelConf.getNn();
                layers[0] += outcomes;
                layers[1] += outcomes;
                modelConf.setNn(layers);
                */
            }
                    //new int[]{outcomes + 1, outcomes + 1};
            // 2 hidden layers
            //modelConf = new SparkMLPCConfig(100, 2, 1E-6);
        }
        return modelConf;
    }
}

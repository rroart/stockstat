package roart.ml;

import java.util.Arrays;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.config.MLConstants;
import roart.indicator.IndicatorMACD;

public  class MLClassifySparkMCPModel  extends MLClassifySparkModel {
    @Override
    public int getId() {
        return IndicatorMACD.MULTILAYERPERCEPTRONCLASSIFIER;
    }
    @Override
    public String getName() {
        return MLConstants.MCP;
    }

    @Override
    public Model getModel(NNConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkMCPConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkMCPConfig();
        }
        if (modelConf == null) {
            int[] layers = new int[]{outcomes + 1, outcomes + 1};
            // 2 hidden layers
            modelConf = new SparkMCPConfig(100, 2, 1E-6);
            modelConf.setNn(layers);
        }
        int layer = modelConf.getLayers();
        int[] nn = modelConf.getNn();
        int[] layers = new int[layer + 2];
        for (int i = 1; i <= layer; i++) {
            layers[i] = nn[i - 1];
        }
        layers[0] = size;
        layers[layers.length - 1] = outcomes + 1;
        log.info("Used ML config {} {}", modelConf, Arrays.toString(layers));
        MultilayerPerceptronClassifier trainer = new MultilayerPerceptronClassifier()
                .setLayers(layers)
                .setTol(modelConf.getTol())
                //.setSeed(1234L)
                .setMaxIter(modelConf.getMaxiter());
        MultilayerPerceptronClassifier dummy = new MultilayerPerceptronClassifier();
        log.info("dymmy " + dummy.getBlockSize() + " " + dummy.getMaxIter() + " " + dummy.getSeed() + " " + dummy.getStepSize() + " " + dummy.getTol());
        return trainer.fit(train);
    }
}

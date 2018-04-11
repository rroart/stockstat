package roart.ml;

import java.util.Arrays;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.config.MLConstants;

public  class MLClassifySparkMCPModel  extends MLClassifySparkModel {
    @Override
    public int getId() {
        return 1;
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
            modelConf = new SparkMCPConfig(100, 2);
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
                .setBlockSize(128)
                .setSeed(1234L)
                .setMaxIter(modelConf.getMaxiter());
        return trainer.fit(train);
    }
}

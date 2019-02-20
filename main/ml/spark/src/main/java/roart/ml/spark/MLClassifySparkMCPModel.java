package roart.ml.spark;

import java.util.Arrays;

import org.apache.spark.ml.Model;
import org.apache.spark.ml.classification.MultilayerPerceptronClassifier;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.constants.Constants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkLRConfig;
import roart.common.ml.SparkMCPConfig;

public  class MLClassifySparkMCPModel  extends MLClassifySparkModel {
    public MLClassifySparkMCPModel(MyMyConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return MLConstants.MULTILAYERPERCEPTRONCLASSIFIER;
    }
    
    @Override
    public String getName() {
        return MLConstants.MCP;
    }

    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGSPARKMLMCPCONFIG;
    }

    @Override
    public Model getModel(NeuralNetConfigs conf, Dataset<Row> train, int size, int outcomes) {
        SparkMCPConfig modelConf = getModel(conf, outcomes);
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

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        return null;
    }
    
    private SparkMCPConfig getModel(NeuralNetConfigs conf, int outcomes) {
        SparkMCPConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkMCPConfig();
        }
        if (modelConf == null) {
            modelConf = convert(SparkMCPConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(SparkMCPConfig.class);
                int[] layers = modelConf.getNn();
                layers[0] += outcomes;
                layers[1] += outcomes;
                modelConf.setNn(layers);
            }
                    //new int[]{outcomes + 1, outcomes + 1};
            // 2 hidden layers
            //modelConf = new SparkMCPConfig(100, 2, 1E-6);
        }
        return modelConf;
    }
}

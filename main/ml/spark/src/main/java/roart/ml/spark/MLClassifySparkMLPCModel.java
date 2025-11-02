package roart.ml.spark;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkMLPCConfig;

public class MLClassifySparkMLPCModel  extends MLClassifySparkModel {
    public MLClassifySparkMLPCModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return MLConstants.MULTILAYERPERCEPTRONCLASSIFIER;
    }
    
    @Override
    public String getName() {
        return MLConstants.MLPC;
    }

    @Override
    public String getKey(boolean binary) {
        return ConfigConstants.MACHINELEARNINGSPARKMLMLPCCONFIG;
    }

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf, boolean binary) {
        return null;
    }
    
    private SparkMLPCConfig getModel(NeuralNetConfigs conf, int outcomes, boolean binary) {
        SparkMLPCConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkMLPCConfig();
        }
        if (modelConf == null) {
            modelConf = convert(SparkMLPCConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(SparkMLPCConfig.class, binary);
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

    @Override
    public boolean wantPersist() {
        return getConf().wantSparkMLPCPersist();
    }
}

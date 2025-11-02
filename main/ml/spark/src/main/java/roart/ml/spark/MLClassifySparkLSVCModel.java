package roart.ml.spark;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkLSVCConfig;

public  class MLClassifySparkLSVCModel  extends MLClassifySparkModel {
    public MLClassifySparkLSVCModel(IclijConfig conf) {
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
    public String getKey(boolean binary) {
        return ConfigConstants.MACHINELEARNINGSPARKMLLSVCCONFIG;
    }

    @Override
    public NeuralNetConfig getModel(NeuralNetConfigs conf, boolean binary) {
        return null;
    }
    
    private SparkLSVCConfig getModel(NeuralNetConfigs conf, int outcomes, boolean binary) {
        SparkLSVCConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkLSVCConfig();
        }
        if (modelConf == null) {
            modelConf = convert(SparkLSVCConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(SparkLSVCConfig.class, binary);
            }
        }
        return modelConf;
    }
    
    public boolean isBinary() {
        return true;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantSparkLSVCPersist();
    }

}

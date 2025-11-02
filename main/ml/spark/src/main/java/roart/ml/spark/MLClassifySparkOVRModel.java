package roart.ml.spark;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkOVRConfig;
import roart.iclij.config.IclijConfig;
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
    public String getKey(boolean binary) {
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

    public NeuralNetConfig getModel(NeuralNetConfigs conf, boolean binary) {
        SparkOVRConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkOVRConfig();
        }
        if (modelConf == null) {
            //modelConf = convert(getKey(), SparkOVRConfig.class);
            modelConf = convert(SparkOVRConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(SparkOVRConfig.class, binary);
            }
        }
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantSparkOVRPersist();
    }

}


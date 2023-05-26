package roart.ml.spark;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.common.ml.SparkLORConfig;
import roart.pipeline.common.aggregate.Aggregator;

public class MLClassifySparkLORModel  extends MLClassifySparkModel {
    public MLClassifySparkLORModel(IclijConfig conf) {
        super(conf);
    }
    
    @Override
    public int getId() {
        return MLConstants.LOGISTICREGRESSION;
    }
    
    @Override
    public String getName() {
        return MLConstants.LOR;
    }

    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGSPARKMLLORCONFIG;
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
    public NeuralNetConfig getModel(NeuralNetConfigs conf) {
        SparkLORConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getSparkConfig().getSparkLORConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(SparkLORConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(SparkLORConfig.class);
            }
        }
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantSparkLORPersist();
    }

}


package roart.ml.spark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.common.MLClassifyModel;

public abstract class MLClassifySparkModel extends MLClassifyModel {

    protected static Logger log = LoggerFactory.getLogger(MLClassifySparkModel.class);
    
    public MLClassifySparkModel(IclijConfig conf) {
        super(conf);
    }

    public String getEngineName() {
        return MLConstants.SPARK;
    }

    public abstract NeuralNetConfig getModel(NeuralNetConfigs conf);
    
    @Override
    public String getPath() {
        return getConf() != null ? getConf().getSparkMLPath() : null;
    }
    
    @Override
    public String getShortName() {
        return getName();
    }

}

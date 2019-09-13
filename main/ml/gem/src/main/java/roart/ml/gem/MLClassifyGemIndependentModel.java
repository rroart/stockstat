package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.GemIConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemIndependentModel  extends MLClassifyGemModel {
    public MLClassifyGemIndependentModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 2;
    }

    @Override
    public String getName() {
        return MLConstants.INDEPENDENT;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGGEMINDEPENDENTCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        GemIConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemIConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemIConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(GemIConfig.class);
            }
        }
        param.setGemIConfig(modelConf);
        return modelConf;
    }

}

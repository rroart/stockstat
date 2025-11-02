package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.GemIConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemIndependentModel  extends MLClassifyGemModel {
    public MLClassifyGemIndependentModel(IclijConfig conf) {
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
    public String getShortName() {
        return MLConstants.IN;
    }
    
    @Override
    public String getKey(boolean binary) {
        return ConfigConstants.MACHINELEARNINGGEMINDEPENDENTCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        GemIConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemIConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemIConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(GemIConfig.class, binary);
            }
        }
        param.setGemIConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantGemIndependentPersist();
    }

}

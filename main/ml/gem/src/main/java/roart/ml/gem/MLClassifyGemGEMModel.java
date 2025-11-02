package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.GemGEMConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemGEMModel  extends MLClassifyGemModel {
    public MLClassifyGemGEMModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 5;
    }

    @Override
    public String getName() {
        return MLConstants.GEM;
    }
    
    @Override
    public String getShortName() {
        return MLConstants.GEM;
    }
    
    @Override
    public String getKey(boolean binary) {
        return ConfigConstants.MACHINELEARNINGGEMGEMCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param, boolean binary) {
        GemGEMConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemGEMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemGEMConfig.class, binary);
            if (modelConf == null) {
                modelConf = getDefault(GemGEMConfig.class, binary);
            }
        }
        param.setGemGEMConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantGemGEMPersist();
    }

}

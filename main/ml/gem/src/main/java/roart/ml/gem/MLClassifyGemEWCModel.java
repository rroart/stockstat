package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.GemEWCConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemEWCModel  extends MLClassifyGemModel {
    public MLClassifyGemEWCModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 4;
    }

    @Override
    public String getName() {
        return MLConstants.EWC;
    }
    
    @Override
    public String getShortName() {
        return MLConstants.EWC;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGGEMEWCCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        GemEWCConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemEWCConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemEWCConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(GemEWCConfig.class);
            }
        }
        param.setGemEWCConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantGemEWCPersist();
    }

}

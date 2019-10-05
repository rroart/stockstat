package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.GemGEMConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemGEMModel  extends MLClassifyGemModel {
    public MLClassifyGemGEMModel(MyMyConfig conf) {
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
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGGEMGEMCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        GemGEMConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemGEMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemGEMConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(GemGEMConfig.class);
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

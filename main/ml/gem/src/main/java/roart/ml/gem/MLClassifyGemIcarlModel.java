package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.GemIcarlConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemIcarlModel  extends MLClassifyGemModel {
    public MLClassifyGemIcarlModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 6;
    }

    @Override
    public String getName() {
        return MLConstants.ICARL;
    }
    
    @Override
    public String getShortName() {
        return MLConstants.IC;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGGEMICARLCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        GemIcarlConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemIcarlConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemIcarlConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(GemIcarlConfig.class);
            }
        }
        param.setGemIcarlConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantGemIcarlPersist();
    }

}

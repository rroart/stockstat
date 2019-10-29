package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.common.config.MyMyConfig;
import roart.common.ml.GemSConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemSingleModel  extends MLClassifyGemModel {
    public MLClassifyGemSingleModel(MyMyConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 1;
    }

    @Override
    public String getName() {
        return MLConstants.SINGLE;
    }
    
    @Override
    public String getShortName() {
        return MLConstants.S;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGGEMSINGLECONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        GemSConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemSConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemSConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(GemSConfig.class);
            }
        }
        param.setGemSConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantGemSinglePersist();
    }

}

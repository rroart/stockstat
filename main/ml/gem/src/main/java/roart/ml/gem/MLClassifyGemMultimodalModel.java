package roart.ml.gem;

import roart.common.config.ConfigConstants;
import roart.common.config.MLConstants;
import roart.iclij.config.IclijConfig;
import roart.common.ml.GemMMConfig;
import roart.common.ml.NeuralNetConfig;
import roart.common.ml.NeuralNetConfigs;
import roart.ml.model.LearnTestClassify;

public class MLClassifyGemMultimodalModel  extends MLClassifyGemModel {
    public MLClassifyGemMultimodalModel(IclijConfig conf) {
        super(conf);
    }

    @Override
    public int getId() {
        return 3;
    }

    @Override
    public String getName() {
        return MLConstants.MULTIMODAL;
    }
    
    @Override
    public String getShortName() {
        return MLConstants.MM;
    }
    
    @Override
    public String getKey() {
        return ConfigConstants.MACHINELEARNINGGEMMULTIMODALCONFIG;
    }

    @Override
    public NeuralNetConfig getModelAndSet(NeuralNetConfigs conf, LearnTestClassify param) {
        GemMMConfig modelConf = null;
        if (conf != null) {
            modelConf = conf.getGemConfig().getGemMMConfig();
        }    
        if (modelConf == null) {
            modelConf = convert(GemMMConfig.class);
            if (modelConf == null) {
                modelConf = getDefault(GemMMConfig.class);
            }
        }
        param.setGemMMConfig(modelConf);
        return modelConf;
    }

    @Override
    public boolean wantPersist() {
        return getConf().wantGemMultiModalPersist();
    }

}

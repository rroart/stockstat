package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class MachineLearningMLRSIConfig extends ActionComponentConfigML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getMachineLearningMLRSIEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getMachineLearningMLRSIMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.MACHINELEARNINGMLRSI);
    }

}


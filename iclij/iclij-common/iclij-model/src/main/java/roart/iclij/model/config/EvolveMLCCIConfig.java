package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class EvolveMLCCIConfig extends ActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getEvolveMLCCIEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getEvolveMLCCIMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.EVOLVEMLCCI);
    }

}

package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class EvolveMLATRConfig extends ActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getEvolveMLATREvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getEvolveMLATRMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.EVOLVEMLATR);
    }

}

package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class EvolveMLIndicatorConfig extends ActionComponentConfigML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getEvolveMLIndicatorEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getEvolveMLIndicatorMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.EVOLVEMLINDICATOR);
    }

}

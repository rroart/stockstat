package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class EvolveMLSTOCHConfig extends ActionComponentConfigML {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
	return config.getEvolveMLSTOCHEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getEvolveMLSTOCHMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.EVOLVEMLSTOCH);
    }

}

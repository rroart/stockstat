package roart.iclij.model.config;




import roart.iclij.config.IclijConfigConstants;
import roart.iclij.config.IclijConfig;

public class FindProfitMLSTOCHConfig extends ActionComponentConfig {
    @Override
    public String getLocalEvolutionConfig(IclijConfig config) {
        return config.getFindProfitMLSTOCHEvolutionConfig();
    }

    @Override
    public String getLocalMLConfig(IclijConfig config) {
        return config.getFindProfitMLSTOCHMLConfig();
    }

    @Override
    public int getPriority(IclijConfig srv) {
        return getPriority(srv, IclijConfigConstants.FINDPROFITMLSTOCH);
    }

}

